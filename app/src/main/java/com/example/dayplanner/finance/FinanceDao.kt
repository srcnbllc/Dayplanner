package com.example.dayplanner.finance

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    
    // Transaction operations
    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<Transaction>
    
    // Aggregation queries
    @Query("""
        SELECT category, SUM(amount) as total, currency 
        FROM transactions 
        WHERE type = :type AND date BETWEEN :start AND :end 
        GROUP BY category, currency
    """)
    fun getCategoryTotals(type: TransactionType, start: Long, end: Long): Flow<List<CategoryTotal>>
    
    @Query("""
        SELECT currency, SUM(amount) as total 
        FROM transactions 
        WHERE date BETWEEN :start AND :end 
        GROUP BY currency
    """)
    fun getTotalsGroupedByCurrency(start: Long, end: Long): Flow<List<CurrencySum>>
    
    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE type = :type AND date BETWEEN :start AND :end
    """)
    suspend fun getTotalOfTypeInRange(type: TransactionType, start: Long, end: Long): Double?
    
    // Debt operations
    @Query("SELECT * FROM debts WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getActiveDebts(): Flow<List<Debt>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)
    
    @Update
    suspend fun updateDebt(debt: Debt)
    
    @Delete
    suspend fun deleteDebt(debt: Debt)
    
    // Category operations
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
}