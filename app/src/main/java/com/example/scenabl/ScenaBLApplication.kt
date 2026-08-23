package com.example.scenabl

import android.app.Application
import com.example.scenabl.di.AppContainer

class ScenaBLApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}
