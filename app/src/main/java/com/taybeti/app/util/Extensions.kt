package com.taybeti.app.util

import java.util.UUID

fun generateNoteId(): String = UUID.randomUUID().toString()

fun generateRandomKey(length: Int = 20): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?"
    val secureRandom = java.security.SecureRandom()
    return (1..length).map { chars[secureRandom.nextInt(chars.length)] }.joinToString("")
}

fun formatTimestamp(epochMillis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(epochMillis))
}

fun String.toBase64(): String = java.util.Base64.getEncoder().encodeToString(this.toByteArray(Charsets.UTF_8))
fun String.fromBase64(): String = String(java.util.Base64.getDecoder().decode(this), Charsets.UTF_8)
