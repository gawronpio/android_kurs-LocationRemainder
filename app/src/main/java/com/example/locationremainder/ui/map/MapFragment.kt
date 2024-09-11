package com.example.locationremainder.ui.map

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.locationremainder.R
import com.example.locationremainder.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private val viewModel: MapViewModel by viewModels() {
        MapViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.poiPickedBtn.setOnClickListener {
            if(viewModel.poi_location == null) {
                Toast.makeText(requireContext(), getString(R.string.poi_needed), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO navigate to Detail Fragment
//            findNavController().navigate(MapFragmentDirections.actionMapFragmentToMainFragment(viewModel.poi_location!!))
        }
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
            viewModel.poi_location = poi.latLng
        }
    }
}