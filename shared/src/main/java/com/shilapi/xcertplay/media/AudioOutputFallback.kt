package com.shilapi.xcertplay.media

/** One independently constructible Android audio route. */
internal data class AudioOutputAttempt<T>(
    val label: String,
    val create: () -> T,
)

internal data class AudioOutputSelection<T>(
    val output: T,
    val label: String,
    val rejected: List<String>,
)

/**
 * Tries routes in order, releasing any uninitialized candidate before continuing.
 *
 * Vendor audio policies can reject either modern AudioAttributes or a legacy stream constructor.
 * Linkage failures are recoverable here, but VM/process-fatal errors are not swallowed.
 */
internal object AudioOutputFallback {
    fun <T> select(
        attempts: List<AudioOutputAttempt<T>>,
        isReady: (T) -> Boolean,
        release: (T) -> Unit,
        onRejected: (label: String, reason: String) -> Unit = { _, _ -> },
    ): AudioOutputSelection<T> {
        require(attempts.isNotEmpty()) { "At least one audio output route is required" }
        val rejected = mutableListOf<String>()
        var lastFailure: Throwable? = null
        for (attempt in attempts) {
            val candidate = try {
                attempt.create()
            } catch (error: Throwable) {
                if (error !is Exception && error !is LinkageError) throw error
                lastFailure = error
                rejected += attempt.label
                onRejected(attempt.label, error.javaClass.simpleName)
                continue
            }
            var readinessFailure: Throwable? = null
            val ready = try {
                isReady(candidate)
            } catch (error: Throwable) {
                if (error !is Exception && error !is LinkageError) throw error
                readinessFailure = error
                lastFailure = error
                false
            }
            if (ready) {
                return AudioOutputSelection(candidate, attempt.label, rejected)
            }
            runCatching { release(candidate) }
            rejected += attempt.label
            onRejected(attempt.label, readinessFailure?.javaClass?.simpleName ?: "uninitialized")
        }
        throw IllegalStateException(
            "No Android audio output route initialized (tried ${attempts.joinToString { it.label }})",
            lastFailure,
        )
    }
}
