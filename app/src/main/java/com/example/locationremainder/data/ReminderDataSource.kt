package com.example.locationremainder.data

import com.example.locationremainder.data.dto.ReminderDTO
import com.example.locationremainder.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Result<List<ReminderDTO>>
    suspend fun saveReminder(reminder: ReminderDTO): Long
    suspend fun getReminder(id: Long): Result<ReminderDTO>
    suspend fun deleteAllReminders()
}