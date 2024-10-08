package com.udacity.project4.data

import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Result<List<ReminderDTO>>
    suspend fun saveReminder(reminder: ReminderDTO): Long
    suspend fun getReminder(id: Long): Result<ReminderDTO>
    suspend fun deleteAllReminders()
}