package com.udacity.project4.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.udacity.project4.data.dto.ReminderDTO

@Database(entities = [ReminderDTO::class], version = 1)
abstract class RemindersDatabase : RoomDatabase() {
    abstract fun reminderDao(): RemindersDao
}