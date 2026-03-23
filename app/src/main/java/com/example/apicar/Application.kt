package com.example.apicar.ui

import android.app.Application
import com.example.apicar.database.model.DatabaseBuilder

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseBuilder.getInstance(this)
    }
}