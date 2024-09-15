package com.example.locationremainder.ui.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.locationremainder.R
import com.example.locationremainder.data.FakeAndroidTestRepository
import com.example.locationremainder.utils.UIRenderIdlingResource
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class RemindersListFragmentWithoutPermissionsTest {
    private val idlingResource = UIRenderIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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

    @Before
    fun setUpIdlingResource() {
        IdlingRegistry.getInstance().register(idlingResource)
        idlingResource.setIdleState(false)
    }

    @After
    fun cleanUpIdlingResource() {
        idlingResource.setIdleState(true)
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun noLocationPermission_showsPermissionSnackbar() = runTest {
        // GIVEN

        // WHEN
        launchFragmentInContainer<RemindersListFragment>(null, R.style.Theme_LocationRemainder)
        idlingResource.setIdleState(true)

        // THEN
        onView(withText(R.string.location_request_info))
            .check(matches(isDisplayed()))
    }
}