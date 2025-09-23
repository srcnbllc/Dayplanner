package com.example.dayplanner.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.dayplanner.Note
import com.example.dayplanner.NoteDatabase
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.passwords.Password
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.io.InputStream

class DataExportImportManager(private val context: Context) {

    suspend fun exportAllData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = NoteDatabase.getDatabase(context)
            val noteDao = database.noteDao()
            val financeDao = database.financeDao()
            val passwordDao = database.passwordDao()

            val notes = noteDao.getAllNotesSync()
            val transactions = financeDao.getAllTransactionsSync()
            val passwords = passwordDao.getAllPasswordsSync()

            val exportData = JSONObject().apply {
                put("version", "1.0")
                put("exportDate", System.currentTimeMillis())
                put("notes", notesToJson(notes))
                put("transactions", transactionsToJson(transactions))
                put("passwords", passwordsToJson(passwords))
            }

            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.use { stream ->
                stream.write(exportData.toString().toByteArray())
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importAllData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: return@withContext false

            val exportData = JSONObject(jsonString)
            val database = NoteDatabase.getDatabase(context)
            val noteDao = database.noteDao()
            val financeDao = database.financeDao()
            val passwordDao = database.passwordDao()

            // Import notes
            val notesJson = exportData.getJSONArray("notes")
            for (i in 0 until notesJson.length()) {
                val noteJson = notesJson.getJSONObject(i)
                val note = Note(
                    id = 0, // Let database generate new ID
                    title = noteJson.getString("title"),
                    description = noteJson.getString("description"),
                    date = noteJson.getString("date"),
                    folderId = if (noteJson.has("folderId") && !noteJson.isNull("folderId")) 
                        noteJson.getInt("folderId") else null,
                    isPinned = noteJson.optBoolean("isPinned", false),
                    isLocked = noteJson.optBoolean("isLocked", false),
                    imageUri = if (noteJson.has("imageUri") && !noteJson.isNull("imageUri")) 
                        noteJson.getString("imageUri") else null,
                    recurrenceRule = if (noteJson.has("recurrenceRule") && !noteJson.isNull("recurrenceRule")) 
                        noteJson.getString("recurrenceRule") else null,
                    reminderMinutesBefore = if (noteJson.has("reminderMinutesBefore") && !noteJson.isNull("reminderMinutesBefore")) 
                        noteJson.getInt("reminderMinutesBefore") else null,
                    status = noteJson.optString("status", "NEW"),
                    createdAt = noteJson.optLong("createdAt", System.currentTimeMillis())
                )
                noteDao.insert(note)
            }

            // Import transactions
            val transactionsJson = exportData.getJSONArray("transactions")
            for (i in 0 until transactionsJson.length()) {
                val transactionJson = transactionsJson.getJSONObject(i)
                val transaction = Transaction(
                    id = 0, // Let database generate new ID
                    title = transactionJson.getString("title"),
                    amount = transactionJson.getDouble("amount"),
                    category = transactionJson.getString("category"),
                    description = transactionJson.optString("description", ""),
                    type = com.example.dayplanner.finance.TransactionType.valueOf(transactionJson.getString("type")),
                    date = transactionJson.getLong("date")
                )
                financeDao.insertTransaction(transaction)
            }

            // Import passwords
            val passwordsJson = exportData.getJSONArray("passwords")
            for (i in 0 until passwordsJson.length()) {
                val passwordJson = passwordsJson.getJSONObject(i)
                val password = Password(
                    id = 0, // Let database generate new ID
                    title = passwordJson.getString("title"),
                    username = passwordJson.getString("username"),
                    password = passwordJson.getString("password"),
                    category = passwordJson.getString("category"),
                    website = if (passwordJson.has("website") && !passwordJson.isNull("website")) 
                        passwordJson.getString("website") else null,
                    notes = if (passwordJson.has("notes") && !passwordJson.isNull("notes")) 
                        passwordJson.getString("notes") else null
                )
                passwordDao.insertPassword(password)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun notesToJson(notes: List<Note>): JSONArray {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            val noteJson = JSONObject().apply {
                put("title", note.title)
                put("description", note.description)
                put("date", note.date)
                put("folderId", note.folderId)
                put("isPinned", note.isPinned)
                put("isLocked", note.isLocked)
                put("imageUri", note.imageUri)
                put("recurrenceRule", note.recurrenceRule)
                put("reminderMinutesBefore", note.reminderMinutesBefore)
                put("status", note.status)
                put("createdAt", note.createdAt)
            }
            jsonArray.put(noteJson)
        }
        return jsonArray
    }

    private fun transactionsToJson(transactions: List<Transaction>): JSONArray {
        val jsonArray = JSONArray()
        transactions.forEach { transaction ->
            val transactionJson = JSONObject().apply {
                put("title", transaction.title)
                put("amount", transaction.amount)
                put("category", transaction.category)
                put("description", transaction.description)
                put("type", transaction.type.name)
                put("date", transaction.date)
            }
            jsonArray.put(transactionJson)
        }
        return jsonArray
    }

    private fun passwordsToJson(passwords: List<Password>): JSONArray {
        val jsonArray = JSONArray()
        passwords.forEach { password ->
            val passwordJson = JSONObject().apply {
                put("title", password.title)
                put("username", password.username)
                put("password", password.password)
                put("category", password.category)
                put("website", password.website)
                put("notes", password.notes)
            }
            jsonArray.put(passwordJson)
        }
        return jsonArray
    }
}
