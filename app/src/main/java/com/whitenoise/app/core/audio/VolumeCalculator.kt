package com.whitenoise.app.core.audio

import kotlin.math.max
import kotlin.math.min

object VolumeCalculator {

    const val DUCK_FACTOR = 0.25f

    /**
     * Calculates the actual volume output for a sound track.
     * Formula: ActualVolume = TrackVolume * MasterVolume * SleepFadeMultiplier * (DuckingFactor if ducked)
     */
    fun calculateActualVolume(
        trackVolume: Float,
        masterVolume: Float,
        sleepFadeMultiplier: Float = 1.0f,
        isDucked: Boolean = false,
        isPlaying: Boolean = true,
        isMuted: Boolean = false
    ): Float {
        if (!isPlaying || isMuted) return 0f
        val duckMultiplier = if (isDucked) DUCK_FACTOR else 1.0f
        val calculated = trackVolume.coerceIn(0f, 1f) *
                masterVolume.coerceIn(0f, 1f) *
                sleepFadeMultiplier.coerceIn(0f, 1f) *
                duckMultiplier
        return calculated.coerceIn(0f, 1f)
    }

    /**
     * Compute smooth quadratic perceptual decay curve for sleep timer fade-out.
     * When remainingTime <= 0 -> 0.0f
     * When remainingTime >= fadeDuration -> 1.0f
     * In between -> (remainingTime / fadeDuration)^2
     */
    fun calculateSleepFadeMultiplier(
        remainingSeconds: Long,
        fadeDurationSeconds: Long
    ): Float {
        if (fadeDurationSeconds <= 0L) return if (remainingSeconds > 0L) 1.0f else 0.0f
        if (remainingSeconds <= 0L) return 0.0f
        if (remainingSeconds >= fadeDurationSeconds) return 1.0f

        val ratio = (remainingSeconds.toDouble() / fadeDurationSeconds.toDouble()).coerceIn(0.0, 1.0)
        // Quadratic curve matches human auditory perception closely and avoids sharp cliff drops
        return (ratio * ratio).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Determines the fade duration based on total sleep timer duration.
     * Typically min(30s, totalSeconds / 3), at least 5s for very short tests, up to 60s for long sessions.
     */
    fun determineFadeDuration(totalSeconds: Long): Long {
        if (totalSeconds <= 10L) return max(1L, totalSeconds / 2)
        if (totalSeconds <= 60L) return min(20L, totalSeconds / 3)
        return min(45L, totalSeconds / 4)
    }
}
