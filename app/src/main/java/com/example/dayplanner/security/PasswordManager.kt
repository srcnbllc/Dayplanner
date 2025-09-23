package com.example.dayplanner.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object PasswordManager {
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128
    private const val KEY_SIZE = 256

    /**
     * Kullanıcı şifresinden AES anahtarı oluşturur
     */
    fun generateKeyFromPassword(password: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return SecretKeySpec(hash, "AES")
    }

    /**
     * Notu şifreler
     */
    fun encryptNote(content: String, password: String): String {
        val key = generateKeyFromPassword(password)
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        
        val encryptedBytes = cipher.doFinal(content.toByteArray())
        val combined = iv + encryptedBytes
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Notu şifreler
     */
    fun decryptNote(encryptedContent: String, password: String): String {
        val combined = Base64.decode(encryptedContent, Base64.DEFAULT)
        val iv = combined.copyOfRange(0, IV_SIZE)
        val encryptedBytes = combined.copyOfRange(IV_SIZE, combined.size)
        
        val key = generateKeyFromPassword(password)
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    /**
     * Şifre doğrulama
     */
    fun verifyPassword(encryptedContent: String, password: String): Boolean {
        return try {
            decryptNote(encryptedContent, password)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Güçlü şifre kontrolü
     */
    fun isStrongPassword(password: String): Boolean {
        if (password.length < 6) return false
        
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecial
    }

    /**
     * Şifre gücü seviyesi
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < 6) return PasswordStrength.WEAK
        
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score++
        
        return when (score) {
            in 0..2 -> PasswordStrength.WEAK
            in 3..4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}
