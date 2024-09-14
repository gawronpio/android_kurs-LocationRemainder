package com.example.locationremainder.ui.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.locationremainder.MainCoroutineRule
import com.example.locationremainder.data.FakeTestRepository
import com.example.locationremainder.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.jupiter.api.Assertions.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RemindersListViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var remindersLocalRepository: FakeTestRepository

    @Mock
    private lateinit var reminderObserver: Observer<List<ReminderDTO>?>

    @Mock
    private lateinit var loadingObserver: Observer<Boolean>

    @Before
    fun setUp() {
        remindersLocalRepository = FakeTestRepository()
        remindersListViewModel = RemindersListViewModel(remindersLocalRepository, Application())
        remindersListViewModel.reminders.observeForever(reminderObserver)
        remindersListViewModel.showLoading.observeForever(loadingObserver)
    }

    @Test
    fun refresh_withSuccessResult_updatesReminders() = runTest {
        // GIVEN
        val reminder1 = ReminderDTO(1L, "title1", "desc1", 10.0, 20.0, 100.0, "loc1")
        val reminder2 = ReminderDTO(2L, "title2", "desc2", 20.0, 30.0, 200.0, "loc2")
        val reminder3 = ReminderDTO(3L, "title3", "desc3", 30.0, -20.0, 10.0, "loc3")
        remindersLocalRepository.addReminders(
            reminder1,
            reminder2,
            reminder3,
        )

        // WHEN
        remindersListViewModel.refresh()
        advanceUntilIdle()

        // THEN
        verify(reminderObserver).onChanged(listOf(reminder1, reminder2, reminder3))
    }

    @Test
    fun refresh_withErrorResult_doesNotUpdateReminders() = runTest {
        // GIVEN
        val reminder1 = ReminderDTO(1L, "title1", "desc1", 10.0, 20.0, 100.0, "loc1")
        val reminder2 = ReminderDTO(2L, "title2", "desc2", 20.0, 30.0, 200.0, "loc2")
        val reminder3 = ReminderDTO(3L, "title3", "desc3", 30.0, -20.0, 10.0, "loc3")
        remindersLocalRepository.addReminders(
            reminder1,
            reminder2,
            reminder3,
        )
        remindersLocalRepository.setReturnError(true)

        // WHEN
        remindersListViewModel.refresh()
        advanceUntilIdle()

        // THEN
        verify(reminderObserver, never()).onChanged(any())
    }

    @Test
    fun saveNewReminderAndRefresh_withSuccessResult_updatesReminders() = runTest {
        // GIVEN
        val reminder1 = ReminderDTO(1L, "title1", "desc1", 10.0, 20.0, 100.0, "loc1")
        val reminder2 = ReminderDTO(2L, "title2", "desc2", 20.0, 30.0, 200.0, "loc2")
        val reminder3 = ReminderDTO(3L, "title3", "desc3", 30.0, -20.0, 10.0, "loc3")
        remindersLocalRepository.addReminders(
            reminder1,
            reminder2,
            reminder3,
        )
        val newReminder = ReminderDTO(null, "title4", "desc4", 5.0, 6.0, 7.0, "loc4")

        // WHEN
        remindersListViewModel.saveNewReminderAndRefresh(newReminder)
        advanceUntilIdle()

        // THEN
        verify(reminderObserver).onChanged(listOf(reminder1, reminder2, reminder3, newReminder))
    }

    @Test
    fun deleteNewReminder_setsNewReminderDTOToNull() = runTest {
        // GIVEN
        val newReminder = ReminderDTO(null, "title4", "desc4", 5.0, 6.0, 7.0, "loc4")

        // THEN
        remindersListViewModel.saveNewReminderAndRefresh(newReminder)
        advanceUntilIdle()
        remindersListViewModel.deleteNewReminder()
        advanceUntilIdle()

        // WHEN
        assertEquals(null, remindersListViewModel.newReminderDTO.value)
    }

    @Test
    fun refresh_checkLoading() = runTest {
        // GIVEN
        remindersLocalRepository.addReminders(
            ReminderDTO(1L, "title1", "desc1", 10.0, 20.0, 100.0, "loc1"))

        // WHEN
        remindersListViewModel.refresh()
        advanceUntilIdle()

        // THEN
        val inOrder = inOrder(loadingObserver)
        inOrder.verify(loadingObserver).onChanged(true)
        inOrder.verify(loadingObserver).onChanged(false)
    }
}