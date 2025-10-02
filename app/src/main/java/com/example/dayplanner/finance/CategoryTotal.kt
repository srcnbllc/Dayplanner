package com.example.dayplanner.finance

data class CategoryTotal(
    val category: String,
    val total: Double,
    val currency: String
)

// Helper data class for totals by currency
data class CurrencySum(
    val currency: String,
    val total: Double
)


