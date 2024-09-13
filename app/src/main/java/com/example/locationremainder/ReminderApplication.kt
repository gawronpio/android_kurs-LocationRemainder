package com.example.locationremainder

import android.app.Application
import com.example.locationremainder.data.ReminderDataSource
import com.example.locationremainder.data.local.LocalDB
import com.example.locationremainder.data.local.RemindersLocalRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import com.example.locationremainder.ui.reminderslist.RemindersListViewModel
import com.example.locationremainder.ui.map.MapViewModel
import com.example.locationremainder.ui.detail.DetailViewModel

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