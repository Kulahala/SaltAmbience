package com.whitenoise.app

import com.whitenoise.app.core.model.PlaybackStats
import com.whitenoise.app.core.service.PlaybackStatsTracker
import com.whitenoise.app.ui.components.getTrackIndividualColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStatsTrackingTest {

    @Test
    fun testZeroPlaybackStatsDefaults() {
        val stats = PlaybackStats(
            totalSeconds = 0L,
            trackSeconds = emptyMap(),
            firstRecordTimestamp = 0L
        )

        assertEquals(0.0f, stats.totalHours, 0.001f)
        assertEquals(0, stats.sleepNights)
        assertEquals("0分钟", stats.getFormattedTotalDuration())
        assertEquals("戴上耳机，开启属于你的第一个安睡之夜", stats.getCompanionPoeticText())
    }

    @Test
    fun testDurationFormattingVariations() {
        val stats1 = PlaybackStats(totalSeconds = 45L)
        assertEquals("45秒", stats1.getFormattedTotalDuration())

        val stats2 = PlaybackStats(totalSeconds = 1800L) // 30 mins
        assertEquals("30分钟", stats2.getFormattedTotalDuration())

        val stats3 = PlaybackStats(totalSeconds = 3600L * 5) // 5 hours exact
        assertEquals("5小时", stats3.getFormattedTotalDuration())

        val stats4 = PlaybackStats(totalSeconds = 3600L * 5 + 60L * 25) // 5h 25m
        assertEquals("5小时25分", stats4.getFormattedTotalDuration())

        val stats5 = PlaybackStats(totalSeconds = 3600L * 100) // 100h
        assertEquals("100小时", stats5.getFormattedTotalDuration())
    }

    @Test
    fun testSleepNightsCalculationStandard8Hours() {
        // Standard 8 hours per night: integer nights and floating precise nights
        val stats8h = PlaybackStats(totalSeconds = 8 * 3600L)
        assertEquals(1, stats8h.sleepNights)
        assertEquals(1.0f, stats8h.sleepNightsPrecise, 0.001f)

        val stats16h = PlaybackStats(totalSeconds = 16 * 3600L)
        assertEquals(2, stats16h.sleepNights)
        assertEquals(2.0f, stats16h.sleepNightsPrecise, 0.001f)

        // 28 hours -> 28 / 8 = 3 nights integer, 3.5 nights precise
        val stats28h = PlaybackStats(totalSeconds = 28 * 3600L)
        assertEquals(3, stats28h.sleepNights)
        assertEquals(3.5f, stats28h.sleepNightsPrecise, 0.001f)

        // 4 hours -> 0 nights integer, 0.5 nights precise
        val stats4h = PlaybackStats(totalSeconds = 4 * 3600L)
        assertEquals(0, stats4h.sleepNights)
        assertEquals(0.5f, stats4h.sleepNightsPrecise, 0.001f)

        // 1 hour -> 0 nights integer, 0.1 nights precise
        val stats1h = PlaybackStats(totalSeconds = 3600L)
        assertEquals(0, stats1h.sleepNights)
        assertEquals(0.1f, stats1h.sleepNightsPrecise, 0.001f)
    }

    @Test
    fun testCompanionPoeticTextProgression() {
        val s0 = PlaybackStats(totalSeconds = 0L)
        assertEquals("戴上耳机，开启属于你的第一个安睡之夜", s0.getCompanionPoeticText())

        val s30m = PlaybackStats(totalSeconds = 1800L)
        assertEquals("声波流淌，开启与自然初遇的静心时刻", s30m.getCompanionPoeticText())

        val s10h = PlaybackStats(totalSeconds = 10 * 3600L)
        assertEquals("风声与梦，见证了属于你的宁静夜晚", s10h.getCompanionPoeticText())

        val s30h = PlaybackStats(totalSeconds = 30 * 3600L)
        assertEquals("静水流深，自然之声在此与你长久相伴", s30h.getCompanionPoeticText())

        val s90h = PlaybackStats(totalSeconds = 90 * 3600L)
        assertEquals("星河长明，声息已陪伴你走过数十个沉睡之夜", s90h.getCompanionPoeticText())
    }

    @Test
    fun testTrackStatsSortingAndRatios() {
        val trackMap = mapOf(
            "rain" to 3600L,        // 1 hour (max)
            "campfire" to 1800L,    // 30 min (50%)
            "wind" to 900L,         // 15 min (25%)
            "stream" to 0L          // 0 min (filtered out in getSortedTrackStats)
        )
        val stats = PlaybackStats(
            totalSeconds = 3600L,
            trackSeconds = trackMap
        )

        val sorted = stats.getSortedTrackStats()
        // stream with 0L is filtered out of leaderboard
        assertEquals(3, sorted.size)
        assertEquals("rain", sorted[0].first)
        assertEquals(3600L, sorted[0].second)

        assertEquals("campfire", sorted[1].first)
        assertEquals(1800L, sorted[1].second)

        assertEquals("wind", sorted[2].first)
        assertEquals(900L, sorted[2].second)

        // Ratio against maximum track
        assertEquals(1.0f, stats.getTrackRatio("rain"), 0.001f)
        assertEquals(0.5f, stats.getTrackRatio("campfire"), 0.001f)
        assertEquals(0.25f, stats.getTrackRatio("wind"), 0.001f)
        assertEquals(0.0f, stats.getTrackRatio("stream"), 0.001f)
        assertEquals(0.0f, stats.getTrackRatio("non_existent"), 0.001f)

        // Track formatted strings
        assertEquals("1小时", stats.getFormattedTrackDuration("rain"))
        assertEquals("30分钟", stats.getFormattedTrackDuration("campfire"))
        assertEquals("15分钟", stats.getFormattedTrackDuration("wind"))
        assertEquals("0分钟", stats.getFormattedTrackDuration("stream"))
    }

    @Test
    fun testPlaybackStatsTrackerPeriodicBatching() = runBlocking {
        var flushedTotal = 0L
        var flushedTracks = emptyMap<String, Long>()

        val tracker = PlaybackStatsTracker(CoroutineScope(Dispatchers.Unconfined)) { totalDelta, trackDeltas ->
            flushedTotal += totalDelta
            flushedTracks = trackDeltas
        }

        // Initially not playing -> tickOnce does nothing and returns false
        tracker.updatePlaybackStatus(isMasterPlaying = false, activeTrackIds = setOf("rain", "thunder"))
        val tickedWhilePaused = tracker.tickOnce()
        assertFalse(tickedWhilePaused)
        assertEquals(0L, flushedTotal)
        assertTrue(flushedTracks.isEmpty())

        // Start playback
        tracker.updatePlaybackStatus(isMasterPlaying = true, activeTrackIds = setOf("rain", "thunder"))

        // 59 ticks -> tickOnce returns false (threshold 60s not reached)
        repeat(59) {
            val shouldFlush = tracker.tickOnce()
            assertFalse(shouldFlush)
        }

        // 60th tick -> threshold reached!
        val shouldFlush60 = tracker.tickOnce()
        assertTrue(shouldFlush60)

        // Sync flush to commit deltas
        tracker.flushSync()
        assertEquals(60L, flushedTotal)
        assertEquals(60L, flushedTracks["rain"])
        assertEquals(60L, flushedTracks["thunder"])
    }

    @Test
    fun testPlaybackStatsTrackerPhysicalWallClockIntegrity() = runBlocking {
        var flushedTotal = 0L
        var flushedTracks = emptyMap<String, Long>()

        val tracker = PlaybackStatsTracker(CoroutineScope(Dispatchers.Unconfined)) { totalDelta, trackDeltas ->
            flushedTotal += totalDelta
            flushedTracks = trackDeltas
        }

        // 3 active concurrent tracks playing simultaneously
        tracker.updatePlaybackStatus(isMasterPlaying = true, activeTrackIds = setOf("rain", "campfire", "wind"))

        // 10 seconds of multi-track mixing
        repeat(10) {
            tracker.tickOnce()
        }

        tracker.flushSync()

        // Total physical time must be exactly 10s, NOT 30s! (Critical for sleep night calculation accuracy)
        assertEquals(10L, flushedTotal)
        // Each individual track gets 10s of enjoyment
        assertEquals(10L, flushedTracks["rain"])
        assertEquals(10L, flushedTracks["campfire"])
        assertEquals(10L, flushedTracks["wind"])
    }

    @Test
    fun testPlaybackStatsTrackerResetClearsMemoryBuffer() = runBlocking {
        var flushedTotal = 0L
        val tracker = PlaybackStatsTracker(CoroutineScope(Dispatchers.Unconfined)) { totalDelta, _ ->
            flushedTotal += totalDelta
        }

        tracker.updatePlaybackStatus(isMasterPlaying = true, activeTrackIds = setOf("brown_noise"))

        repeat(20) {
            tracker.tickOnce()
        }

        // Reset memory counters before flush
        tracker.resetMemoryCounters()

        // Explicit flush should have nothing to write
        tracker.flushSync()
        assertEquals(0L, flushedTotal)
    }

    @Test
    fun testTrackIndividualColorMapping() {
        val tracks = listOf("rain", "storm", "wind", "stream", "fireplace", "summer_night", "white_noise", "brown_noise", "pink_noise", "green_noise")
        for (trackId in tracks) {
            val colorDark = getTrackIndividualColor(trackId, isDark = true)
            val colorLight = getTrackIndividualColor(trackId, isDark = false)
            assertTrue(colorDark.alpha > 0f)
            assertTrue(colorLight.alpha > 0f)
        }
        // Verify individual acoustic distinctiveness
        val rainColor = getTrackIndividualColor("rain")
        val stormColor = getTrackIndividualColor("storm")
        val fireColor = getTrackIndividualColor("fireplace")
        assertFalse(rainColor == stormColor)
        assertFalse(rainColor == fireColor)
    }

    @Test
    fun testPresetSoundDnaResolution() {
        val tracks = listOf("rain", "wind", "birds", "storm", "fireplace")
        // Verify clamping logic: 5 tracks should yield 3 visible tracks + 2 overflow
        val maxVisible = 4
        val visibleTracks = if (tracks.size <= maxVisible) tracks else tracks.take(3)
        val overflow = tracks.size - visibleTracks.size
        assertEquals(3, visibleTracks.size)
        assertEquals(2, overflow)

        // For <= 4 tracks, should display all directly with 0 overflow
        val normalTracks = listOf("rain", "wind", "birds")
        val normalVisible = if (normalTracks.size <= maxVisible) normalTracks else normalTracks.take(3)
        val normalOverflow = normalTracks.size - normalVisible.size
        assertEquals(3, normalVisible.size)
        assertEquals(0, normalOverflow)
    }
}
