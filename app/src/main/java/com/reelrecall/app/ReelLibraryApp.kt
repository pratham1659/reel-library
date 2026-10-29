package com.reelrecall.app

import android.app.Application
import com.reelrecall.app.data.local.AppDatabase
import com.reelrecall.app.data.repository.PreferencesRepository

class ReelLibraryApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        preferencesRepository = PreferencesRepository(this)
    }
}
