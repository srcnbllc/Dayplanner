package com.example.dayplanner

import android.content.Context
import android.net.Uri
import com.example.dayplanner.utils.CustomToast
import com.example.dayplanner.security.SecurityKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportImportManager {

    /**
     * Export all notes to a JSON file
     */
    suspend fun exportNotes(context: Context, notes: List<Note>): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()
                
                notes.forEach { note ->
                    val noteJson = JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("description", note.description)
                        put("date", note.date)
                        put("folderId", note.folderId)
                        put("isPinned", note.isPinned)
                        put("isLocked", note.isLocked)
                        put("isEncrypted", note.isEncrypted)
                        put("imageUri", note.imageUri)
                        put("recurrenceRule", note.recurrenceRule)
                        put("reminderMinutesBefore", note.reminderMinutesBefore)
                        put("tags", note.tags)
                        put("status", note.status)
                        put("createdAt", note.createdAt)
                        put("deletedAt", note.deletedAt)
                        // Note: encryptedBlob is not exported for security reasons
                    }
                    jsonArray.put(noteJson)
                }

                val exportData = JSONObject().apply {
                    put("version", "1.0")
                    put("exportDate", System.currentTimeMillis())
                    put("noteCount", notes.size)
                    put("notes", jsonArray)
                }

                val fileName = "notes_backup_${getCurrentDateString()}.json"
                val file = File(context.getExternalFilesDir(null), fileName)
                
                file.writeText(exportData.toString())
                
                android.util.Log.d("ExportImportManager", "Notes exported to: ${file.absolutePath}")
                Uri.fromFile(file)
            } catch (e: Exception) {
                android.util.Log.e("ExportImportManager", "Export failed: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Import notes from a JSON file
     */
    suspend fun importNotes(context: Context, uri: Uri, noteViewModel: NoteViewModel): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                val jsonObject = JSONObject(jsonString)
                val notesArray = jsonObject.getJSONArray("notes")
                
                var importedCount = 0
                var skippedCount = 0

                for (i in 0 until notesArray.length()) {
                    val noteJson = notesArray.getJSONObject(i)
                    
                    try {
                        val note = Note(
                            id = 0, // New ID will be assigned
                            title = noteJson.getString("title"),
                            description = noteJson.getString("description"),
                            date = noteJson.getString("date"),
                            folderId = if (noteJson.isNull("folderId")) null else noteJson.getInt("folderId"),
                            isPinned = noteJson.getBoolean("isPinned"),
                            isLocked = noteJson.getBoolean("isLocked"),
                            isEncrypted = noteJson.getBoolean("isEncrypted"),
                            imageUri = if (noteJson.isNull("imageUri")) null else noteJson.getString("imageUri"),
                            recurrenceRule = if (noteJson.isNull("recurrenceRule")) null else noteJson.getString("recurrenceRule"),
                            reminderMinutesBefore = if (noteJson.isNull("reminderMinutesBefore")) null else noteJson.getInt("reminderMinutesBefore"),
                            tags = if (noteJson.isNull("tags")) null else noteJson.getString("tags"),
                            status = noteJson.getString("status"),
                            createdAt = noteJson.getLong("createdAt"),
                            deletedAt = if (noteJson.isNull("deletedAt")) null else noteJson.getLong("deletedAt")
                        )

                        withContext(Dispatchers.Main) {
                            noteViewModel.insert(note)
                        }
                        importedCount++
                    } catch (e: Exception) {
                        android.util.Log.w("ExportImportManager", "Failed to import note at index $i: ${e.message}")
                        skippedCount++
                    }
                }

                android.util.Log.d("ExportImportManager", "Import completed: $importedCount imported, $skippedCount skipped")
                true
            } catch (e: Exception) {
                android.util.Log.e("ExportImportManager", "Import failed: ${e.message}", e)
                false
            }
        }
    }

    /**
     * Create a backup file with encryption
     */
    suspend fun createEncryptedBackup(context: Context, notes: List<Note>): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()
                
                notes.forEach { note ->
                    val noteJson = JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("description", note.description)
                        put("date", note.date)
                        put("folderId", note.folderId)
                        put("isPinned", note.isPinned)
                        put("isLocked", note.isLocked)
                        put("isEncrypted", note.isEncrypted)
                        put("imageUri", note.imageUri)
                        put("recurrenceRule", note.recurrenceRule)
                        put("reminderMinutesBefore", note.reminderMinutesBefore)
                        put("tags", note.tags)
                        put("status", note.status)
                        put("createdAt", note.createdAt)
                        put("deletedAt", note.deletedAt)
                    }
                    jsonArray.put(noteJson)
                }

                val exportData = JSONObject().apply {
                    put("version", "1.0")
                    put("exportDate", System.currentTimeMillis())
                    put("noteCount", notes.size)
                    put("notes", jsonArray)
                }

                val fileName = "notes_encrypted_backup_${getCurrentDateString()}.dat"
                val file = File(context.getExternalFilesDir(null), fileName)
                
                // Encrypt the backup
                val key = SecurityKeyManager.getAppAesKey(context)
                val encryptedData = com.example.dayplanner.security.CryptoUtils.encryptAesGcm(
                    key, 
                    exportData.toString().toByteArray(),
                    ByteArray(12).also { java.security.SecureRandom().nextBytes(it) }
                )
                
                file.writeBytes(encryptedData)
                
                android.util.Log.d("ExportImportManager", "Encrypted backup created: ${file.absolutePath}")
                Uri.fromFile(file)
            } catch (e: Exception) {
                android.util.Log.e("ExportImportManager", "Encrypted backup failed: ${e.message}", e)
                null
            }
        }
    }

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
