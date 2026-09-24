package com.whitenoise.app.core.model

/**
 * Sound categories for filtering the sound matrix in Bento grid.
 */
enum class SoundCategory(
    val title: String,
    val trackIds: Set<String>
) {
    ALL(
        title = "全部",
        trackIds = emptySet()
    ),
    RAIN(
        title = "雨水",
        trackIds = setOf("rain", "storm", "stream", "waves", "boat")
    ),
    NATURE(
        title = "自然",
        trackIds = setOf("wind", "fireplace", "birds", "summer_night")
    ),
    LIFE(
        title = "生活",
        trackIds = setOf("coffee_shop", "train", "city")
    ),
    NOISE(
        title = "纯噪",
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
