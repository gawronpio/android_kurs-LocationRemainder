package com.example.locationremainder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PoiDao {
    @Insert
    suspend fun insert(poi: Poi)

    @Update
    suspend fun update(poi: Poi)

    @Query("SELECT * from poi_table ORDER BY id")
    suspend fun getAll(): List<Poi>

    @Query("SELECT * from poi_table WHERE id = :key")
    suspend fun get(key: Long): Poi?
}