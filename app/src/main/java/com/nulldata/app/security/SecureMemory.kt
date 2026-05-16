package com.nulldata.app.security

object SecureMemory {

    fun clear(array: CharArray?) {
        array?.let {
            for (i in it.indices) {
                it[i] = '\u0000'
            }
        }
    }

    fun clear(array: ByteArray?) {
        array?.let {
            for (i in it.indices) {
                it[i] = 0
            }
        }
    }

    fun clearString(s: String?): String {
        return "\u0000".repeat(s?.length ?: 0)
    }
}
