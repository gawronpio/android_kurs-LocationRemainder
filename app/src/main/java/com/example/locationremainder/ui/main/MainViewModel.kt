package com.example.locationremainder.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationremainder.data.Poi
import com.example.locationremainder.data.PoiDao
import kotlinx.coroutines.launch

const val TAG = "MainViewModel"

class MainViewModelFactory(
    private val poiDao: PoiDao,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(poiDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel(private val poiDao: PoiDao, application: Application) : AndroidViewModel(application) {
    var pois = MutableLiveData<List<Poi>?>(null)
    private val _newPoi = MutableLiveData<Poi?>(null)
    val newPoi: LiveData<Poi?> get() = _newPoi

    fun refresh() {
        viewModelScope.launch { pois.value =  poiDao.getAll() }
    }

    fun saveNewPoiAndRefresh(newPoi: Poi){
        var newId: Long? = null
        viewModelScope.launch {
            if (newPoi.id == null) {
                newId = poiDao.insert(newPoi)
                newPoi.id = newId
                _newPoi.value = newPoi
            } else {
                poiDao.update(newPoi)
            }
            pois.value =  poiDao.getAll()
        }
    }

    fun createGeofence() {
        viewModelScope.launch {

            _newPoi.value = null
        }
    }
}