package com.example.locationremainder.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.locationremainder.R
import com.example.locationremainder.data.Poi
import com.example.locationremainder.data.PoiDao

const val TAG = "DetailViewModel"

class DetailViewModelFactory(
    private val poiDao: PoiDao,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(poiDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DetailViewModel(private val poiDao: PoiDao, application: Application) : ViewModel() {
    private val app = application

    var id: Long? = null
    var title = MutableLiveData<String>("")
    var description = MutableLiveData<String>("")
    var radius = MutableLiveData<Float>(0.0f)
    var latitude = 0.0
    var longitude = 0.0
    var location = ""

    fun setvariables(poi: Poi?) {
        id = poi?.id
        title.value = poi?.title ?: ""
        description.value = poi?.description ?: ""
        radius.value = poi?.radius?.toFloat() ?: app.resources.getString(R.string.default_radius).toFloat()
        latitude = poi?.latitude ?: 0.0
        longitude = poi?.longitude ?: 0.0
        location = poi?.location ?: ""
    }

    fun getPoi(): Poi {
        return Poi(
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