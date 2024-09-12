package com.example.locationremainder.ui.main

import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationremainder.R
import com.example.locationremainder.data.Poi
import com.example.locationremainder.data.PoiDao
import com.example.locationremainder.data.PoiDatabase
import com.example.locationremainder.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(requireContext(),
                getString(R.string.location_permissions_denied), Toast.LENGTH_SHORT).show()
        }
    }

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkLocationPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }

    private fun checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            val snackbar = Snackbar.make(
                binding.root,
                getString(R.string.location_request_info),
                Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(getString(R.string.ok)) {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
            snackbar.show()
        }
    }
}