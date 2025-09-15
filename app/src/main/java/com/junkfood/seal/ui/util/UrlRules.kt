package com.junkfood.seal.ui.util

import java.net.URI

object UrlRules {
    private val blockedHosts = setOf(
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "music.youtube.com",
        "youtu.be",
    )

    fun isBlocked(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return runCatching { URI(url).host?.lowercase() }.getOrNull()?.let { host ->
            blockedHosts.any { host == it || host.endsWith(".$it") }
        } ?: false
    }
}


