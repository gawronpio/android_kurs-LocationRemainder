package com.example.locationremainder.ui.reminderslist

import android.Manifest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.locationremainder.R
import com.example.locationremainder.data.FakeAndroidTestRepository
import com.example.locationremainder.data.dto.ReminderDTO
import com.example.locationremainder.utils.PermissionRuleHelper
import com.example.locationremainder.utils.UIRenderIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class RemindersListFragmentTest : AutoCloseKoinTest() {
    private val idlingResource = UIRenderIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val permissionHelper = PermissionRuleHelper(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val permissionRule: TestRule = permissionHelper

    private lateinit var repository: FakeAndroidTestRepository
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setup() {
        stopKoin()
        repository = FakeAndroidTestRepository()
        viewModel = RemindersListViewModel(repository, ApplicationProvider.getApplicationContext())

        val myModule = module {
            viewModel {
                viewModel
            }
        }

        startKoin {
            modules(listOf(myModule))
        }
    }

    private fun setUpIdlingResource() {
        IdlingRegistry.getInstance().register(idlingResource)
        idlingResource.setIdleState(false)
    }

    private fun cleanUpIdlingResource() {
        idlingResource.setIdleState(true)
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun remindersListEmpty_showNoData() = runTest {
        // GIVEN
        setUpIdlingResource()

        // WHEN
        launchFragmentInContainer<RemindersListFragment>(null, R.style.Theme_LocationRemainder)
        idlingResource.setIdleState(true)

        // THEN
        onView(withId(R.id.reminders_recycler)).check(matches(not(isDisplayed())))
        onView(withId(R.id.no_data_text)).check(matches(isDisplayed()))

        cleanUpIdlingResource()
    }

    @Test
    fun remindersListNotEmpty_showReminders() = runTest {
        // GIVEN
        setUpIdlingResource()
        val reminder = ReminderDTO(1L, "Title", "Description", 20.0, 30.0, 10.0, "Location")
        repository.addReminders(reminder)

        // WHEN
        launchFragmentInContainer<RemindersListFragment>(null, R.style.Theme_LocationRemainder)
        idlingResource.setIdleState(true)

        // THEN
        onView(withId(R.id.reminders_recycler)).check(matches(isDisplayed()))
        onView(withId(R.id.no_data_text)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        cleanUpIdlingResource()
    }

    @Test
    fun clickAddReminderButton_navigateToMapFragment() = runTest {
        // GIVEN
        val scenario = launchFragmentInContainer<RemindersListFragment>(null, R.style.Theme_LocationRemainder)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        // WHEN
        onView(withId(R.id.add_btn)).perform(click())
        idlingResource.setIdleState(true)

        // THEN
        verify(navController).navigate(
            RemindersListFragmentDirections.actionMainFragmentToMapFragment()
        )
    }
}