package com.whitenoise.app.ui.home

import com.whitenoise.app.core.model.SoundCategory

/**
 * Pure helper for sound category swipe navigation and boundary constraints.
 */
object CategoryNavigationHelper {

    /**
     * Returns the next category in order, or the current category if already at the last item.
     */
    fun getNextCategory(current: SoundCategory): SoundCategory {
        val index = SoundCategory.entries.indexOf(current)
        return if (index in 0 until SoundCategory.entries.lastIndex) {
            SoundCategory.entries[index + 1]
        } else {
            current
        }
    }

    /**
     * Returns the previous category in order, or the current category if already at the first item.
     */
    fun getPreviousCategory(current: SoundCategory): SoundCategory {
        val index = SoundCategory.entries.indexOf(current)
        return if (index > 0) {
            SoundCategory.entries[index - 1]
        } else {
            current
        }
    }

    /**
     * Resolves target category based on horizontal drag offset and fling velocity.
     * Negative drag/velocity indicates swiping left (advancing to next category).
     * Positive drag/velocity indicates swiping right (retreating to previous category).
     */
    fun resolveTargetCategory(
        current: SoundCategory,
        totalDragOffsetPx: Float,
        thresholdPx: Float,
        velocity: Float,
        velocityThreshold: Float = 800f
    ): SoundCategory {
        val shouldNext = totalDragOffsetPx < -thresholdPx || velocity < -velocityThreshold
        val shouldPrev = totalDragOffsetPx > thresholdPx || velocity > velocityThreshold

        return when {
            shouldNext -> getNextCategory(current)
            shouldPrev -> getPreviousCategory(current)
            else -> current
        }
    }
}
