package com.udacity.project4.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import com.udacity.project4.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.udacity.project4.data.dto.Result

class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun initializeDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        localDataSource = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveReminder_getReminder() = runTest {
        // GIVEN
        val newReminder = ReminderDTO(null, "title", "description", 0.0, 0.0, 1.0, "location")

        // WHEN
        val reminderId = localDataSource.saveReminder(newReminder)
        val result = localDataSource.getReminder(reminderId)

        // THEN
        assertTrue(result is Result.Success)
        result as Result.Success
        assertEquals(reminderId, result.data.id)
        assertEquals(newReminder.title, result.data.title)
        assertEquals(newReminder.description, result.data.description)
        assertEquals(newReminder.latitude, result.data.latitude, 0.000001)
        assertEquals(newReminder.longitude, result.data.longitude, 0.000001)
        assertEquals(newReminder.radius!!, result.data.radius!!, 0.1)
        assertEquals(newReminder.location, result.data.location)
    }

    @Test
    fun updateReminderAndGetById() = runTest {
        // GIVEN
        val reminder = ReminderDTO(null, "title", "description", 0.0, 0.0, 1.0, "location")
        val reminderId = localDataSource.saveReminder(reminder)

        // WHEN
        reminder.id = reminderId
        reminder.latitude = 20.034244
        reminder.longitude = 50.324343
        reminder.radius = 150.0
        localDataSource.saveReminder(reminder)
        val result = localDataSource.getReminder(reminderId)

        // THEN
        assertTrue(result is Result.Success)
        result as Result.Success
        assertEquals(reminderId, result.data.id)
        assertEquals(reminder.title, result.data.title)
        assertEquals(reminder.description, result.data.description)
        assertEquals(reminder.latitude, result.data.latitude, 0.000001)
        assertEquals(reminder.longitude, result.data.longitude, 0.000001)
        assertEquals(reminder.radius!!, result.data.radius!!, 0.1)
        assertEquals(reminder.location, result.data.location)
    }

    @Test
    fun getReminderByWrongId() = runTest {
        // GIVEN
        val reminder = ReminderDTO(null, "title", "description", 0.0, 0.0, 1.0, "location")
        val reminderId = localDataSource.saveReminder(reminder)
        val wrongId = 100L
        assert(reminderId != wrongId)

        // WHEN
        val result = localDataSource.getReminder(wrongId)

        // THEN
        assertFalse(result is Result.Success)
        assertTrue(result is Result.Error)
    }
}