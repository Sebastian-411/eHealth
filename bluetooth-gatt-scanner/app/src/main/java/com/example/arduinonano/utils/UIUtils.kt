package com.example.arduinonano.utils

import android.content.Context
import android.widget.Toast

object UIUtils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
