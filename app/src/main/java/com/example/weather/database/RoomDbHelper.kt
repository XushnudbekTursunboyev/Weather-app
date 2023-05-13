package com.example.weather.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weather.model.WeatherModel

@Database(
    entities = [Entity::class],
    version = 1,
    exportSchema = true
)

abstract class RoomDbHelper : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

    object DatabaseBuilder {

        private var instance: RoomDbHelper? = null

        fun getInstance(context: Context): RoomDbHelper {
            if (instance == null) {
                synchronized(this) {
                    instance = buildRoomDb(context)
                }
            }
            return instance!!
        }

        private fun buildRoomDb(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                RoomDbHelper::class.java,
                "room.weather.db"
            )
                .allowMainThreadQueries()
                .build()

    }
}