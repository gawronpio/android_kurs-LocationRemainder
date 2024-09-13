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
    var reminders = MutableLiveData<List<ReminderDTO>?>(null)
    private val _newReminderDTO = MutableLiveData<ReminderDTO?>(null)
    val newReminderDTO: LiveData<ReminderDTO?> get() = _newReminderDTO

    fun refresh() {
        viewModelScope.launch {
            val result = remindersLocalRepository.getReminders()
            if(result is Result.Success) {
                reminders.value = result.data
            }
        }
    }

    fun saveNewReminderAndRefresh(reminderDTO: ReminderDTO){
        viewModelScope.launch {
            val newId = remindersLocalRepository.saveReminder(reminderDTO)
            reminderDTO.id = newId
            _newReminderDTO.value = reminderDTO
            val result = remindersLocalRepository.getReminders()
            if(result is Result.Success) {
                reminders.value = result.data
            }
        }
    }

    fun deleteNewReminder() {
        _newReminderDTO.value = null
    }
}