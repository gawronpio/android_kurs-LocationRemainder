package com.example.locationremainder.ui.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.locationremainder.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class MapViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val _location: MutableLiveData<LatLng> = MutableLiveData()
    val location: LiveData<LatLng>
        get() = _location
    private val app = application
    private val resources = app.resources

    var poi_location: LatLng? = null

    init {
        val lat = resources.getString(R.string.default_latitude).toDoubleOrNull() ?: 0.0
        val lon = resources.getString(R.string.default_longitude).toDoubleOrNull() ?: 0.0
        _location.value = LatLng(lat, lon)
        findLocation()
    }

    fun isLocationPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            app.applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }

    @SuppressLint("MissingPermission")
    fun findLocation() {
        if(isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { _location.value = LatLng(it.latitude, it.longitude) }
            }
        }
    }
}