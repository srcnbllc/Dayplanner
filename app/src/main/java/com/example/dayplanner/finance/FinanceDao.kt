package com.example.dayplanner.finance

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FinanceDao {
    // Original methods for FinanceRecord
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FinanceRecord): Long

    @Update
    suspend fun updateRecord(record: FinanceRecord)

    @Delete
    suspend fun deleteRecord(record: FinanceRecord)

    @Query("SELECT * FROM finance_record ORDER BY id DESC")
    fun getAllRecords(): LiveData<List<FinanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallment(installment: Installment): Long

    @Update
    suspend fun updateInstallment(installment: Installment)

    @Query("SELECT * FROM installment WHERE financeRecordId = :recordId ORDER BY dueDate ASC")
    fun getInstallments(recordId: Int): LiveData<List<Installment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: PaymentReminder): Long

    @Query("SELECT * FROM payment_reminder WHERE financeRecordId = :recordId")
    fun getReminders(recordId: Int): LiveData<List<PaymentReminder>>

    // New methods for Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    suspend fun getTransactionsByCategory(category: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    suspend fun getTransactionsByType(type: TransactionType): List<Transaction>

    // Category methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT name FROM categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<String>

    // Synchronous methods for export/import
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<Transaction>
}


