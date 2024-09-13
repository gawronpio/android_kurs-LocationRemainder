package com.example.locationremainder.ui.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.locationremainder.R
import com.example.locationremainder.data.dto.ReminderDTO
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

private const val TAG = "MapViewModel"

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val _location: MutableLiveData<LatLng> = MutableLiveData()
    val location: LiveData<LatLng>
        get() = _location
    private val _newLocation = MutableLiveData(false)
    val newLocation: LiveData<Boolean> get() = _newLocation
    private val app = application
    private val resources = app.resources

    var reminderDTO = ReminderDTO()

    init {
        val lat = resources.getString(R.string.default_latitude).toDoubleOrNull() ?: 0.0
        val lon = resources.getString(R.string.default_longitude).toDoubleOrNull() ?: 0.0
        _location.value = LatLng(lat, lon)
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
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        _location.value = LatLng(it.latitude, it.longitude)
                        _newLocation.value = true
                    }
                }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(0)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        _location.value = LatLng(location.latitude, location.longitude)
                        _newLocation.value = true
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    fun newLocationObserved() {
        _newLocation.value = false
    }
}