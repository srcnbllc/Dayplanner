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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val category: String,
    val subtype: String? = null,
    val title: String,
    val amount: Double,
    val currency: String = "TRY",
    val paymentType: PaymentType = PaymentType.CASH,
    val date: Long,
    val description: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val reminder: Long? = null,
    val meta: Map<String, Any>? = null,
    val isDebt: Boolean = false,
    val debtId: Long? = null,
    val status: TransactionStatus = TransactionStatus.ACTIVE
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: TransactionType,
    val icon: String? = null
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String = "TRY",
    val dueDate: Long? = null,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "currency_rates")
data class CurrencyRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val base: String,
    val currency: String,
    val rate: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "finance_record")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val currency: String = "TRY",
    val date: Long,
    val description: String? = null,
    val category: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installment")
data class Installment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val financeRecordId: Int,
    val amount: Double,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val paidDate: Long? = null
)

@Entity(tableName = "payment_reminder")
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val financeRecordId: Int,
    val reminderTime: Long,
    val isTriggered: Boolean = false
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): Map<String, Any>? {
        return if (value == null) null else {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(value, type)
        }
    }

    @TypeConverter
    fun fromMap(map: Map<String, Any>?): String? {
        return if (map == null) null else {
            val gson = Gson()
            gson.toJson(map)
        }
    }
}