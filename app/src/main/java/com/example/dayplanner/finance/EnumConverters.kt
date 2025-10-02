package com.example.dayplanner.finance

import androidx.room.TypeConverter

/**
 * Room TypeConverters for enum types
 */
class EnumConverters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? =
        value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun fromPaymentType(value: PaymentType?): String? = value?.name

    @TypeConverter
    fun toPaymentType(value: String?): PaymentType? =
        value?.let { PaymentType.valueOf(it) }

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus?): String? = value?.name

    @TypeConverter
    fun toTransactionStatus(value: String?): TransactionStatus? =
        value?.let { TransactionStatus.valueOf(it) }
}
