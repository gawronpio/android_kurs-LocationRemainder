package com.example.locationremainder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Poi::class], version = 1)
abstract class PoiDatabase : RoomDatabase() {
    abstract val poiDatabaseDao: PoiDao

    companion object {
        @Volatile
        private var INSTANCE: PoiDatabase? = null

        fun getInstance(context: Context): PoiDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PoiDatabase::class.java,
                        name = "poi_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}