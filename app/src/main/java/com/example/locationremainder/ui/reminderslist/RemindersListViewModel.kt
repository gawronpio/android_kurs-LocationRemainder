package com.example.locationremainder.ui.reminderslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.locationremainder.data.ReminderDataSource
import com.example.locationremainder.data.dto.ReminderDTO
import kotlinx.coroutines.launch
import com.example.locationremainder.data.dto.Result

private const val TAG = "RemindersListViewModel"

class RemindersListViewModel(
    private val remindersLocalRepository: ReminderDataSource,
    application: Application
) : AndroidViewModel(application) {
    var reminders = MutableLiveData<List<ReminderDTO>?>()
    private val _newReminderDTO = MutableLiveData<ReminderDTO?>(null)
    val newReminderDTO: LiveData<ReminderDTO?> get() = _newReminderDTO
    private val _showLoading = MutableLiveData(false)
    val showLoading: LiveData<Boolean> get() = _showLoading

    fun refresh() {
        _showLoading.value = true
        viewModelScope.launch {
            val result = remindersLocalRepository.getReminders()
            if(result is Result.Success) {
                reminders.value = result.data
            }
            _showLoading.postValue(false)
        }
    }

    fun saveNewReminderAndRefresh(reminderDTO: ReminderDTO){
        viewModelScope.launch {
            val newId = remindersLocalRepository.saveReminder(reminderDTO)
            reminderDTO.id = newId
            _newReminderDTO.value = reminderDTO
            refresh()
        }
    }

    fun deleteNewReminder() {
        _newReminderDTO.value = null
    }
}