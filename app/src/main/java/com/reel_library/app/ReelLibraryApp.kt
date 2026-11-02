package com.reel_library.app

import android.app.Application
import com.reel_library.app.data.local.AppDatabase
import com.reel_library.app.data.repository.PreferencesRepository
import com.reel_library.app.worker.DailyCloudBackupWorker

class ReelLibraryApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        preferencesRepository = PreferencesRepository(this)
        DailyCloudBackupWorker.scheduleDailyBackup(this)
    }
}
