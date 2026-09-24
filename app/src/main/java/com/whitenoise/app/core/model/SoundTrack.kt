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
     * Category classification for matrix filtering.
     */
    val category: SoundCategory
        get() = SoundCategory.fromTrackId(id)
}
