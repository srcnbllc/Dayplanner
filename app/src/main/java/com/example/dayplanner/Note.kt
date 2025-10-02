package com.example.dayplanner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long,
    val folderId: Int? = null,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val isEncrypted: Boolean = false,
    val encryptedBlob: ByteArray? = null,
    val imageUri: String? = null,
    val recurrenceRule: String? = null,
    val reminderMinutesBefore: Int? = null,
    val tags: String? = null,
    val status: String = "NOTES",
    val createdAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (id != other.id) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (date != other.date) return false
        if (folderId != other.folderId) return false
        if (isPinned != other.isPinned) return false
        if (isLocked != other.isLocked) return false
        if (isEncrypted != other.isEncrypted) return false
        if (encryptedBlob != null) {
            if (other.encryptedBlob == null) return false
            if (!encryptedBlob.contentEquals(other.encryptedBlob)) return false
        } else if (other.encryptedBlob != null) return false
        if (imageUri != other.imageUri) return false
        if (recurrenceRule != other.recurrenceRule) return false
        if (reminderMinutesBefore != other.reminderMinutesBefore) return false
        if (tags != other.tags) return false
        if (status != other.status) return false
        if (createdAt != other.createdAt) return false
        if (deletedAt != other.deletedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + (folderId ?: 0)
        result = 31 * result + isPinned.hashCode()
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + isEncrypted.hashCode()
        result = 31 * result + (encryptedBlob?.contentHashCode() ?: 0)
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        result = 31 * result + (recurrenceRule?.hashCode() ?: 0)
        result = 31 * result + (reminderMinutesBefore ?: 0)
        result = 31 * result + (tags?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (deletedAt?.hashCode() ?: 0)
        return result
    }
}