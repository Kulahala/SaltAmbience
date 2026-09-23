package com.whitenoise.app.core.model

/**
 * Sound categories for filtering the sound matrix in Bento grid.
 */
enum class SoundCategory(
    val title: String,
    val iconEmoji: String,
    val trackIds: Set<String>
) {
    ALL(
        title = "全部",
        iconEmoji = "✨",
        trackIds = emptySet()
    ),
    RAIN(
        title = "雨水",
        iconEmoji = "🌧️",
        trackIds = setOf("rain", "storm", "stream", "waves", "boat")
    ),
    NATURE(
        title = "自然",
        iconEmoji = "🌲",
        trackIds = setOf("wind", "fireplace", "birds", "summer_night")
    ),
    LIFE(
        title = "生活",
        iconEmoji = "☕",
        trackIds = setOf("coffee_shop", "train", "city")
    ),
    NOISE(
        title = "纯噪",
        iconEmoji = "🌊",
        trackIds = setOf("white_noise", "pink_noise", "brown_noise")
    );

    fun matches(trackId: String): Boolean {
        return this == ALL || trackId in trackIds
    }

    companion object {
        fun fromTrackId(trackId: String): SoundCategory {
            return entries.firstOrNull { it != ALL && it.matches(trackId) } ?: ALL
        }
    }
}
