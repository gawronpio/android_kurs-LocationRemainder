package com.example.locationremainder.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationremainder.data.Poi
import com.example.locationremainder.data.PoiDao
import kotlinx.coroutines.launch

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

    fun refresh() {
        viewModelScope.launch { pois.value =  poiDao.getAll() }
    }
}