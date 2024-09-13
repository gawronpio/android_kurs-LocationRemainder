package com.example.locationremainder.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.locationremainder.MainActivity.Companion.ACTION_GEOFENCE_EVENT
import com.example.locationremainder.R
import com.example.locationremainder.data.PoiDao
import com.example.locationremainder.data.PoiDatabase
import com.example.locationremainder.utils.sendNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "GeofenceReceiver"

class GeofenceBroadcastReceiver: BroadcastReceiver() {
    private var poiDao: PoiDao? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent == null) {
                Log.e(TAG, "GeofencingEvent is null")
                return
            }

            if(geofencingEvent.hasError()) {
                val errorMessage = errorMessage(context!!, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if(geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                if (!triggeringGeofences.isNullOrEmpty()) {
                    for (geofence in triggeringGeofences) {
                        val fenceId = geofence.requestId.toLong()
                        val title = context!!.resources.getString(R.string.notification_channel_name)
                        poiDao = PoiDatabase.getInstance(context).poiDatabaseDao
                        CoroutineScope(Dispatchers.IO).launch {
                            val poi = poiDao!!.get(fenceId)
                            if(poi == null) {
                                Log.e(TAG, "No POI found with fence ID: $fenceId")
                                return@launch
                            }
                            val messageBody = String.format(
                                context.resources.getString(R.string.message_body_template),
                                poi.location,
                                poi.title
                            )
                            val notificationManager = ContextCompat.getSystemService(
                                context,
                                NotificationManager::class.java
                            ) as NotificationManager
                            notificationManager.sendNotification(poi.id!!, title, messageBody, context)
                        }
                    }
                }
            }
        }
    }
}