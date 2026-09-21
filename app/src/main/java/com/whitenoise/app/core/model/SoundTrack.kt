package com.whitenoise.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SoundTrack(
    val id: String,
    val name: String,
    val subtitle: String,
    val assetFileName: String,
    val volume: Float = 0.5f,
    val isPlaying: Boolean = false,
    val isMuted: Boolean = false
) {
    /**
     * Compute effective volume accounting for track mute.
     */
    val effectiveTrackVolume: Float
        get() = if (isPlaying && !isMuted) volume.coerceIn(0f, 1f) else 0f

    /**
     * Associated Emoji representation for the sound track.
     */
    val iconEmoji: String
        get() = when (id) {
            "rain" -> "🌧️"
            "storm" -> "⛈️"
            "wind" -> "🌲"
            "stream" -> "🌊"
            "fireplace" -> "🪵"
            "birds" -> "🐦"
            "summer_night" -> "🦗"
            "white_noise" -> "📻"
            else -> "🎵"
        }
}
