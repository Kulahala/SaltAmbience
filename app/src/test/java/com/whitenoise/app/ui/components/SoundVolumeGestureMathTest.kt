package com.whitenoise.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundVolumeGestureMathTest {

    @Test
    fun calculateNewVolume_zeroDelta_retainsVolume() {
        val result = SoundVolumeGestureMath.calculateNewVolume(
            initialVolume = 0.5f,
            dragDeltaXPx = 0f,
            cardWidthPx = 400f
        )
        assertEquals(0.5f, result, 0.001f)
    }

    @Test
    fun calculateNewVolume_positiveDelta_increasesVolume() {
        val result = SoundVolumeGestureMath.calculateNewVolume(
            initialVolume = 0.5f,
            dragDeltaXPx = 100f,
            cardWidthPx = 400f
        )
        assertEquals(0.75f, result, 0.001f)
    }

    @Test
    fun calculateNewVolume_negativeDelta_decreasesVolume() {
        val result = SoundVolumeGestureMath.calculateNewVolume(
            initialVolume = 0.5f,
            dragDeltaXPx = -100f,
            cardWidthPx = 400f
        )
        assertEquals(0.25f, result, 0.001f)
    }

    @Test
    fun calculateNewVolume_clampedAtMinimumZero() {
        val result = SoundVolumeGestureMath.calculateNewVolume(
            initialVolume = 0.2f,
            dragDeltaXPx = -200f,
            cardWidthPx = 400f
        )
        assertEquals(0.0f, result, 0.001f)
    }

    @Test
    fun calculateNewVolume_clampedAtMaximumOne() {
        val result = SoundVolumeGestureMath.calculateNewVolume(
            initialVolume = 0.8f,
            dragDeltaXPx = 200f,
            cardWidthPx = 400f
        )
        assertEquals(1.0f, result, 0.001f)
    }

    @Test
    fun isHittingBoundary_detectsMinBoundaryHit() {
        assertTrue(SoundVolumeGestureMath.isHittingBoundary(0.05f, 0.0f))
        assertFalse(SoundVolumeGestureMath.isHittingBoundary(0.0f, 0.0f))
    }

    @Test
    fun isHittingBoundary_detectsMaxBoundaryHit() {
        assertTrue(SoundVolumeGestureMath.isHittingBoundary(0.95f, 1.0f))
        assertFalse(SoundVolumeGestureMath.isHittingBoundary(1.0f, 1.0f))
    }
}
