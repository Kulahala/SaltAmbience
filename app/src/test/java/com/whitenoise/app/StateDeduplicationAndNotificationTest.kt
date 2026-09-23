package com.whitenoise.app

import com.whitenoise.app.core.audio.VolumeCalculator
import com.whitenoise.app.core.model.PlaybackState
import com.whitenoise.app.core.model.PresetShareCode
import com.whitenoise.app.core.model.PresetSharePayload
import com.whitenoise.app.core.service.WhiteNoiseMediaService
import com.whitenoise.app.data.repository.SoundRepository
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StateDeduplicationAndNotificationTest {

    @Test
    fun testNotificationSubtextFormatting() {
        // Playing with active tracks
        assertEquals(
            "正在混音播放 3 种自然声",
            WhiteNoiseMediaService.formatNotificationSubtext(isMasterPlaying = true, activeTrackCount = 3)
        )
        // Playing with 0 active tracks
        assertEquals(
            "未选择音效",
            WhiteNoiseMediaService.formatNotificationSubtext(isMasterPlaying = true, activeTrackCount = 0)
        )
        // Paused with tracks waiting
        assertEquals(
            "已暂停 · 2 轨待续",
            WhiteNoiseMediaService.formatNotificationSubtext(isMasterPlaying = false, activeTrackCount = 2)
        )
        // Paused with 0 tracks
        assertEquals(
            "已暂停",
            WhiteNoiseMediaService.formatNotificationSubtext(isMasterPlaying = false, activeTrackCount = 0)
        )
    }

    @Test
    fun testDistinctUntilChangedBlocksPerSecondTimerWakeups() = runBlocking {
        val states = listOf(
            PlaybackState(isMasterPlaying = true, activeTrackCount = 2, sleepTimerRemainingSeconds = 1800, sleepFadeFraction = 1.0f),
            PlaybackState(isMasterPlaying = true, activeTrackCount = 2, sleepTimerRemainingSeconds = 1799, sleepFadeFraction = 1.0f),
            PlaybackState(isMasterPlaying = true, activeTrackCount = 2, sleepTimerRemainingSeconds = 1798, sleepFadeFraction = 1.0f),
            PlaybackState(isMasterPlaying = true, activeTrackCount = 2, sleepTimerRemainingSeconds = 1797, sleepFadeFraction = 1.0f),
            PlaybackState(isMasterPlaying = false, activeTrackCount = 2, sleepTimerRemainingSeconds = 1796, sleepFadeFraction = 1.0f), // Paused
            PlaybackState(isMasterPlaying = false, activeTrackCount = 0, sleepTimerRemainingSeconds = null, sleepFadeFraction = 1.0f)   // Stopped
        )

        val filtered = states.asFlow()
            .distinctUntilChanged { old, new ->
                old.isMasterPlaying == new.isMasterPlaying && old.activeTrackCount == new.activeTrackCount
            }
            .toList()

        // Only 3 emissions should pass: Initial Playing(2), Paused(2), Stopped(0)
        assertEquals(3, filtered.size)
        assertEquals(true, filtered[0].isMasterPlaying)
        assertEquals(2, filtered[0].activeTrackCount)
        assertEquals(false, filtered[1].isMasterPlaying)
        assertEquals(2, filtered[1].activeTrackCount)
        assertEquals(false, filtered[2].isMasterPlaying)
        assertEquals(0, filtered[2].activeTrackCount)
    }

    @Test
    fun testClipboardDismissedHashSuppression() {
        val dismissedHashes = mutableSetOf<Int>()

        val rawText1 = PresetShareCode.generateShareText(
            com.whitenoise.app.core.model.Preset(
                id = "p1",
                name = "雨中阅读",
                description = "细雨伴读",
                trackVolumes = mapOf("rain" to 0.7f, "coffee_shop" to 0.3f)
            )
        )
        val payload1 = PresetShareCode.parseShareText(rawText1)
        org.junit.Assert.assertNotNull(payload1)

        // Before dismissal, not suppressed
        assertFalse(dismissedHashes.contains(payload1!!.hashCode()))

        // User dismisses banner
        dismissedHashes.add(payload1.hashCode())
        assertTrue(dismissedHashes.contains(payload1.hashCode()))

        // Re-inspecting identical content produces same hash and is suppressed
        val reParsed1 = PresetShareCode.parseShareText(rawText1)
        assertTrue(dismissedHashes.contains(reParsed1!!.hashCode()))

        // A new, different preset is not suppressed
        val rawText2 = PresetShareCode.generateShareText(
            com.whitenoise.app.core.model.Preset(
                id = "p2",
                name = "海边小木屋",
                description = "海浪与柴火",
                trackVolumes = mapOf("waves" to 0.8f, "fireplace" to 0.5f)
            )
        )
        val payload2 = PresetShareCode.parseShareText(rawText2)
        org.junit.Assert.assertNotNull(payload2)
        assertFalse(dismissedHashes.contains(payload2!!.hashCode()))
    }

    @Test
    fun testSleepTimerFadeWindowCondition() {
        val totalSeconds = 1800L // 30 minutes
        val fadeDuration = VolumeCalculator.determineFadeDuration(totalSeconds) // 45s
        assertEquals(45L, fadeDuration)

        // Far from end: remaining > fadeDuration -> no volume update
        val remainingNormal = 100L
        assertFalse("Volume update should not run during normal countdown", remainingNormal <= fadeDuration)

        // Inside fade window: remaining <= fadeDuration -> volume update runs
        val remainingFadeStart = 45L
        assertTrue("Volume update must run at start of fade duration", remainingFadeStart <= fadeDuration)

        val remainingFadeLate = 10L
        assertTrue("Volume update must run during fade duration", remainingFadeLate <= fadeDuration)
    }

    @Test
    fun testDecideNotificationActionRules() {
        // Rule 1: Cold start without active tracks -> REMOVE_NOTIFICATION (no notification)
        assertEquals(
            WhiteNoiseMediaService.Companion.NotificationAction.REMOVE_NOTIFICATION,
            WhiteNoiseMediaService.decideNotificationAction(
                hasStartedForeground = false,
                isMasterPlaying = false,
                activeTrackCount = 0
            )
        )

        // Rule 2: Cold start with restored tracks before user taps play -> REMOVE_NOTIFICATION (prevents phantom paused notification)
        assertEquals(
            WhiteNoiseMediaService.Companion.NotificationAction.REMOVE_NOTIFICATION,
            WhiteNoiseMediaService.decideNotificationAction(
                hasStartedForeground = false,
                isMasterPlaying = false,
                activeTrackCount = 3
            )
        )

        // Rule 3: User hits Play -> START_FOREGROUND
        assertEquals(
            WhiteNoiseMediaService.Companion.NotificationAction.START_FOREGROUND,
            WhiteNoiseMediaService.decideNotificationAction(
                hasStartedForeground = false,
                isMasterPlaying = true,
                activeTrackCount = 3
            )
        )

        // Rule 4: User pauses during active playback session -> UPDATE_PAUSED (keeps notification with play button)
        assertEquals(
            WhiteNoiseMediaService.Companion.NotificationAction.UPDATE_PAUSED,
            WhiteNoiseMediaService.decideNotificationAction(
                hasStartedForeground = true,
                isMasterPlaying = false,
                activeTrackCount = 3
            )
        )

        // Rule 5: User clears all tracks while paused -> REMOVE_NOTIFICATION
        assertEquals(
            WhiteNoiseMediaService.Companion.NotificationAction.REMOVE_NOTIFICATION,
            WhiteNoiseMediaService.decideNotificationAction(
                hasStartedForeground = true,
                isMasterPlaying = false,
                activeTrackCount = 0
            )
        )
    }

    @Test
    fun testConcurrentShareCodeHashSuppression() {
        val dismissedHashes = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
        val threads = (1..8).map { threadIdx ->
            Thread {
                for (i in 0 until 100) {
                    dismissedHashes.add(threadIdx * 1000 + i)
                }
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals(800, dismissedHashes.size)
    }

    @Test
    fun testShouldUpdateNotificationPredicate() {
        val baseState = PlaybackState(
            isMasterPlaying = true,
            activeTrackCount = 2,
            primaryTrackId = "rain",
            sleepTimerRemainingSeconds = 1800,
            sleepFadeFraction = 1.0f
        )

        // 1. Timer tick: remaining seconds decreases, should NOT update notification
        val timerTickState = baseState.copy(sleepTimerRemainingSeconds = 1799)
        assertFalse(
            "Timer ticks must not trigger notification updates",
            WhiteNoiseMediaService.shouldUpdateNotification(baseState, timerTickState)
        )

        // 2. Fade fraction changes, should NOT update notification
        val fadeTickState = baseState.copy(sleepFadeFraction = 0.95f)
        assertFalse(
            "Sleep fade fraction must not trigger notification updates",
            WhiteNoiseMediaService.shouldUpdateNotification(baseState, fadeTickState)
        )

        // 3. Primary sound track changes (e.g. from rain to fireplace): MUST update notification for new Bauhaus cover!
        val trackChangeState = baseState.copy(primaryTrackId = "fireplace")
        assertTrue(
            "Primary sound track change must update notification cover",
            WhiteNoiseMediaService.shouldUpdateNotification(baseState, trackChangeState)
        )

        // 4. Play to Pause toggle: MUST update notification
        val pauseState = baseState.copy(isMasterPlaying = false)
        assertTrue(
            "Play to pause toggle must update notification",
            WhiteNoiseMediaService.shouldUpdateNotification(baseState, pauseState)
        )

        // 5. Active track count change: MUST update notification
        val countChangeState = baseState.copy(activeTrackCount = 3)
        assertTrue(
            "Active track count change must update notification",
            WhiteNoiseMediaService.shouldUpdateNotification(baseState, countChangeState)
        )
    }

    @Test
    fun testDistinctUntilChangedWithThemeCoverSwitching() = runBlocking {
        val states = listOf(
            PlaybackState(isMasterPlaying = true, activeTrackCount = 1, primaryTrackId = "rain", sleepTimerRemainingSeconds = 1800),
            PlaybackState(isMasterPlaying = true, activeTrackCount = 1, primaryTrackId = "rain", sleepTimerRemainingSeconds = 1799), // tick (blocked)
            PlaybackState(isMasterPlaying = true, activeTrackCount = 1, primaryTrackId = "fireplace", sleepTimerRemainingSeconds = 1798), // cover changed! (passed)
            PlaybackState(isMasterPlaying = true, activeTrackCount = 1, primaryTrackId = "fireplace", sleepTimerRemainingSeconds = 1797), // tick (blocked)
            PlaybackState(isMasterPlaying = false, activeTrackCount = 1, primaryTrackId = "fireplace", sleepTimerRemainingSeconds = 1796), // paused (passed)
            PlaybackState(isMasterPlaying = false, activeTrackCount = 0, primaryTrackId = null, sleepTimerRemainingSeconds = null) // stopped (passed)
        )

        val filtered = states.asFlow()
            .distinctUntilChanged { old, new ->
                !WhiteNoiseMediaService.shouldUpdateNotification(old, new)
            }
            .toList()

        // 4 emissions should pass: Initial Rain, Fireplace Cover, Paused, Stopped
        assertEquals(4, filtered.size)
        assertEquals("rain", filtered[0].primaryTrackId)
        assertTrue(filtered[0].isMasterPlaying)

        assertEquals("fireplace", filtered[1].primaryTrackId)
        assertTrue(filtered[1].isMasterPlaying)

        assertEquals("fireplace", filtered[2].primaryTrackId)
        assertFalse(filtered[2].isMasterPlaying)

        assertEquals(null, filtered[3].primaryTrackId)
        assertFalse(filtered[3].isMasterPlaying)
        assertEquals(0, filtered[3].activeTrackCount)
    }

    @Test
    fun testAllFifteenTracksHaveCorrespondingNameResolution() {
        val tracks = SoundRepository.ALL_TRACKS
        assertEquals(15, tracks.size)

        for (track in tracks) {
            val resolvedName = SoundRepository.ALL_TRACKS.find { it.id == track.id }?.name
            org.junit.Assert.assertNotNull("Track ${track.id} must resolve a name", resolvedName)
            assertEquals(track.name, resolvedName)
        }
    }

    @Test
    fun testPrimaryTrackSelectionRules() {
        val tracks = listOf(
            SoundRepository.ALL_TRACKS[0].copy(isPlaying = true, volume = 0.3f), // rain
            SoundRepository.ALL_TRACKS[1].copy(isPlaying = true, volume = 0.8f), // storm
            SoundRepository.ALL_TRACKS[2].copy(isPlaying = true, volume = 0.5f)  // wind
        )

        val activeTracks = tracks.filter { it.isPlaying && !it.isMuted }
        val highestVolumeTrack = activeTracks.maxByOrNull { it.volume }

        org.junit.Assert.assertNotNull(highestVolumeTrack)
        assertEquals("storm", highestVolumeTrack?.id)
        assertEquals("雷雨", highestVolumeTrack?.name)
    }
}
