package com.example.locationremainder.ui.reminderslist

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationremainder.MainActivity.Companion.ACTION_GEOFENCE_EVENT
import com.example.locationremainder.R
import com.example.locationremainder.data.ReminderDataSource
import com.example.locationremainder.data.dto.ReminderDTO
import com.example.locationremainder.databinding.FragmentRemindersListBinding
import com.example.locationremainder.geofence.GeofenceBroadcastReceiver
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.koin.core.component.KoinComponent
import com.example.locationremainder.data.dto.Result

private const val TAG = "RemindersListFragment"

class RemindersListFragment : Fragment(), KoinComponent {

    private lateinit var binding: FragmentRemindersListBinding
    private val remindersLocalRepository: ReminderDataSource by inject()
    private val viewModel: RemindersListViewModel by viewModel()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.logout_menu_item -> {
                    AuthUI.getInstance()
                        .signOut(requireContext())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                findNavController().navigateUp()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.logout_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    return true
                }
            }
            return false
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted) {
            requestBackgroundLocationPermission()
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.location_permissions_denied), Toast.LENGTH_LONG).show()
        }
    }
    private val requestBackgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted) {
            requestNotificationPermission()
            startGeoFencing()
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.background_location_permissions_denied), Toast.LENGTH_LONG).show()
        }
    }
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(!isGranted) {
            Toast.makeText(requireContext(),
                getString(R.string.notification_permissions_denied), Toast.LENGTH_LONG).show()
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireActivity(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reminders_list, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        createNotificationChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)

        val manager = LinearLayoutManager(activity)
        binding.remindersRecycler.layoutManager = manager
        val adapter = RemindersListRecyclerAdapter(RemindersListListener { remindId ->
            lifecycleScope.launch {
                val result = remindersLocalRepository.getReminder(remindId)
                if(result is Result.Success) {
                    findNavController().navigate(
                        RemindersListFragmentDirections.actionMainFragmentToDetailFragment(
                            result.data
                        )
                    )
                }
            }
        })
        binding.remindersRecycler.adapter = adapter

        binding.addBtn.setOnClickListener {
            findNavController().navigate(RemindersListFragmentDirections.actionMainFragmentToMapFragment())
        }

        viewModel.reminders.observe(viewLifecycleOwner) {
            if(it.isNullOrEmpty()) {
                binding.noDataImg.visibility = View.VISIBLE
                binding.noDataText.visibility = View.VISIBLE
                binding.remindersRecycler.visibility = View.GONE
            } else {
                binding.noDataImg.visibility = View.GONE
                binding.noDataText.visibility = View.GONE
                binding.remindersRecycler.visibility = View.VISIBLE
                adapter.addAndSubmitList(it)
            }
        }

        viewModel.refresh()

        parentFragmentManager.setFragmentResultListener("reminderData", this) { key, bundle ->
            val newReminderDTO: ReminderDTO? = bundle.getParcelable("newReminder")
            newReminderDTO?.let {
                viewModel.saveNewReminderAndRefresh(it)
                Toast.makeText(requireContext(), getString(R.string.remainder_saved), Toast.LENGTH_SHORT).show()
            }
        }

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        viewModel.newReminderDTO.observe(viewLifecycleOwner) {
            it?.let { createGeofence() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gets the reminder id from notification
        requireActivity().intent.getLongExtra("id", -1).let { id ->
            if (id != -1L) {
                requireActivity().intent.removeExtra("id")
                lifecycleScope.launch {
                    val result = remindersLocalRepository.getReminder(id)
                    if(result is Result.Success) {
                        findNavController().navigate(
                            RemindersListFragmentDirections.actionMainFragmentToDetailFragment(
                                result.data
                            )
                        )
                    }
                }
            }
        }

        requestPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }

    @SuppressLint("NewApi")
    private fun requestPermissions() {
        if(requestLocationPermission()) {
            if(requestBackgroundLocationPermission()) {
                requestNotificationPermission()
            }
        }
    }

    private fun hasPermissions(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasLocationPermissions(): Boolean {
        return hasPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun hasBackgroundLocationPermissions(): Boolean {
        return if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            true
        } else {
            hasPermissions(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    private  fun hasNotificationPermission(): Boolean {
        return if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            hasPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestLocationPermission(): Boolean {
        return if(!hasLocationPermissions()) {
            Snackbar
                .make(
                    binding.root,
                    getString(R.string.location_request_info),
                    Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.ok)) {
                    requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
                .show()
            false
        } else {
            startGeoFencing()
            true
        }
    }

    @SuppressLint("NewApi")
    private fun requestBackgroundLocationPermission(): Boolean {
        return if(!hasBackgroundLocationPermissions()) {
            Snackbar
                .make(
                    binding.root,
                    getString(R.string.background_location_request_info),
                    Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.ok)) {
                    requestBackgroundLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                .show()
            false
        } else {
            startGeoFencing()
            true
        }
    }

    @SuppressLint("NewApi")
    private fun requestNotificationPermission(): Boolean {
        return if(!hasNotificationPermission()) {
            Snackbar
                .make(
                    binding.root,
                    getString(R.string.notification_request_info),
                    Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.ok)) {
                    requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                .show()
            false
        } else {
            true
        }
    }

    private fun startGeoFencing() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(locationSettingsRequest)
        locationSettingsResponseTask.addOnCompleteListener {
            if(!it.isSuccessful) {
                Log.e(TAG, "Location settings failed")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createGeofence() {
        if (!hasLocationPermissions() || !hasBackgroundLocationPermissions()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.location_permissions_denied), Toast.LENGTH_LONG
            ).show()
            viewModel.deleteNewReminder()
            return
        }
        val id = viewModel.newReminderDTO.value!!.id.toString()
        val position = LatLng(viewModel.newReminderDTO.value!!.latitude, viewModel.newReminderDTO.value!!.longitude)
        val radius = viewModel.newReminderDTO.value!!.radius!!.toFloat()


        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(
                position.latitude,
                position.longitude,
                radius
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), getString(R.string.geofence_add_failed), Toast.LENGTH_SHORT).show()
            }

        viewModel.deleteNewReminder()
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.enableVibration(true)
        notificationChannel.description = channelName

        val notificationManager = requireActivity().getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}