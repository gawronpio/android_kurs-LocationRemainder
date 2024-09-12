package com.example.locationremainder.ui.main

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.viewModels
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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationremainder.MainActivity.Companion.ACTION_GEOFENCE_EVENT
import com.example.locationremainder.R
import com.example.locationremainder.data.Poi
import com.example.locationremainder.data.PoiDao
import com.example.locationremainder.data.PoiDatabase
import com.example.locationremainder.databinding.FragmentMainBinding
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

private const val TAG = "MainFragment"

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val poiDao: PoiDao by lazy { PoiDatabase.getInstance(requireContext()).poiDatabaseDao }

    private val viewModel: MainViewModel by viewModels() {
        MainViewModelFactory(poiDao, requireActivity().application)
    }

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
            startGeoFencing()
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.location_permissions_denied), Toast.LENGTH_LONG).show()
        }
    }
    private val requestBackgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted) {
            startGeoFencing()
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.background_location_permissions_denied), Toast.LENGTH_LONG).show()
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireActivity(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)

        val manager = LinearLayoutManager(activity)
        binding.poiRecycler.layoutManager = manager
        val adapter = MainRecyclerAdapter(MainListener { poiId ->
            lifecycleScope.launch {
                val poiData = poiDao.get(poiId)
                poiData?.let {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(it))
                }
            }
        })
        binding.poiRecycler.adapter = adapter

        binding.addBtn.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToMapFragment())
        }

        viewModel.pois.observe(viewLifecycleOwner) {
            if(it.isNullOrEmpty()) {
                binding.noDataImg.isVisible = true
                binding.noDataText.isVisible = true
            } else {
                adapter.addAndSubmitList(it)
                binding.noDataImg.isVisible = false
                binding.noDataText.isVisible = false
            }
        }

        viewModel.refresh()

        parentFragmentManager.setFragmentResultListener("poiData", this) { key, bundle ->
            val newPoi: Poi? = bundle.getParcelable("newPoi")
            newPoi?.let { viewModel.saveNewPoiAndRefresh(it) }
        }

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        viewModel.newPoi.observe(viewLifecycleOwner) {
            it?.let { createGeofence() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }

    @SuppressLint("NewApi")
    private fun requestPermissions() {
        requestLocationPermission()
        requestBackgroundLocationPermission()
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

    private fun requestLocationPermission() {
        if(!hasLocationPermissions()) {
            val snackbar = Snackbar.make(
                binding.root,
                getString(R.string.location_request_info),
                Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(getString(R.string.ok)) {
                requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
            snackbar.show()
        } else {
            startGeoFencing()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        if(!hasBackgroundLocationPermissions()) {
            val snackbar = Snackbar.make(
                binding.root,
                getString(R.string.background_location_request_info),
                Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(getString(R.string.ok)) {
                requestBackgroundLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            snackbar.show()
        } else {
            startGeoFencing()
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
            viewModel.deleteNewPoi()
            return
        }
        val id = viewModel.newPoi.value!!.id.toString()
        val position = LatLng(viewModel.newPoi.value!!.latitude, viewModel.newPoi.value!!.longitude)
        val radius = viewModel.newPoi.value!!.radius!!.toFloat()


        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(
                position.latitude,
                position.longitude,
                radius
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnFailureListener { exception ->
                Toast.makeText(requireContext(), getString(R.string.geofence_add_failed), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deleteNewPoi()
    }
}