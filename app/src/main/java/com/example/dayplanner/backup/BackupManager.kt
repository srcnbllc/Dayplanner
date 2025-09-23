package com.example.dayplanner.backup

import android.content.Context
import android.util.Base64
import com.example.dayplanner.security.SecurityKeyManager
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

object BackupManager {
    fun createEncryptedBackup(context: Context, content: ByteArray, outFile: File) {
        val key = SecurityKeyManager.getAppAesKey(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).also { java.security.SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val enc = cipher.doFinal(content)
        outFile.writeBytes(iv + enc)
    }

    fun decryptBackup(context: Context, file: File): ByteArray {
        val key = SecurityKeyManager.getAppAesKey(context)
        val all = file.readBytes()
        val iv = all.copyOfRange(0, 12)
        val data = all.copyOfRange(12, all.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(data)
    }
}












