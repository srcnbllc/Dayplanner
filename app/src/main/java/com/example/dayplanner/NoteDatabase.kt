package com.example.dayplanner

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.Category
import com.example.dayplanner.finance.Debt
import com.example.dayplanner.finance.CurrencyRate
import com.example.dayplanner.finance.FinanceRecord
import com.example.dayplanner.finance.Installment
import com.example.dayplanner.finance.PaymentReminder
import com.example.dayplanner.passwords.Password
import com.example.dayplanner.passwords.PasswordCategory
import com.example.dayplanner.passwords.PasswordItem
import com.example.dayplanner.passwords.PasswordHistory
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.finance.CurrencyRateDao
import com.example.dayplanner.passwords.PasswordDao
import com.example.dayplanner.ActivityLogDao
import com.example.dayplanner.ActivityLog
import com.example.dayplanner.finance.EnumConverters
import com.example.dayplanner.finance.Converters

@Database(entities = [
    Note::class, 
    Folder::class,
    Transaction::class,
    FinanceRecord::class,
    Installment::class,
    PaymentReminder::class,
    Category::class,
    Debt::class,
    CurrencyRate::class,
    Password::class,
    PasswordCategory::class,
    PasswordItem::class,
    PasswordHistory::class,
    ActivityLog::class
], version = 12, exportSchema = false)
@TypeConverters(EnumConverters::class, Converters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun financeDao(): FinanceDao
    abstract fun currencyRateDao(): CurrencyRateDao
    abstract fun passwordDao(): PasswordDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .fallbackToDestructiveMigration() // Database'i tamamen sıfırla
                    .allowMainThreadQueries() // Test için main thread'e izin ver
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}