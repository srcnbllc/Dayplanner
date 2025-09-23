package com.example.dayplanner.ocr

import android.content.Context
import android.net.Uri

object OcrHelper {
    suspend fun recognize(context: Context, uri: Uri): String {
        // OCR functionality temporarily disabled for 16KB compatibility
        // ML Kit has been removed to fix 16KB page size warnings
        // Parameters are kept for API compatibility
        android.util.Log.d("OcrHelper", "OCR called with context: ${context.javaClass.simpleName}, uri: $uri")
        return "OCR özelliği 16KB uyumluluğu için geçici olarak devre dışı"
    }
}









