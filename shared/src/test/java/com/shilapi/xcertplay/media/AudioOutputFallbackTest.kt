package com.shilapi.xcertplay.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class AudioOutputFallbackTest {
    @Test
    fun rejectedUsageRouteFallsBackToStandardStreamAndReportsTheWinner() {
        val events = mutableListOf<String>()
        val standard = Any()
        val result = AudioOutputFallback.select(
            attempts = listOf(
                AudioOutputAttempt("usage") { throw IllegalArgumentException("vendor rejection") },
                AudioOutputAttempt("standard-stream") { standard },
            ),
            isReady = { true },
            release = { events += "release" },
            onRejected = { label, reason -> events += "$label:$reason" },
        )

        assertSame(standard, result.output)
        assertEquals("standard-stream", result.label)
        assertEquals(listOf("usage"), result.rejected)
        assertEquals(listOf("usage:IllegalArgumentException"), events)
    }

    @Test
    fun uninitializedTrackIsReleasedBeforeTryingTheNextRoute() {
        val events = mutableListOf<String>()
        val result = AudioOutputFallback.select(
            attempts = listOf(
                AudioOutputAttempt("vendor") { "not-ready" },
                AudioOutputAttempt("usage") { "ready" },
            ),
            isReady = { it == "ready" },
            release = { events += "release:$it" },
            onRejected = { label, reason -> events += "$label:$reason" },
        )

        assertEquals("ready", result.output)
        assertEquals(
            listOf("release:not-ready", "vendor:uninitialized"),
            events,
        )
    }

    @Test
    fun allRejectedRoutesProduceOneConcreteFailure() {
        val error = assertThrows(IllegalStateException::class.java) {
            AudioOutputFallback.select(
                attempts = listOf(
                    AudioOutputAttempt("usage") { "bad" },
                    AudioOutputAttempt("standard-stream") { "also-bad" },
                ),
                isReady = { false },
                release = {},
            )
        }

        assertEquals(
            "No Android audio output route initialized (tried usage, standard-stream)",
            error.message,
        )
    }
}
