package com.example.dayplanner.utils

import android.content.Context
import android.widget.Toast

object CustomToast {
    var enabled = true  // Enable Toast messages for user feedback

    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (enabled) {
            Toast.makeText(context, message, duration).show()
        }
    }
    
    fun showLong(context: Context, message: String) {
        if (enabled) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    
    fun showShort(context: Context, message: String) {
        if (enabled) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
