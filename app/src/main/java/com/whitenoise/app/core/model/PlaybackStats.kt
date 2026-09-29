package com.whitenoise.app.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing aggregated playback statistics and companion metrics.
 *
 * @param totalSeconds Cumulative physical playback time across all active sessions in seconds.
 * @param trackSeconds Individual cumulative playback time for each sound track in seconds.
 * @param firstRecordTimestamp Timestamp of when playback was first recorded in milliseconds.
 */
@Serializable
data class PlaybackStats(
    val totalSeconds: Long = 0L,
    val trackSeconds: Map<String, Long> = emptyMap(),
    val firstRecordTimestamp: Long = 0L
) {
    /**
     * Total playback time in hours with 1 decimal precision.
     */
    val totalHours: Float
        get() = if (totalSeconds <= 0L) 0.0f else (totalSeconds / 360.0f).toInt() / 10.0f

    /**
     * Converted companion sleep nights based on standard 8 hours per night.
     */
    val sleepNights: Int
        get() = (totalSeconds / (8 * 3600L)).toInt()

    /**
     * Precise floating sleep nights (e.g. 1.5 nights).
     */
    val sleepNightsPrecise: Float
        get() = if (totalSeconds <= 0L) 0.0f else (totalSeconds / (8 * 360.0f)).toInt() / 10.0f

    /**
     * Formats total duration for large-number display (e.g. "48.5 小时" or "35 分钟").
     */
    fun getFormattedTotalDuration(): String {
        val hours = totalSeconds / 3600
        val remainingMinutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}小时${if (remainingMinutes > 0) "${remainingMinutes}分" else ""}"
            remainingMinutes > 0 -> "${remainingMinutes}分钟"
            totalSeconds > 0 -> "${totalSeconds}秒"
            else -> "0分钟"
        }
    }

    /**
     * Formats duration for an individual sound track in the rank list.
     */
    fun getFormattedTrackDuration(trackId: String): String {
        val seconds = trackSeconds[trackId] ?: 0L
        val hours = seconds / 3600
        val remainingMinutes = (seconds % 3600) / 60
        return when {
            hours > 0 && remainingMinutes > 0 -> "${hours}h ${remainingMinutes}m"
            hours > 0 -> "${hours}小时"
            remainingMinutes > 0 -> "${remainingMinutes}分钟"
            seconds > 0 -> "${seconds}秒"
            else -> "0分钟"
        }
    }

    /**
     * Returns a sorted list of tracks by playback duration descending,
     * filtering out tracks that have never been played.
     */
    fun getSortedTrackStats(): List<Pair<String, Long>> {
        return trackSeconds.entries
            .filter { it.value > 0L }
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    /**
     * Calculates the ratio of a track's playback time relative to the top track (0.0f .. 1.0f).
     */
    fun getTrackRatio(trackId: String): Float {
        val maxDuration = trackSeconds.values.maxOrNull() ?: return 0f
        if (maxDuration <= 0L) return 0f
        val currentDuration = trackSeconds[trackId] ?: 0L
        return (currentDuration.toFloat() / maxDuration.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Generates a poetic companion subtitle matching SaltAmbience's aesthetic tone.
     */
    fun getCompanionPoeticText(): String {
        return when {
            totalSeconds >= 80 * 3600L -> "星河长明，声息已陪伴你走过数十个沉睡之夜"
            totalSeconds >= 24 * 3600L -> "静水流深，自然之声在此与你长久相伴"
            totalSeconds >= 8 * 3600L -> "风声与梦，见证了属于你的宁静夜晚"
            totalSeconds > 0L -> "声波流淌，开启与自然初遇的静心时刻"
            else -> "戴上耳机，开启属于你的第一个安睡之夜"
        }
    }
}
