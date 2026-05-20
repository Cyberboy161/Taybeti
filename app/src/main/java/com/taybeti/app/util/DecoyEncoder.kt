package com.taybeti.app.util

import java.util.Base64

enum class DecoyPlatform(val label: String, val urlTemplate: String) {
    YOUTUBE("YouTube", "https://www.youtube.com/watch?v="),
    TWITTER("X / Twitter", "https://x.com/user/status/"),
    INSTAGRAM("Instagram", "https://www.instagram.com/p/"),
    REDDIT("Reddit", "https://www.reddit.com/r/all/comments/"),
    WIKIPEDIA("Wikipedia", "https://en.wikipedia.org/wiki/"),
    SOUNDCLOUD("SoundCloud", "https://soundcloud.com/user/sets/")
}

object DecoyEncoder {

    fun encode(data: String, platform: DecoyPlatform): String {
        val base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(data.toByteArray(Charsets.UTF_8))
        return "${platform.urlTemplate}$base64"
    }

    fun decode(url: String, platform: DecoyPlatform): String? {
        return try {
            val prefix = platform.urlTemplate
            if (!url.startsWith(prefix)) return null
            val id = url.removePrefix(prefix).trim()
            val bytes = Base64.getUrlDecoder().decode(id)
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
