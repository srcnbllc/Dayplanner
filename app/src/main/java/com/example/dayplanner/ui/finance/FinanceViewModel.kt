package com.example.dayplanner.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dayplanner.finance.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)

class FinanceViewModel(
    private val repo: FinanceRepository
) : ViewModel() {

    private val _filter = MutableStateFlow("MONTHLY") // date range identifier (DAILY/WEEKLY/MONTHLY/YEARLY/ALL)
    private val _typeFilter = MutableStateFlow("ALL") // ALL/INCOME/EXPENSE or category
    private val _baseCurrency = MutableStateFlow("TRY")

    // Compose streams: transactions, totals, category breakdowns, debts, insights
    val dateRangeFlow: Flow<Pair<Long, Long>> = _filter.map { getDateRange(it) }

    val transactions: StateFlow<List<Transaction>> =
        combine(_typeFilter, dateRangeFlow) { f, range ->
            Pair(f, range)
        }.flatMapLatest { (filter, range) ->
            repo.transactionsFlow(filter, range)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categoryTotals: StateFlow<List<CategoryTotal>> =
        combine(_filter, _baseCurrency) { rangeStr, base ->
            Pair(rangeStr, base)
        }.flatMapLatest { (rangeStr, base) ->
            val range = getDateRange(rangeStr)
            repo.categoryTotalsFlow(TransactionType.EXPENSE, range)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totals: StateFlow<FinancialTotals> =
        combine(_filter, _baseCurrency) { rangeStr, base ->
            Pair(rangeStr, base)
        }.flatMapLatest { (rangeStr, base) ->
            flow {
                val range = getDateRange(rangeStr)
                emit(repo.computeTotals(range, base))
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, FinancialTotals(0.0,0.0,0.0,0.0))
    
    // Performance optimization: Debounce filter changes
    private val _debouncedFilter = _filter.debounce(300)
    private val _debouncedTypeFilter = _typeFilter.debounce(300)
    
    val optimizedTransactions: StateFlow<List<Transaction>> =
        combine(_debouncedTypeFilter, dateRangeFlow) { f, range ->
            Pair(f, range)
        }.flatMapLatest { (filter, range) ->
            repo.transactionsFlow(filter, range)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val debts: StateFlow<List<Debt>> =
        repo.activeDebts().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Simple insight engine: compute a few rules
    val insights: StateFlow<List<String>> = totals.map { t ->
        val out = mutableListOf<String>()
        if (t.income <= 0 && t.expense > 0) {
            out += "Gelir yok fakat gider mevcut — bütçeni gözden geçir."
        } else {
            val ratio = if (t.income > 0) (t.expense / t.income) else 1.0
            if (ratio >= 1.0) out += "Harcamaların gelirlerini aşıyor!"
            else if (ratio >= 0.8) out += "Harcamalar gelirlerin %80'inden fazla — dikkat."
            else out += "Güzel: gelir-gider dengesi olumlu."
        }
        out
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Public UI actions
    fun setFilter(filter: String) { _filter.value = filter }
    fun setTypeFilter(type: String) { _typeFilter.value = type }
    fun setBaseCurrency(currency: String) { _baseCurrency.value = currency }

    fun addTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.addTransaction(tx)
        }
    }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.updateTransaction(tx)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repo.deleteTransaction(id)
        }
    }

    fun repayDebt(debtId: Long, amount: Double) {
        viewModelScope.launch {
            // Very simple: load debt from repo via debts state and apply change
            val current = debts.value.find { it.id == debtId } ?: return@launch
            val newAmount = (current.amount - amount).coerceAtLeast(0.0)
            val updated = current.copy(amount = newAmount, isPaid = newAmount <= 0.0)
            // Note: This should be handled by repository, not direct DAO access
            // repo.updateDebt(updated)
        }
    }

    private fun getDateRange(range: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        when (range) {
            "DAILY" -> {
                calendar.set(Calendar.HOUR_OF_DAY,0); calendar.set(Calendar.MINUTE,0); calendar.set(Calendar.SECOND,0); calendar.set(Calendar.MILLISECOND,0)
                return Pair(calendar.timeInMillis, endTime)
            }
            "WEEKLY" -> { calendar.add(Calendar.WEEK_OF_YEAR,-1); return Pair(calendar.timeInMillis, endTime) }
            "MONTHLY" -> { calendar.add(Calendar.MONTH,-1); return Pair(calendar.timeInMillis, endTime) }
            "YEARLY" -> { calendar.add(Calendar.YEAR,-1); return Pair(calendar.timeInMillis, endTime) }
            else -> return Pair(0L, endTime)
        }
    }
}