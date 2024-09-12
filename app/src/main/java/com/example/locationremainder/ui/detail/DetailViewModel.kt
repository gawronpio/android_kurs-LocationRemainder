package com.example.locationremainder.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationremainder.data.Poi
import com.example.locationremainder.data.PoiDao
import kotlinx.coroutines.launch

const val TAG = "DetailViewModel"

class DetailViewModelFactory(
    private val poiDao: PoiDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(poiDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DetailViewModel(private val poiDao: PoiDao) : ViewModel() {
    var poiData: Poi? = null

    fun savePoi() {
        if(poiData == null) {
            Log.e(TAG, "poiData is null")
            return
        }
        poiData?.let { data ->
            viewModelScope.launch {
                if (data.id == null) {
                    poiDao.insert(data)
                } else {
                    poiDao.update(data)
                }
            }
        }
    }
}