package com.example.locationremainder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.locationremainder.data.dto.ReminderDTO

@Database(entities = [ReminderDTO::class], version = 1)
abstract class RemindersDatabase : RoomDatabase() {
    abstract fun reminderDao(): RemindersDao
}