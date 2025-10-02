package com.example.dayplanner.ui.finance

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.dayplanner.finance.CategoryTotal
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import android.widget.ArrayAdapter
import java.text.NumberFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var toolbar: MaterialToolbar
    private lateinit var transactionTypeFilter: MaterialAutoCompleteTextView
    private lateinit var paymentTypeFilter: MaterialAutoCompleteTextView
    private lateinit var dateRangeFilter: MaterialAutoCompleteTextView
    private lateinit var viewModel: FinanceViewModel
    
    // Performance optimization: Cache formatters
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize views
        pieChart = view.findViewById(R.id.pieChart)
        lineChart = view.findViewById(R.id.lineChart)
        toolbar = view.findViewById(R.id.toolbar)
        transactionTypeFilter = view.findViewById(R.id.transactionTypeFilter)
        paymentTypeFilter = view.findViewById(R.id.paymentTypeFilter)
        dateRangeFilter = view.findViewById(R.id.dateRangeFilter)

        // Setup toolbar navigation
        setupToolbar()
        
        // Setup filters
        setupFilters()

        // Obtain ViewModel with factory
        val database = com.example.dayplanner.NoteDatabase.getDatabase(requireContext())
        val currencyConverter = com.example.dayplanner.finance.CurrencyConverter()
        val repository = com.example.dayplanner.finance.FinanceRepository(database.financeDao(), currencyConverter)
        val factory = FinanceViewModelFactory(repository)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[FinanceViewModel::class.java]

        // Observe data
        observeData()
    }
    
    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupFilters() {
        // Transaction Type Filter
        val transactionTypes = arrayOf("Tümü", "Gelir", "Gider")
        val transactionTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, transactionTypes)
        transactionTypeFilter.setAdapter(transactionTypeAdapter)
        transactionTypeFilter.setText("Tümü", false)
        
        // Payment Type Filter
        val paymentTypes = arrayOf("Tümü", "Nakit", "Kredi Kartı", "Banka Transferi", "Diğer")
        val paymentTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paymentTypes)
        paymentTypeFilter.setAdapter(paymentTypeAdapter)
        paymentTypeFilter.setText("Tümü", false)
        
        // Date Range Filter
        val dateRanges = arrayOf("Tümü", "Bugün", "Bu Hafta", "Bu Ay", "Bu Yıl")
        val dateRangeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dateRanges)
        dateRangeFilter.setAdapter(dateRangeAdapter)
        dateRangeFilter.setText("Tümü", false)
    }
    
    private fun observeData() {
        // Observe category totals for expenses
        lifecycleScope.launch {
            viewModel.categoryTotals.collectLatest { list ->
                updatePieChart(list)
            }
        }

        // Observe totals over time
        lifecycleScope.launch {
            viewModel.transactions.collectLatest { list ->
                updateLineChart(list)
            }
        }
        
        // Observe summary totals
        lifecycleScope.launch {
            viewModel.totals.collectLatest { totals ->
                updateSummaryCards(totals)
            }
        }
    }
    
    private fun updateSummaryCards(totals: com.example.dayplanner.finance.FinancialTotals) {
        view?.findViewById<android.widget.TextView>(R.id.totalIncomeText)?.text = currencyFormatter.format(totals.income)
        view?.findViewById<android.widget.TextView>(R.id.totalExpenseText)?.text = currencyFormatter.format(totals.expense)
        view?.findViewById<android.widget.TextView>(R.id.balanceText)?.text = currencyFormatter.format(totals.balance)
    }

    private fun updatePieChart(list: List<CategoryTotal>) {
        val entries = list.map { PieEntry(it.total.toFloat(), it.category) }
        val set = PieDataSet(entries, "Gider Dağılımı").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }
        pieChart.data = PieData(set)
        pieChart.centerText = "Giderler"
        pieChart.invalidate()
    }

    private fun updateLineChart(transactions: List<com.example.dayplanner.finance.Transaction>) {
        // Create daily buckets for last 30 days
        val last30 = mutableMapOf<Int, Float>() // dayOffset -> balance (simplified)
        val now = System.currentTimeMillis()
        val dayMs = 24*60*60*1000L
        for (i in 0 until 30) last30[i] = 0f
        transactions.forEach { t ->
            val daysAgo = ((now - t.date) / dayMs).toInt().coerceIn(0,29)
            val sign = if (t.type == com.example.dayplanner.finance.TransactionType.INCOME) 1 else -1
            last30[daysAgo] = last30[daysAgo]!! + sign * t.amount.toFloat()
        }
        // accumulate backwards to show running balance
        val entries = mutableListOf<Entry>()
        var running = 0f
        for (i in 29 downTo 0) {
            running += last30[i] ?: 0f
            entries.add(Entry((29 - i).toFloat(), running))
        }
        val set = LineDataSet(entries, "Son 30 Gün Bakiye").apply {
            color = ColorTemplate.getHoloBlue()
            lineWidth = 2f
            setDrawCircles(false)
        }
        lineChart.data = LineData(set)
        lineChart.invalidate()
    }
}
