package com.example.dayplanner

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.finance.FinanceRecord
import com.example.dayplanner.finance.Installment
import com.example.dayplanner.finance.PaymentReminder
import com.example.dayplanner.passwords.PasswordDao
import com.example.dayplanner.passwords.PasswordHistory
import com.example.dayplanner.passwords.PasswordItem

@Database(
    entities = [
        Note::class,
        FinanceRecord::class,
        Installment::class,
        PaymentReminder::class,
        PasswordItem::class,
        PasswordHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun financeDao(): FinanceDao
    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "note_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
