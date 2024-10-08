package com.udacity.project4.data.local

import android.content.Context
import androidx.room.Room

/**
 * Singleton class that is used to create a reminder db
 */
object LocalDB {

    /**
     * Static method that creates a reminder class and returns the DAO of the reminder
     */
    fun createRemindersDao(context: Context): RemindersDao {
        return Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        )
            .fallbackToDestructiveMigration()
            .build()
            .reminderDao()
    }
}