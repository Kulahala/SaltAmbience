package com.whitenoise.app

import com.whitenoise.app.core.audio.VolumeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeCalculatorTest {

    @Test
    fun testNormalVolumeCalculation() {
        val actual = VolumeCalculator.calculateActualVolume(
            trackVolume = 0.8f,
            masterVolume = 0.5f,
            sleepFadeMultiplier = 1.0f,
            isDucked = false,
            isPlaying = true,
            isMuted = false
        )
        assertEquals(0.40f, actual, 0.001f)
    }

    @Test
    fun testMutedVolumeIsZero() {
        val actual = VolumeCalculator.calculateActualVolume(
            trackVolume = 0.8f,
            masterVolume = 1.0f,
            sleepFadeMultiplier = 1.0f,
            isDucked = false,
            isPlaying = true,
            isMuted = true
        )
        assertEquals(0.0f, actual, 0.0001f)
    }

    @Test
    fun testNotPlayingVolumeIsZero() {
        val actual = VolumeCalculator.calculateActualVolume(
            trackVolume = 0.8f,
            masterVolume = 1.0f,
            sleepFadeMultiplier = 1.0f,
            isDucked = false,
            isPlaying = false,
            isMuted = false
        )
        assertEquals(0.0f, actual, 0.0001f)
    }

    @Test
    fun testDuckingReducesVolume() {
        val normal = VolumeCalculator.calculateActualVolume(
            trackVolume = 0.8f,
            masterVolume = 1.0f,
            sleepFadeMultiplier = 1.0f,
            isDucked = false,
            isPlaying = true,
            isMuted = false
        )
        val ducked = VolumeCalculator.calculateActualVolume(
            trackVolume = 0.8f,
            masterVolume = 1.0f,
            sleepFadeMultiplier = 1.0f,
            isDucked = true,
            isPlaying = true,
            isMuted = false
        )
        assertEquals(0.8f, normal, 0.001f)
        assertEquals(0.8f * VolumeCalculator.DUCK_FACTOR, ducked, 0.001f)
        assertTrue("Ducked volume must be less than normal volume", ducked < normal)
    }

    @Test
    fun testSleepFadeMonotonicityAndSmoothness() {
        val fadeDuration = 30L

        val atStart = VolumeCalculator.calculateSleepFadeMultiplier(30L, fadeDuration)
        val atHalf = VolumeCalculator.calculateSleepFadeMultiplier(15L, fadeDuration)
        val atQuarter = VolumeCalculator.calculateSleepFadeMultiplier(7L, fadeDuration)
        val atZero = VolumeCalculator.calculateSleepFadeMultiplier(0L, fadeDuration)

        assertEquals(1.0f, atStart, 0.001f)
        assertEquals(0.25f, atHalf, 0.001f) // (15/30)^2 = 0.25
        assertEquals(0.0f, atZero, 0.0001f)

        assertTrue(atStart > atHalf)
        assertTrue(atHalf > atQuarter)
        assertTrue(atQuarter > atZero)
    }

    @Test
    fun testBoundaryClamping() {
        val over = VolumeCalculator.calculateActualVolume(
            trackVolume = 1.5f,
            masterVolume = 2.0f,
            sleepFadeMultiplier = 1.5f,
            isPlaying = true
        )
        assertEquals(1.0f, over, 0.0001f)

        val under = VolumeCalculator.calculateActualVolume(
            trackVolume = -0.5f,
            masterVolume = 1.0f,
            isPlaying = true
        )
        assertEquals(0.0f, under, 0.0001f)
    }

    @Test
    fun testDetermineFadeDuration() {
        assertEquals(5L, VolumeCalculator.determineFadeDuration(10L))
        assertEquals(20L, VolumeCalculator.determineFadeDuration(60L))
        // 30 mins (1800s): min(45, 1800/4 = 450) -> 45
        assertEquals(45L, VolumeCalculator.determineFadeDuration(1800L))
        // 60 mins (3600s): min(45, 3600/4 = 900) -> 45
        assertEquals(45L, VolumeCalculator.determineFadeDuration(3600L))
    }
}
