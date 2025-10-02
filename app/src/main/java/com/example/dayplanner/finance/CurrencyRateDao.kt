package com.example.dayplanner.finance

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyRateDao {
    @Query("SELECT * FROM currency_rates WHERE base = :base AND currency = :currency ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRate(base: String, currency: String): CurrencyRate?

    @Query("SELECT * FROM currency_rates WHERE base = :base ORDER BY timestamp DESC")
    fun getRatesByBase(base: String): Flow<List<CurrencyRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: CurrencyRate)

    @Update
    suspend fun updateRate(rate: CurrencyRate)

    @Delete
    suspend fun deleteRate(rate: CurrencyRate)

    @Query("DELETE FROM currency_rates WHERE timestamp < :cutoffTime")
    suspend fun deleteOldRates(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM currency_rates")
    suspend fun getRateCount(): Int
}