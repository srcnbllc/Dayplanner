package com.example.dayplanner

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.Category
import com.example.dayplanner.finance.FinanceRecord
import com.example.dayplanner.finance.Installment
import com.example.dayplanner.finance.PaymentReminder
import com.example.dayplanner.passwords.Password
import com.example.dayplanner.passwords.PasswordCategory
import com.example.dayplanner.passwords.PasswordItem
import com.example.dayplanner.passwords.PasswordHistory
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.passwords.PasswordDao
import com.example.dayplanner.ActivityLogDao
import com.example.dayplanner.ActivityLog

@Database(entities = [
    Note::class, 
    Folder::class,
    Transaction::class,
    Category::class,
    FinanceRecord::class,
    Installment::class,
    PaymentReminder::class,
    Password::class,
    PasswordCategory::class,
    PasswordItem::class,
    PasswordHistory::class,
    ActivityLog::class
], version = 6, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun financeDao(): FinanceDao
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // 1 -> 2: isEncrypted (INTEGER NOT NULL DEFAULT 0), isDeleted (INTEGER NOT NULL DEFAULT 0)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note_table ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE note_table ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        // 2 -> 3: Replace isDeleted with status column
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new status column
                database.execSQL("ALTER TABLE note_table ADD COLUMN status TEXT NOT NULL DEFAULT 'NEW'")
                database.execSQL("ALTER TABLE note_table ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE note_table ADD COLUMN deletedAt INTEGER")
                
                // Migrate existing data: convert isDeleted to status
                database.execSQL("UPDATE note_table SET status = 'DELETED' WHERE isDeleted = 1")
                database.execSQL("UPDATE note_table SET status = 'NOTES' WHERE isDeleted = 0 AND status = 'NEW'")
                
                // Drop old isDeleted column
                database.execSQL("ALTER TABLE note_table DROP COLUMN isDeleted")
            }
        }

        // 3 -> 4: Add finance and password tables
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        amount REAL NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        type TEXT NOT NULL,
                        date INTEGER NOT NULL
                    )
                """)
                
                // Create categories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        name TEXT PRIMARY KEY NOT NULL
                    )
                """)
                
                // Create passwords table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS passwords (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        username TEXT NOT NULL,
                        password TEXT NOT NULL,
                        category TEXT NOT NULL,
                        website TEXT,
                        notes TEXT
                    )
                """)
                
                // Create password_categories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS password_categories (
                        name TEXT PRIMARY KEY NOT NULL
                    )
                """)
            }
        }

        // 4 -> 5: Add activity log table
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create activity_log_table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS activity_log_table (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        action TEXT NOT NULL,
                        description TEXT NOT NULL,
                        noteId INTEGER,
                        noteTitle TEXT,
                        timestamp INTEGER NOT NULL,
                        details TEXT
                    )
                """)
            }
        }

        // 5 -> 6: Add missing finance and password tables
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create finance_record table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS finance_record (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        amount REAL NOT NULL,
                        startDate TEXT NOT NULL,
                        endDate TEXT
                    )
                """)
                
                // Create installment table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS installment (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        financeRecordId INTEGER NOT NULL,
                        dueDate TEXT NOT NULL,
                        amount REAL NOT NULL,
                        isPaid INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Create payment_reminder table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS payment_reminder (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        financeRecordId INTEGER NOT NULL,
                        daysBefore INTEGER NOT NULL
                    )
                """)
                
                // Create password_item table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS password_item (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        service TEXT NOT NULL,
                        username TEXT NOT NULL,
                        encryptedPassword BLOB NOT NULL,
                        backupEmail TEXT,
                        securityKeyNote TEXT,
                        lastChangedAt TEXT NOT NULL
                    )
                """)
                
                // Create password_history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS password_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        passwordItemId INTEGER NOT NULL,
                        encryptedPassword BLOB NOT NULL,
                        changedAt TEXT NOT NULL
                    )
                """)
            }
        }
    }
}
