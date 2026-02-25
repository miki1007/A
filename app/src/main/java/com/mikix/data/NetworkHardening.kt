package com.mikix.data

import kotlinx.coroutines.delay

suspend fun <T> retryIo(
    attempts: Int = 3,
    initialDelayMs: Long = 500,
    maxDelayMs: Long = 4_000,
    block: suspend () -> T
): T {
    var delayMs = initialDelayMs
    repeat(attempts - 1) {
        runCatching { return block() }
        delay(delayMs)
        delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
    }
    return block()
}
