package com.udacity.project4.data

import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result

class FakeTestRepository : ReminderDataSource {
    private var shouldReturnError = false

    private val fakeDatabase: MutableList<ReminderDTO> = mutableListOf()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(shouldReturnError) {
            Result.Error("Test exception")
        } else {
            Result.Success(fakeDatabase.toList())
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO): Long {
        val fakeDatabaseIds = fakeDatabase.map { it.id }
        if(reminder.id != null && reminder.id in fakeDatabaseIds) {
            fakeDatabase.removeIf { it.id == reminder.id }
        } else {
            reminder.id = fakeDatabaseIds.maxByOrNull { it!! }?.plus(1) ?: 0
        }
        fakeDatabase.add(reminder)
        return reminder.id!!.toLong()
    }

    override suspend fun getReminder(id: Long): Result<ReminderDTO> {
        return if(shouldReturnError) {
            Result.Error("Test exception")
        } else {
            val reminder = fakeDatabase.find { it.id == id }
            if(reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found!")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        fakeDatabase.clear()
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for(reminder in reminders) {
            fakeDatabase.add(reminder)
        }
    }
}