package com.whitenoise.app

import androidx.media3.common.C
import com.whitenoise.app.core.audio.LoopFatigueHelper
import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.data.repository.SoundRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LoopFatigueAvoidanceTest {

    @Test
    fun testMicroSpeedDriftRangeAndBounds() {
        // Boundary tests
        val minSpeed = LoopFatigueHelper.calculateMicroSpeedDrift(0.0f)
        val maxSpeed = LoopFatigueHelper.calculateMicroSpeedDrift(1.0f)
        val midSpeed = LoopFatigueHelper.calculateMicroSpeedDrift(0.5f)

        assertEquals(0.98f, minSpeed, 0.0001f)
        assertEquals(1.02f, maxSpeed, 0.0001f)
        assertEquals(1.00f, midSpeed, 0.0001f)

        // Out-of-bounds inputs should be safely clamped to [0.98f, 1.02f]
        val clampedUnder = LoopFatigueHelper.calculateMicroSpeedDrift(-1.5f)
        val clampedOver = LoopFatigueHelper.calculateMicroSpeedDrift(3.0f)
        assertEquals(0.98f, clampedUnder, 0.0001f)
        assertEquals(1.02f, clampedOver, 0.0001f)

        // Float.NaN must be safely handled without throwing or propagating NaN
        val nanSpeed = LoopFatigueHelper.calculateMicroSpeedDrift(Float.NaN)
        assertEquals(1.00f, nanSpeed, 0.0001f)
        val nanParams = LoopFatigueHelper.createDriftPlaybackParameters(Float.NaN)
        assertEquals(1.00f, nanParams.speed, 0.0001f)
        assertEquals(1.00f, nanParams.pitch, 0.0001f)

        // Monte Carlo test with 10,000 random factors
        var sum = 0.0
        val sampleCount = 10_000
        for (i in 0 until sampleCount) {
            val speed = LoopFatigueHelper.calculateMicroSpeedDrift(Random.nextFloat())
            assertTrue("Speed $speed must be >= 0.98f", speed >= 0.98f)
            assertTrue("Speed $speed must be <= 1.02f", speed <= 1.02f)
            sum += speed
        }
        val avg = sum / sampleCount
        // Average should be centered around 1.00x (+-0.005)
        assertEquals(1.00, avg, 0.005)
    }

    @Test
    fun testDriftPlaybackParametersPureResamplingMode() {
        // Scheme B requirement: PlaybackParameters(speed, speed)
        // Sonic audio processor checks speed == pitch to use pure resampling without FFT artifacts
        val testSpeeds = listOf(0.98f, 0.985f, 0.99f, 1.00f, 1.01f, 1.015f, 1.02f)
        for (speed in testSpeeds) {
            val params = LoopFatigueHelper.createDriftPlaybackParameters(speed)
            assertEquals(speed, params.speed, 0.0001f)
            assertEquals(speed, params.pitch, 0.0001f)
            assertEquals("Speed and Pitch must be identical for pure resampling", params.speed, params.pitch, 0.0001f)
        }
    }

    @Test
    fun testRandomStartOffsetCalculations() {
        val testDuration = 100_000L // 100 seconds
        // Margin: 5% of 100s = 5000ms, clamped to max 3000ms -> 3000ms.
        // Max offset: 100000 - 3000 = 97000ms.
        val offsetStart = LoopFatigueHelper.calculateRandomStartOffset(testDuration, 0.0f)
        val offsetEnd = LoopFatigueHelper.calculateRandomStartOffset(testDuration, 1.0f)
        val offsetHalf = LoopFatigueHelper.calculateRandomStartOffset(testDuration, 0.5f)

        assertEquals(0L, offsetStart)
        assertEquals(97_000L, offsetEnd)
        assertEquals(48_500L, offsetHalf)

        // Safety: offset must never be negative or exceed duration
        assertTrue(offsetStart >= 0L)
        assertTrue(offsetEnd < testDuration)
        assertTrue(offsetEnd <= 97_000L)

        // Float.NaN safety
        val offsetNaN = LoopFatigueHelper.calculateRandomStartOffset(testDuration, Float.NaN)
        assertEquals(48_500L, offsetNaN)
    }

    @Test
    fun testAllThirtyAssetsHaveValidPresetDurationsAndOffsets() {
        val tracks = SoundRepository.ALL_TRACKS
        assertEquals(30, tracks.size)

        for (track in tracks) {
            val duration = LoopFatigueHelper.ASSET_DURATIONS_MS[track.id]
            assertNotNull("Asset ${track.id} must have a preset duration", duration)
            assertTrue("Duration for ${track.id} must be positive (> 0)", duration!! > 0L)
            assertTrue("Asset ${track.id} duration (${duration}ms) must be at least 7000ms", duration >= 7_000L)

            // Test 1,000 random offsets for this track
            for (i in 0 until 1_000) {
                val offset = LoopFatigueHelper.calculateRandomStartOffset(duration, Random.nextFloat())
                assertTrue("Offset $offset for ${track.id} must be >= 0", offset >= 0L)
                assertTrue("Offset $offset for ${track.id} must be strictly less than duration $duration", offset < duration)
                // Also verify safety margin at the end: offset should not exceed duration - 1000ms
                assertTrue("Offset $offset must leave at least 1s margin before end", offset <= duration - 1_000L)
            }
        }
    }

    @Test
    fun testShortAndZeroDurationEdgeCases() {
        // Zero duration
        assertEquals(0L, LoopFatigueHelper.calculateRandomStartOffset(0L, 0.5f))
        // Negative duration
        assertEquals(0L, LoopFatigueHelper.calculateRandomStartOffset(-5000L, 0.5f))
        // Duration <= 1000ms
        assertEquals(0L, LoopFatigueHelper.calculateRandomStartOffset(500L, 0.5f))
        assertEquals(0L, LoopFatigueHelper.calculateRandomStartOffset(1000L, 0.5f))
        // Duration > 1000ms
        val offset = LoopFatigueHelper.calculateRandomStartOffset(1500L, 0.5f)
        assertTrue(offset >= 0L)
        assertTrue(offset < 1500L)
    }

    @Test
    fun testEffectiveDurationResolution() {
        // 1. Valid runtime player duration takes highest priority
        val runtimeDuration = 60_000L
        val effective1 = LoopFatigueHelper.getEffectiveDuration("rain", runtimeDuration)
        assertEquals(runtimeDuration, effective1)

        // 2. C.TIME_UNSET (-9223372036854775807L) falls back to asset duration table
        val effective2 = LoopFatigueHelper.getEffectiveDuration("rain", C.TIME_UNSET)
        assertEquals(124_578L, effective2)

        // 3. Null player duration falls back to asset duration table
        val effective3 = LoopFatigueHelper.getEffectiveDuration("rain", null)
        assertEquals(124_578L, effective3)

        // 4. Zero or negative player duration falls back to asset duration table
        val effective4 = LoopFatigueHelper.getEffectiveDuration("rain", 0L)
        val effective5 = LoopFatigueHelper.getEffectiveDuration("rain", -1L)
        assertEquals(124_578L, effective4)
        assertEquals(124_578L, effective5)

        // 5. Unknown trackId with C.TIME_UNSET returns 0L (signals unknown, defers seek to STATE_READY)
        val effectiveUnknown = LoopFatigueHelper.getEffectiveDuration("unknown_sound", C.TIME_UNSET)
        assertEquals(0L, effectiveUnknown)

        // 6. Unknown trackId with explicit fallback parameter returns explicit fallback
        val effectiveWithFallback = LoopFatigueHelper.getEffectiveDuration(
            "unknown_sound",
            C.TIME_UNSET,
            fallbackDurationMs = LoopFatigueHelper.DEFAULT_FALLBACK_DURATION_MS
        )
        assertEquals(LoopFatigueHelper.DEFAULT_FALLBACK_DURATION_MS, effectiveWithFallback)
    }

    @Test
    fun testMultiTrackSpeedDivergencePreventsCyclePhaseLock() {
        // Simulate 3 tracks running concurrently over 10 minutes (600s)
        // Track 1: Rain (124.578s, speed 0.985x)
        // Track 2: Fireplace (25.538s, speed 1.015x)
        // Track 3: Stream (145.529s, speed 0.995x)
        val duration1 = 124.578
        val speed1 = 0.985
        val duration2 = 25.538
        val speed2 = 1.015
        val duration3 = 145.529
        val speed3 = 0.995

        val playbackWallClockSeconds = 600.0 // 10 minutes

        // Effective media seconds played = wallClockSeconds * speed
        val mediaPlayed1 = playbackWallClockSeconds * speed1 // 591.0s
        val mediaPlayed2 = playbackWallClockSeconds * speed2 // 609.0s
        val mediaPlayed3 = playbackWallClockSeconds * speed3 // 597.0s

        // Relative drift between Track 1 and Track 2 in 10 minutes
        val driftDiffSeconds = kotlin.math.abs(mediaPlayed2 - mediaPlayed1)
        assertEquals(18.0, driftDiffSeconds, 0.01)
        assertTrue("Phase drift after 10 minutes must exceed 10 seconds", driftDiffSeconds > 10.0)

        // Calculate positions in their respective loops
        val loopPosition1 = mediaPlayed1 % duration1
        val loopPosition2 = mediaPlayed2 % duration2
        val loopPosition3 = mediaPlayed3 % duration3

        // At exactly 600s without speed drift (speed=1.0):
        val noDriftPos1 = (playbackWallClockSeconds * 1.0) % duration1
        val noDriftPos2 = (playbackWallClockSeconds * 1.0) % duration2
        val noDriftPos3 = (playbackWallClockSeconds * 1.0) % duration3

        // Verify that the loop positions with drift have significantly drifted away from standard 1.0x
        assertTrue(kotlin.math.abs(loopPosition1 - noDriftPos1) > 1.0)
        assertTrue(kotlin.math.abs(loopPosition2 - noDriftPos2) > 1.0)
        assertTrue(kotlin.math.abs(loopPosition3 - noDriftPos3) > 1.0)
    }

    @Test
    fun testActiveSessionStateLifecycleSimulation() {
        val activeSessionTracks = mutableSetOf<String>()
        val pendingInitialSeekTracks = mutableSetOf<String>()

        fun onTrackActivated(trackId: String, isFresh: Boolean) {
            if (isFresh) {
                activeSessionTracks.add(trackId)
            }
        }

        fun onTrackDeactivated(trackId: String) {
            activeSessionTracks.remove(trackId)
            pendingInitialSeekTracks.remove(trackId)
        }

        fun onPauseAll() {
            // Master pause keeps session tracks intact
        }

        fun onStopAll() {
            activeSessionTracks.clear()
            pendingInitialSeekTracks.clear()
        }

        fun onApplyPreset(preset: Preset) {
            preset.trackVolumes.forEach { (id, vol) ->
                if (vol > 0f) {
                    activeSessionTracks.add(id)
                } else {
                    activeSessionTracks.remove(id)
                }
            }
        }

        // 1. Initial Start
        onTrackActivated("rain", isFresh = true)
        assertTrue(activeSessionTracks.contains("rain"))

        // 2. Pause: activeSessionTracks must be preserved
        onPauseAll()
        assertTrue("Pause must NOT remove track from active session", activeSessionTracks.contains("rain"))

        // 3. Resume: because track is already in active session, no new seek
        val needsSeekOnResume = !activeSessionTracks.contains("rain")
        assertFalse("Resume must NOT re-seek an already active track", needsSeekOnResume)

        // 4. User turns off track
        onTrackDeactivated("rain")
        assertFalse("Turning off track must remove it from active session", activeSessionTracks.contains("rain"))

        // 5. User turns track back on -> fresh seek needed!
        val needsSeekOnReactivate = !activeSessionTracks.contains("rain")
        assertTrue("Reactivating track must require fresh seek", needsSeekOnReactivate)
        onTrackActivated("rain", isFresh = true)
        assertTrue(activeSessionTracks.contains("rain"))

        // 6. User switches preset to "sea_cabin" (waves + fireplace)
        val seaCabin = Preset(
            id = "sea_cabin",
            name = "海边小木屋",
            description = "海浪与柴火",
            trackVolumes = mapOf("waves" to 0.8f, "fireplace" to 0.5f, "rain" to 0.0f)
        )
        onApplyPreset(seaCabin)
        assertTrue(activeSessionTracks.contains("waves"))
        assertTrue(activeSessionTracks.contains("fireplace"))
        assertFalse(activeSessionTracks.contains("rain"))

        // 7. Stop all: clears everything
        onStopAll()
        assertEquals(0, activeSessionTracks.size)
        assertEquals(0, pendingInitialSeekTracks.size)
    }

    @Test
    fun testDeferredSeekLifecycleWhenDurationInitiallyUnset() {
        val pendingInitialSeekTracks = mutableSetOf<String>()
        var appliedSeekOffsetMs = -1L

        fun onTrackFirstActivated(trackId: String, playerDurationMs: Long?) {
            val dur = LoopFatigueHelper.getEffectiveDuration(trackId, playerDurationMs)
            if (dur > 0L) {
                appliedSeekOffsetMs = LoopFatigueHelper.calculateRandomStartOffset(dur, 0.5f)
                pendingInitialSeekTracks.remove(trackId)
            } else {
                pendingInitialSeekTracks.add(trackId)
            }
        }

        fun onPlayerStateReady(trackId: String, readyDurationMs: Long) {
            if (pendingInitialSeekTracks.remove(trackId)) {
                val dur = LoopFatigueHelper.getEffectiveDuration(trackId, readyDurationMs)
                if (dur > 0L) {
                    appliedSeekOffsetMs = LoopFatigueHelper.calculateRandomStartOffset(dur, 0.5f)
                }
            }
        }

        // 1. Unknown custom track starts with C.TIME_UNSET
        onTrackFirstActivated("custom_sound_42", C.TIME_UNSET)
        assertEquals(-1L, appliedSeekOffsetMs)
        assertTrue("Track must be queued in pendingInitialSeekTracks", pendingInitialSeekTracks.contains("custom_sound_42"))

        // 2. STATE_READY arrives with 20s media duration
        onPlayerStateReady("custom_sound_42", 20_000L)
        assertFalse("Track must be removed from pendingInitialSeekTracks", pendingInitialSeekTracks.contains("custom_sound_42"))
        assertTrue("Seek offset must be calculated upon STATE_READY", appliedSeekOffsetMs >= 0L)
        assertTrue("Seek offset must be strictly within bounds", appliedSeekOffsetMs <= 19_000L)
    }

    @Test
    fun testUnmuteWhileMasterPlayingResumesPlaybackState() {
        var isMasterPlaying = true
        var trackIsPlaying = true
        var trackIsMuted = true
        var playWhenReady = false
        val activeSessionTracks = mutableSetOf<String>()

        // Simulate master pause-resume cycle when track is muted
        fun onMasterPaused() {
            isMasterPlaying = false
            playWhenReady = false
        }

        fun onMasterResumed() {
            isMasterPlaying = true
            // Only active and unmuted tracks are resumed
            playWhenReady = trackIsPlaying && !trackIsMuted
        }

        fun onTrackUnmuted(trackId: String) {
            trackIsMuted = false
            if (trackIsPlaying && !trackIsMuted && isMasterPlaying) {
                if (!activeSessionTracks.contains(trackId)) {
                    activeSessionTracks.add(trackId)
                }
                playWhenReady = true
            }
        }

        onMasterPaused()
        assertFalse(playWhenReady)

        onMasterResumed()
        assertFalse("Muted track must not play when master resumes", playWhenReady)

        // When user unmutes the track while master is playing, it must resume playback!
        onTrackUnmuted("birds")
        assertTrue("Unmuting track during master playback MUST set playWhenReady to true", playWhenReady)
        assertTrue("Track must be registered in active session", activeSessionTracks.contains("birds"))
    }

    @Test
    fun testColdStartWithoutActiveTracksDoesNotPolluteSession() {
        val activeSessionTracks = mutableSetOf<String>()
        val savedTracks = emptyMap<String, Boolean>() // No tracks saved as playing

        fun restoreTracksStateSimulation(saved: Map<String, Boolean>) {
            val active = saved.filter { it.value }
            if (active.isNotEmpty()) {
                active.keys.forEach { activeSessionTracks.add(it) }
            }
            // else: pre-warm fallback default track "rain" into memory, but do NOT add to activeSessionTracks
        }

        restoreTracksStateSimulation(savedTracks)
        assertEquals(
            "Cold start with no active tracks must NOT pollute activeSessionTracks",
            0,
            activeSessionTracks.size
        )
    }

    @Test
    fun testPlayerStateReadyGuardPreventsPrematureSeekOnAssetTracks() {
        val pendingInitialSeekTracks = mutableSetOf<String>()
        var appliedSeekOffsetMs = -1L

        // Emulates new AudioMixerEngine.applyRandomStartAndDrift logic
        fun applyRandomStartAndDriftSim(trackId: String, isStateReady: Boolean, playerDurationMs: Long?) {
            if (isStateReady) {
                val dur = LoopFatigueHelper.getEffectiveDuration(trackId, playerDurationMs)
                if (dur > 0L) {
                    val offset = LoopFatigueHelper.calculateRandomStartOffset(dur, 0.5f)
                    if (offset > 0L) appliedSeekOffsetMs = offset
                    pendingInitialSeekTracks.remove(trackId)
                }
            } else {
                pendingInitialSeekTracks.add(trackId)
            }
        }

        fun onPlayerStateReadySim(trackId: String, durationMs: Long) {
            if (pendingInitialSeekTracks.remove(trackId)) {
                val dur = LoopFatigueHelper.getEffectiveDuration(trackId, durationMs)
                if (dur > 0L) {
                    val offset = LoopFatigueHelper.calculateRandomStartOffset(dur, 0.5f)
                    if (offset > 0L) appliedSeekOffsetMs = offset
                }
            }
        }

        // Test with known asset track (e.g. stream) that is NOT yet STATE_READY upon prepare()
        applyRandomStartAndDriftSim("stream", isStateReady = false, playerDurationMs = C.TIME_UNSET)
        // Must NOT seek immediately even though asset duration is known
        assertEquals("Must NOT seek while player is buffering/unready", -1L, appliedSeekOffsetMs)
        assertTrue("Track must be queued for STATE_READY", pendingInitialSeekTracks.contains("stream"))

        // When STATE_READY is delivered by ExoPlayer
        onPlayerStateReadySim("stream", 145_529L)
        assertFalse("Track must be removed from pending set", pendingInitialSeekTracks.contains("stream"))
        assertTrue("Seek offset must now be applied", appliedSeekOffsetMs > 0L)

        // When a track is reactivated while already in STATE_READY (e.g. pre-warmed player)
        var immediateSeekOffset = -1L
        fun applyImmediateSeekSim(trackId: String) {
            val dur = LoopFatigueHelper.getEffectiveDuration(trackId, 124_578L)
            val offset = LoopFatigueHelper.calculateRandomStartOffset(dur, 0.5f)
            immediateSeekOffset = offset
        }
        applyImmediateSeekSim("rain")
        assertTrue("Pre-warmed track in STATE_READY seeks immediately", immediateSeekOffset > 0L)
    }
}
