package com.example.apicar.database.model

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    private var INSTANCE: com.example.apicar.database.model.AppDatabase? = null

    fun getInstance(context: Context): com.example.apicar.database.model.AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                com.example.apicar.database.model.AppDatabase::class.java,
                "app_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }

    fun getInstance(): com.example.apicar.database.model.AppDatabase {
        return INSTANCE ?: throw IllegalStateException("DatabaseBuilder deve ser inicializado na Application!")
    }
}