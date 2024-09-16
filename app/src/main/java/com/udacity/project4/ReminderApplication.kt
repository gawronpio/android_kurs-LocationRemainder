package com.udacity.project4

import android.app.Application
import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import com.udacity.project4.ui.reminderslist.RemindersListViewModel
import com.udacity.project4.ui.map.MapViewModel
import com.udacity.project4.ui.detail.DetailViewModel

class ReminderApplication: Application() {
    override fun onCreate() {
        super.onCreate()


        val reminderModule = module {
            viewModel {
                RemindersListViewModel(
                    get() as ReminderDataSource,
                    get()
                )
            }
            viewModel {
                MapViewModel(
                    get()
                )
            }
            viewModel {
                DetailViewModel(
                    get()
                )
            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@ReminderApplication) }
        }

        startKoin {
            androidContext(this@ReminderApplication)
            modules(listOf(reminderModule))
        }
    }
}