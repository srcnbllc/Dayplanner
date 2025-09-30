package com.example.dayplanner.finance

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: TransactionType, // Gelir/Gider
    val subtype: String, // Maaş, Serbest Gelir, Kira Gideri, vb.
    val title: String,
    val amount: Double,
    val currency: String = "TRY", // Para birimi
    val category: String,
    val description: String = "",
    val date: Long, // Unix timestamp
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // daily, weekly, monthly, yearly
    val reminder: Boolean = false,
    val meta: String? = null // JSON string for dynamic fields
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class IncomeSubtype {
    SALARY, // Maaş
    FREELANCE, // Serbest Gelir
    RENTAL_INCOME, // Kira Geliri
    INVESTMENT_RETURN, // Yatırım Getirisi
    RECEIVABLE, // Alacak
    BONUS, // Prim / Bonus
    PENSION, // Emekli Maaşı
    INTEREST_DIVIDEND, // Faiz / Temettü
    OTHER_INCOME // Diğer Gelirler
}

enum class ExpenseSubtype {
    RENT_EXPENSE, // Kira Gideri
    BANK_LOAN, // Banka Kredisi
    BILL_PAYMENT, // Fatura Ödemeleri
    INSURANCE, // Sigorta
    TAX_FEE, // Vergi/Harç
    EDUCATION, // Eğitim
    HEALTHCARE, // Sağlık
    TRANSPORTATION, // Ulaşım
    FOOD_GROCERY, // Gıda / Market
    SHOPPING, // Alışveriş
    TRAVEL, // Tatiller / Seyahat
    SUBSCRIPTIONS, // Abonelikler
    OTHER_EXPENSE // Diğer Giderler
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val name: String,
    val type: TransactionType,
    val icon: String? = null
)

// Type converters for Room database
class Converters {
    @TypeConverter
    fun fromString(value: String?): Map<String, Any>? {
        if (value == null) return null
        val gson = Gson()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Any>?): String? {
        if (map == null) return null
        val gson = Gson()
        return gson.toJson(map)
    }
}
