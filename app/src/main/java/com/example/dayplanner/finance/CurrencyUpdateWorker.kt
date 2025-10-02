package com.example.dayplanner.finance

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dayplanner.NoteDatabase

class CurrencyUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val converter = CurrencyConverter()
            val ratesMap = converter.getRates("TRY")

            val dao = NoteDatabase.getDatabase(applicationContext).currencyRateDao()
            val rateList = ratesMap.map { (currency, value) ->
                CurrencyRate(base = "TRY", currency = currency, rate = value)
            }
            // Clear old rates and insert new ones
            dao.deleteOldRates(System.currentTimeMillis() - (24 * 60 * 60 * 1000)) // Delete rates older than 24 hours
            rateList.forEach { rate ->
                dao.insertRate(rate)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}


