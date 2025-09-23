package com.example.dayplanner.security

import android.content.Context
import android.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object SecurityKeyManager {
    private const val PREF_KEY = "app_aes_key"

    fun getAppAesKey(context: Context): SecretKey {
        val prefs = CryptoUtils.getSecurePrefs(context)
        val existingB64 = prefs.getString(PREF_KEY, null)
        if (existingB64 != null) {
            val bytes = Base64.decode(existingB64, Base64.DEFAULT)
            return SecretKeySpec(bytes, "AES")
        }
        val keyBytes = ByteArray(32)
        SecureRandom().nextBytes(keyBytes)
        val b64 = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
        prefs.edit().putString(PREF_KEY, b64).apply()
        return SecretKeySpec(keyBytes, "AES")
    }
}


