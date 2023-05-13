package com.example.weather.database

import androidx.room.*

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addCategory(entity: Entity):Long

    @Query("select * from weather order by weather.id")
    fun getList():List<Entity>

    @Update
    fun updateWeather(entity: Entity)
}