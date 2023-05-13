package com.example.weather.database

import androidx.room.PrimaryKey

@androidx.room.Entity(tableName = "weather")
data class Entity(
    @PrimaryKey
    val id:Int,
    val city:String,
    val time:String,
    val condition:String,
    val currentTemp:String,
    val maxTemp:String,
    val minTemp:String,
    val imageUrl:String,
    val hours:String
)