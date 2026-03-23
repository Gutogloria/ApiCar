package com.example.apicar.database.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.apicar.database.UserLocation
import com.example.meuprimeiroapp.database.converters.DateConverters
import com.example.meuprimeiroapp.database.dao.UserLocationDao

@Database(entities = [UserLocation::class], version = 1, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userLocationDao(): UserLocationDao
}