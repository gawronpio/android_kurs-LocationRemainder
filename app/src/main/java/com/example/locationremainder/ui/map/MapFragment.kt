package com.example.locationremainder.ui.map

import android.annotation.SuppressLint
import android.content.res.Resources
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
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.locationremainder.R
import com.example.locationremainder.data.Poi
import com.example.locationremainder.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

const val TAG = "MapFragment"

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private val viewModel: MapViewModel by viewModels() {
        MapViewModelFactory(requireActivity().application)
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.map_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.map_type_normal -> {
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    true
                }
                R.id.map_type_satellite -> {
                    map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    true
                }
                R.id.map_type_hybrid -> {
                    map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    true
                }
                R.id.map_type_terrain -> {
                    map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    true
                }
                R.id.map_type_fancy -> {
                    try {
                        val success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                requireActivity(),
                                R.raw.map_style_fancy
                            )
                        )
                        if(!success) {
                            Toast.makeText(requireContext(), getString(R.string.map_style_failed), Toast.LENGTH_SHORT).show()
                            map.mapType = GoogleMap.MAP_TYPE_NORMAL
                        }
                    } catch (e: Resources.NotFoundException) {
                        Log.e(TAG, "Can't find style. Error: $e")
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.poiPickedBtn.setOnClickListener {
            if(viewModel.poi.location.isBlank()) {
                Toast.makeText(requireContext(), getString(R.string.poi_needed), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(MapFragmentDirections.actionMapFragmentToDetailFragment(viewModel.poi))
        }

        Toast.makeText(requireContext(), getString(R.string.map_instructions), Toast.LENGTH_LONG).show()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        map = p0

        viewModel.location.observe(viewLifecycleOwner) { location ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                location,
                requireActivity().resources.getString(R.string.default_zoom_level).toFloatOrNull() ?: 10f))
        }

        map.isMyLocationEnabled = viewModel.isLocationPermissionGranted()
        viewModel.findLocation()
        setPoiClick()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    private fun setPoiClick() {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            viewModel.poi = Poi(
                latitude = poi.latLng.latitude,
                longitude = poi.latLng.longitude,
                location = poi.name
            )
        }
    }
}