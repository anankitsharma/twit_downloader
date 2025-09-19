package com.rit.twitdownloader.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Simple process-wide bus for URLs shared into the app.
 * - replay = 1 so latest link is delivered to late subscribers (e.g., Home tab after navigation)
 * - extraBufferCapacity = 1 to avoid drop on quick successive shares
 */
object SharedUrlBus {
    private val _urls = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 1)
    val urls = _urls.asSharedFlow()

    fun emit(url: String) {
        if (url.isNotBlank()) {
            _urls.tryEmit(url)
        }
    }
}


