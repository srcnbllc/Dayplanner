package com.example.dayplanner.finance

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_record ORDER BY date DESC")
    fun getAllRecords(): LiveData<List<FinanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FinanceRecord)

    @Update
    suspend fun updateRecord(record: FinanceRecord)

    @Delete
    suspend fun deleteRecord(record: FinanceRecord)

    @Query("SELECT * FROM installment WHERE recordId = :recordId ORDER BY dueDate ASC")
    fun getInstallments(recordId: Int): LiveData<List<Installment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallment(installment: Installment)

    @Update
    suspend fun updateInstallment(installment: Installment)

    @Query("SELECT * FROM payment_reminder WHERE recordId = :recordId ORDER BY remindAt ASC")
    fun getReminders(recordId: Int): LiveData<List<PaymentReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: PaymentReminder)
}

