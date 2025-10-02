package com.example.dayplanner

import android.app.Application
import androidx.work.*
import com.example.dayplanner.finance.CurrencyUpdateWorker
import java.util.concurrent.TimeUnit

class DayPlannerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // WorkManager günlük iş planı
        val workRequest = PeriodicWorkRequestBuilder<CurrencyUpdateWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // internet şart
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CurrencyUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}


