package com.example.locationremainder.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.locationremainder.data.dto.ReminderDTO
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReminderDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runTest {
        // GIVEN
        val reminder = ReminderDTO(null, "title", "description", 0.0, 0.0, 1.0, "location")

        // WHEN
        val reminderId = database.reminderDao().saveReminder(reminder)
        val loaded = database.reminderDao().getReminderById(reminderId)

        // THEN
        assertNotEquals(null, loaded as ReminderDTO)
        assertEquals(reminderId, loaded.id)
        assertEquals(reminder.title, loaded.title)
        assertEquals(reminder.description, loaded.description)
        assertEquals(reminder.latitude, loaded.latitude, 0.000001)
        assertEquals(reminder.longitude, loaded.longitude, 0.000001)
        assertEquals(reminder.radius!!, loaded.radius!!, 0.1)
        assertEquals(reminder.location, loaded.location)
    }

    @Test
    fun updateReminderAndGetById() = runTest {
        // GIVEN
        val reminder = ReminderDTO(null, "title", "description", 0.0, 0.0, 1.0, "location")
        val reminderId = database.reminderDao().saveReminder(reminder)

        // WHEN
        reminder.id = reminderId
        reminder.latitude = 20.034244
        reminder.longitude = 50.324343
        reminder.radius = 150.0
        database.reminderDao().saveReminder(reminder)
        val loaded = database.reminderDao().getReminderById(reminderId)

        // THEN
        assertNotEquals(null, loaded as ReminderDTO)
        assertEquals(reminderId, loaded.id)
        assertEquals(reminder.title, loaded.title)
        assertEquals(reminder.description, loaded.description)
        assertEquals(reminder.latitude, loaded.latitude, 0.000001)
        assertEquals(reminder.longitude, loaded.longitude, 0.000001)
        assertEquals(reminder.radius!!, loaded.radius!!, 0.1)
        assertEquals(reminder.location, loaded.location)
    }

    @Test
    fun getReminderByWrongId() = runTest {
        // GIVEN
        val reminder = ReminderDTO(null, "title", "description", 0.0, 0.0, 1.0, "location")
        val reminderId = database.reminderDao().saveReminder(reminder)
        val wrongId = 100L
        assert(reminderId != wrongId)

        // WHEN
        val loaded = database.reminderDao().getReminderById(wrongId)

        // THEN
        assertEquals(null, loaded)
    }
}