package com.example.arduinonano.data

class MessageAssembler {
    private val receivedChunks = mutableMapOf<String, StringBuilder>()

    // TODO: Implement a better way to assemble messages as JSON objects
    fun processReceivedData(data: String): String? {
        val parts = data.split("#", limit = 2)
        if (parts.size == 2) {
            val id = parts[0]
            val jsonData = parts[1]

            val jsonBuilder = receivedChunks.getOrPut(id) { StringBuilder() }
            jsonBuilder.append(jsonData)

            if (isCompleteMessageReceived(jsonBuilder.toString())) {
                val completeJson = jsonBuilder.toString()
                receivedChunks.remove(id)
                return completeJson
            }
        }
        return null
    }

    private fun isCompleteMessageReceived(data: String): Boolean {
        return data.endsWith("}")
    }
}