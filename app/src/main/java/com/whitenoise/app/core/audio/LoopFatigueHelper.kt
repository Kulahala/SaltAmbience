package com.whitenoise.app.core.audio

import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import kotlin.random.Random

/**
 * Helper for Anti-Loop Fatigue (方案 A: 起播随机时间戳偏置, 方案 B: ±2% 自然微速差).
 *
 * 方案 A (Random Start Offset):
 * When a track starts playing or is activated by a preset, seek to a safe random offset
 * [0, duration - endMargin] to destroy memory point of 00:00.000.
 *
 * 方案 B (Micro-Speed Drift):
 * Apply PlaybackParameters(speed, speed) with speed in [0.98f, 1.02f].
 * Setting speed == pitch engages SonicAudioProcessor's pure resampling mode,
 * avoiding time-stretching FFT/WSOLA phase artifacts while breaking fixed-cycle phase lock.
 */
object LoopFatigueHelper {

    const val MIN_SPEED_DRIFT = 0.98f
    const val MAX_SPEED_DRIFT = 1.02f

    // Fallback duration when asset duration cannot be determined (10 seconds)
    const val DEFAULT_FALLBACK_DURATION_MS = 10_000L

    // Minimum track duration to consider for random offset (1 second)
    const val MIN_DURATION_FOR_OFFSET_MS = 1_000L

    /**
     * Exact durations of the 30 bundled local OGG assets in milliseconds.
     * Measured from Vorbis stream granule positions.
     */
    val ASSET_DURATIONS_MS: Map<String, Long> = mapOf(
        "airplane" to 60_030L,
        "birds" to 129_805L,
        "boat" to 41_561L,
        "brown_noise" to 10_000L,
        "cat_purr" to 38_660L,
        "city" to 24_650L,
        "clock" to 14_440L,
        "coffee_shop" to 16_663L,
        "fan" to 14_090L,
        "fireplace" to 25_538L,
        "green_noise" to 28_500L,
        "keyboard" to 10_650L,
        "paper" to 17_210L,
        "pink_noise" to 9_959L,
        "rain" to 124_578L,
        "rain_car_roof" to 10_020L,
        "rain_roof" to 31_640L,
        "rain_umbrella" to 26_550L,
        "singing_bowl" to 49_080L,
        "storm" to 25_664L,
        "stream" to 145_529L,
        "summer_night" to 49_321L,
        "train" to 7_240L,
        "underwater" to 41_750L,
        "vinyl" to 65_900L,
        "walk_snow" to 36_670L,
        "waves" to 118_034L,
        "white_noise" to 20_016L,
        "wind" to 14_840L,
        "wind_chimes" to 81_510L
    )

    /**
     * Scheme B: Generate a micro-speed drift in [0.98f, 1.02f].
     * Passed into PlaybackParameters(speed, speed) to ensure pure resampling without frequency-domain artifacts.
     */
    fun calculateMicroSpeedDrift(randomFactor: Float = Random.nextFloat()): Float {
        val safeFactor = if (randomFactor.isNaN()) 0.5f else randomFactor.coerceIn(0f, 1f)
        return MIN_SPEED_DRIFT + safeFactor * (MAX_SPEED_DRIFT - MIN_SPEED_DRIFT)
    }

    /**
     * Creates PlaybackParameters in pure resampling mode (pitch == speed).
     */
    fun createDriftPlaybackParameters(speed: Float = calculateMicroSpeedDrift()): PlaybackParameters {
        val safeSpeed = if (speed.isNaN()) 1.0f else speed.coerceIn(MIN_SPEED_DRIFT, MAX_SPEED_DRIFT)
        return PlaybackParameters(safeSpeed, safeSpeed)
    }

    /**
     * Get effective duration in ms:
     * Prefers runtime player duration if positive and valid,
     * otherwise falls back to preset asset duration table,
     * or fallbackDurationMs (default 0L) if unknown.
     * Returning 0L signals to caller that duration is not yet known,
     * allowing initial seek to be safely deferred to Player.STATE_READY.
     */
    fun getEffectiveDuration(
        trackId: String,
        playerDurationMs: Long?,
        fallbackDurationMs: Long = 0L
    ): Long {
        if (playerDurationMs != null && playerDurationMs > 0L && playerDurationMs != C.TIME_UNSET) {
            return playerDurationMs
        }
        val assetDuration = ASSET_DURATIONS_MS[trackId]
        if (assetDuration != null && assetDuration > 0L) {
            return assetDuration
        }
        return fallbackDurationMs
    }

    /**
     * Scheme A: Calculate safe random start offset in milliseconds.
     * Prevents starting too close to the end (reserves 5% or 1-3s margin),
     * completely breaking the repetitive memory point at 00:00.000.
     */
    fun calculateRandomStartOffset(durationMs: Long, randomFactor: Float = Random.nextFloat()): Long {
        if (durationMs <= MIN_DURATION_FOR_OFFSET_MS) return 0L
        // Reserve an end margin: 5% of duration, clamped between 1000ms and 3000ms
        val endMargin = (durationMs * 0.05f).toLong().coerceIn(1_000L, 3_000L)
        val maxOffset = (durationMs - endMargin).coerceAtLeast(0L)
        if (maxOffset <= 0L) return 0L
        val safeFactor = if (randomFactor.isNaN()) 0.5f else randomFactor.coerceIn(0f, 1f)
        return (safeFactor * maxOffset).toLong().coerceIn(0L, maxOffset)
    }
}
