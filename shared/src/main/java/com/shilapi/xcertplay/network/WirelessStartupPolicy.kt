package com.shilapi.xcertplay.network

import java.io.IOException

object WirelessStartupPolicy {
    const val HOTSPOT_READY_MILLIS = 15_000L
    const val INTERFACE_POLL_MILLIS = 250L
    const val STABLE_SAMPLES = 3
    const val FIRST_TCP_MILLIS = 30_000L
    const val MAX_STARTUP_RETRIES = 5
    const val STABLE_SESSION_MILLIS = 60_000L
}

enum class WirelessStartupFailure {
    HOTSPOT_NOT_READY,
    FIRST_TCP_TIMEOUT,
    HOTSPOT_CONFIGURATION,
    /** A required service/adapter is absent or its permission is permanently unavailable. */
    PLATFORM_UNAVAILABLE,
}

class WirelessStartupException(
    val reason: WirelessStartupFailure,
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)
