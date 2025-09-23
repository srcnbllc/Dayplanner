package com.example.dayplanner

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.finance.FinanceRecord
import com.example.dayplanner.finance.Installment
import com.example.dayplanner.finance.PaymentReminder
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.Category
import com.example.dayplanner.passwords.PasswordDao
import com.example.dayplanner.passwords.PasswordHistory
import com.example.dayplanner.passwords.PasswordItem
import com.example.dayplanner.passwords.Password
import com.example.dayplanner.passwords.PasswordCategory
import com.example.dayplanner.tags.NoteTag
import com.example.dayplanner.tags.Tag
import com.example.dayplanner.tags.TagDao

@Database(
    entities = [
        Note::class,
        Folder::class,
        FinanceRecord::class,
        Installment::class,
        PaymentReminder::class,
        Transaction::class,
        Category::class,
        PasswordItem::class,
        PasswordHistory::class,
        Password::class,
        PasswordCategory::class,
        Tag::class,
        NoteTag::class,
        ActivityLog::class
    ],
    version = 7,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun financeDao(): FinanceDao
    abstract fun passwordDao(): PasswordDao
    abstract fun tagDao(): TagDao
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
                    .fallbackToDestructiveMigration()
                    .addMigrations()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
