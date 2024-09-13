package com.example.locationremainder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.locationremainder.data.dto.ReminderDTO

@Dao
interface RemindersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReminder(reminderDTO: ReminderDTO): Long

    @Query("SELECT * from reminders ORDER BY id")
    suspend fun getReminders(): List<ReminderDTO>

    @Query("SELECT * from reminders WHERE id = :key")
    suspend fun getReminderById(key: Long): ReminderDTO?

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}