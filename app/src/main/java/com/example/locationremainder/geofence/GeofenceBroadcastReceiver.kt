package com.example.locationremainder.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.locationremainder.MainActivity.Companion.ACTION_GEOFENCE_EVENT
import com.google.android.gms.location.GeofencingEvent
import android.util.Log
import com.google.android.gms.location.Geofence

private const val TAG = "GeofenceReceiver"

class GeofenceBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if(geofencingEvent!!.hasError()) {
                val errorMessage = errorMessage(context!!, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if(geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                if (!triggeringGeofences.isNullOrEmpty()) {
                    for (geofence in triggeringGeofences) {
                        val fenceId = geofence.requestId.toInt()
                        Log.i(TAG, "Geofence triggered with ID: $fenceId")
                        // Tutaj można dodać kod obsługujący aktywację konkretnego geofence
                    }
                }
            }
        }
    }
}