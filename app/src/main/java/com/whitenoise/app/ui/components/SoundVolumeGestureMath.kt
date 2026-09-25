package com.whitenoise.app.ui.components

/**
 * Pure mathematical helper for card-level horizontal drag volume calculations.
 */
object SoundVolumeGestureMath {

    /**
     * Calculates the new volume based on initial volume, horizontal drag delta, and reference card width.
     * Full card width travel equals 100% volume adjustment.
     */
    fun calculateNewVolume(
        initialVolume: Float,
        dragDeltaXPx: Float,
        cardWidthPx: Float,
        sensitivity: Float = 1.0f
    ): Float {
        if (cardWidthPx <= 0f) return initialVolume.coerceIn(0f, 1f)
        val effectiveRange = cardWidthPx / sensitivity
        val deltaRatio = dragDeltaXPx / effectiveRange
        return (initialVolume + deltaRatio).coerceIn(0.0f, 1.0f)
    }

    /**
     * Detects when volume transitions into boundary (0.0 or 1.0) from a non-boundary state.
     */
    fun isHittingBoundary(
        previousVolume: Float,
        newVolume: Float
    ): Boolean {
        val hitMin = previousVolume > 0.001f && newVolume <= 0.001f
        val hitMax = previousVolume < 0.999f && newVolume >= 0.999f
        return hitMin || hitMax
    }
}
