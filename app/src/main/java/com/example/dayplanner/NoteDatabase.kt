package com.example.dayplanner

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dayplanner.finance.*
import com.example.dayplanner.passwords.*
import com.example.dayplanner.tags.*

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
    version = 8,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    // DAO tanımları
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun financeDao(): FinanceDao
    abstract fun passwordDao(): PasswordDao
    abstract fun tagDao(): TagDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        // Migration from version 7 to 8 - Add isEncrypted field
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note_table ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): NoteDatabase {
            // Eğer INSTANCE null ise synchronized içinde oluştur
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .addMigrations(MIGRATION_7_8)
                    // 🔄 Şema değiştiğinde migration yoksa db'yi sıfırla
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
