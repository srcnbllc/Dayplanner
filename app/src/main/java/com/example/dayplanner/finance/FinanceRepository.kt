package com.example.dayplanner.finance

import kotlinx.coroutines.flow.*
import java.util.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData

class FinanceRepository(
    private val dao: FinanceDao,
    private val currencyConverter: CurrencyConverter
) {
    // Return transactions filtered/combined by UI selection
    fun transactionsFlow(filter: String, dateRange: Pair<Long, Long>): Flow<List<Transaction>> {
        return when (filter) {
            "ALL" -> dao.getTransactionsByDateRange(dateRange.first, dateRange.second)
            "INCOME" -> dao.getTransactionsByType(TransactionType.INCOME)
            "EXPENSE" -> dao.getTransactionsByType(TransactionType.EXPENSE)
            else -> dao.getTransactionsByDateRange(dateRange.first, dateRange.second)
                .map { list -> list.filter { it.category == filter } }
        }
    }

    suspend fun addTransaction(tx: Transaction) {
        dao.insertTransaction(tx)
    }

    suspend fun updateTransaction(tx: Transaction) {
        dao.updateTransaction(tx)
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteTransaction(id)
    }

    suspend fun getAllTransactionsSync(): List<Transaction> {
        return dao.getAllTransactionsSync()
    }

    fun categoryTotalsFlow(type: TransactionType, range: Pair<Long, Long>): Flow<List<CategoryTotal>> {
        return dao.getCategoryTotals(type, range.first, range.second)
    }

    fun totalsGroupedByCurrency(range: Pair<Long, Long>): Flow<List<CurrencySum>> {
        return dao.getTotalsGroupedByCurrency(range.first, range.second)
    }

    fun activeDebts(): Flow<List<Debt>> = dao.getActiveDebts()

    // Yeni yardımcılar — repository dışına çıkarmaya gerek yoksa class içinde kullan
    fun transactionsLiveData(filter: String, dateRange: Pair<Long, Long>): LiveData<List<Transaction>> {
        return transactionsFlow(filter, dateRange).asLiveData()
    }

    fun categoryTotalsLiveData(type: TransactionType, range: Pair<Long, Long>): LiveData<List<CategoryTotal>> {
        return dao.getCategoryTotals(type, range.first, range.second).asLiveData()
    }

    fun totalsGroupedByCurrencyLiveData(range: Pair<Long, Long>): LiveData<List<CurrencySum>> {
        return dao.getTotalsGroupedByCurrency(range.first, range.second).asLiveData()
    }

    fun activeDebtsLiveData(): LiveData<List<Debt>> = dao.getActiveDebts().asLiveData()

    // Convert aggregated sums into a single base currency (suspend because conversion may require IO)
    suspend fun convertSumsToBase(sums: List<CurrencySum>, base: String): Map<String, Double> {
        val rates = currencyConverter.getRates(base) // returns map like {"USD":1.0,"TRY":27.0}
        val map = mutableMapOf<String, Double>()
        for (s in sums) {
            val rate = rates[s.currency] ?: 1.0
            val converted = s.total * rate
            map[s.currency] = converted
        }
        return map
    }

    // Simple insight computations
    suspend fun computeTotals(range: Pair<Long, Long>, base: String): FinancialTotals {
        val income = dao.getTotalOfTypeInRange(TransactionType.INCOME, range.first, range.second) ?: 0.0
        val expense = dao.getTotalOfTypeInRange(TransactionType.EXPENSE, range.first, range.second) ?: 0.0

        // Note: amounts currently mixed currencies; for accuracy use getTotalsGroupedByCurrency + convert
        val sums = dao.getTotalsGroupedByCurrency(range.first, range.second).firstOrNull() ?: emptyList()
        val converted = convertSumsToBase(sums, base)
        val totalConverted = converted.values.sum()
        val balance = income - expense // coarse; prefer converted total if cross-currency used

        return FinancialTotals(income, expense, balance, totalConverted)
    }
}

data class FinancialTotals(
    val income: Double,
    val expense: Double,
    val balance: Double,
    val totalConvertedToBase: Double
)


