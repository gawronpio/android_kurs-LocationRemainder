package com.example.locationremainder.ui.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.locationremainder.R
import com.example.locationremainder.data.dto.ReminderDTO

const val TAG = "DetailViewModel"

class DetailViewModel(
    application: Application
) : ViewModel() {
    private val app = application

    var id: Long? = null
    var title = MutableLiveData("")
    var description = MutableLiveData("")
    var radius = MutableLiveData(0.0f)
    var latitude = 0.0
    var longitude = 0.0
    var location = ""

    fun setvariables(reminderDTO: ReminderDTO?) {
        id = reminderDTO?.id
        title.value = reminderDTO?.title ?: ""
        description.value = reminderDTO?.description ?: ""
        radius.value = reminderDTO?.radius?.toFloat() ?: app.resources.getString(R.string.default_radius).toFloat()
        latitude = reminderDTO?.latitude ?: 0.0
        longitude = reminderDTO?.longitude ?: 0.0
        location = reminderDTO?.location ?: ""
    }

    fun getPoi(): ReminderDTO {
        return ReminderDTO(
            id = id,
            title = title.value,
            description = description.value,
            radius = radius.value?.toDouble(),
            latitude = latitude,
            longitude = longitude,
            location = location
        )
    }
}