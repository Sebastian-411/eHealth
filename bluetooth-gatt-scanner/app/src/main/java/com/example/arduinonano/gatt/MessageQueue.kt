package com.example.arduinonano.gatt

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class MessageQueue {
    private val queue: MutableList<String> = mutableListOf()

    fun addMessage(fragment: String) {
        queue.add(fragment)
        processMessages()
    }

    private fun processMessages() {
        val completeMessage = buildCompleteMessage()
        if (completeMessage != null) {
            processCompleteMessage(completeMessage)
            clearQueue()
        }
    }

    private fun buildCompleteMessage(): String? {
        return if (queue.joinToString("").contains("}")) {
            queue.joinToString("")
        } else {
            null
        }
    }

    private fun processCompleteMessage(message: String) {
        try {
            val jsonObject = JSONObject(message)
            Log.d("MessageQueue", "Mensaje procesado: $jsonObject")
        } catch (e: JSONException) {
            Log.e("MessageQueue", "Error procesando JSON: ${e.message}")
        }
    }

    private fun clearQueue() {
        queue.clear()
    }
}
