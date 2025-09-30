package com.example.dayplanner.finance

import androidx.lifecycle.LiveData
import androidx.room.*

data class CategoryTotal(
    val category: String,
    val total: Double
)

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

    // Transaction methods with filtering
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE subtype = :subtype ORDER BY date DESC")
    fun getTransactionsBySubtype(subtype: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 ORDER BY date DESC")
    fun getRecurringTransactions(): LiveData<List<Transaction>>

    // Summary queries for dashboard
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalIncome(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpense(startDate: Long, endDate: Long): Double?

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate GROUP BY category ORDER BY total DESC")
    suspend fun getCategoryTotals(type: TransactionType, startDate: Long, endDate: Long): List<CategoryTotal>

    // Category methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT name FROM categories ORDER BY name ASC")
    suspend fun getAllCategoryNames(): List<String>

    // Transaction CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // Synchronous methods for export/import
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<Transaction>
}


