package com.whitenoise.app.core.service

import android.util.Log
import com.whitenoise.app.data.datastore.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * High-efficiency, battery-protective companion tracker for playback duration.
 *
 * Design:
 * - Accumulates playback seconds in memory every 1000ms.
 * - Dual-flush strategy:
 *   1. Periodic batch flush: Automatically writes to DataStore every 60 seconds (1 minute).
 *   2. Event-driven immediate flush: Flushes uncommitted residual seconds on pause, stop, track changes, or service destroy.
 * - Zero flash memory wear and negligible battery consumption.
 */
class PlaybackStatsTracker(
    private val scope: CoroutineScope,
    private val commitAction: suspend (Long, Map<String, Long>) -> Unit
) {
    /**
     * Primary constructor for production use with PreferencesManager.
     */
    constructor(
        preferencesManager: PreferencesManager,
        scope: CoroutineScope
    ) : this(
        scope = scope,
        commitAction = { total, tracks -> preferencesManager.commitPlaybackDelta(total, tracks) }
    )

    companion object {
        private const val TAG = "PlaybackStatsTracker"
        const val FLUSH_INTERVAL_SECONDS = 60L
    }

    private val mutex = Mutex()
    private var uncommittedTotalSeconds = 0L
    private val uncommittedTrackSeconds = ConcurrentHashMap<String, Long>()

    @Volatile
    var isPlaying = false
        private set

    val currentActiveTrackIds = CopyOnWriteArraySet<String>()

    private var tickerJob: Job? = null

    /**
     * Updates active playback status.
     * Called whenever master play/pause or active track set changes.
     */
    fun updatePlaybackStatus(isMasterPlaying: Boolean, activeTrackIds: Set<String>) {
        val shouldPlay = isMasterPlaying && activeTrackIds.isNotEmpty()

        currentActiveTrackIds.clear()
        if (shouldPlay) {
            currentActiveTrackIds.addAll(activeTrackIds)
        }

        if (shouldPlay != isPlaying) {
            isPlaying = shouldPlay
            if (shouldPlay) {
                startTicker()
            } else {
                stopTickerAndFlush()
            }
        }
    }

    /**
     * Executes a single 1-second simulation tick.
     * Returns true if periodic flush threshold (60s) is reached.
     */
    fun tickOnce(): Boolean {
        if (!isPlaying) return false
        val activeIds = currentActiveTrackIds.toSet()
        if (activeIds.isEmpty()) return false

        var shouldFlush = false
        synchronized(this) {
            uncommittedTotalSeconds += 1L
            for (trackId in activeIds) {
                uncommittedTrackSeconds.compute(trackId) { _, existing -> (existing ?: 0L) + 1L }
            }
            if (uncommittedTotalSeconds % FLUSH_INTERVAL_SECONDS == 0L) {
                shouldFlush = true
            }
        }
        return shouldFlush
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch(Dispatchers.Default) {
            while (isActive && isPlaying) {
                delay(1000L)
                if (!isPlaying) break

                val shouldFlush = tickOnce()
                if (shouldFlush) {
                    flushToDiskAsync()
                }
            }
        }
    }

    private fun stopTickerAndFlush() {
        tickerJob?.cancel()
        tickerJob = null
        flushToDiskAsync()
    }

    /**
     * Extracts uncommitted memory deltas and asynchronously commits them.
     */
    fun flushToDiskAsync() {
        scope.launch(Dispatchers.IO) {
            var totalDelta = 0L
            val trackDeltas = mutableMapOf<String, Long>()

            synchronized(this@PlaybackStatsTracker) {
                if (uncommittedTotalSeconds > 0L || uncommittedTrackSeconds.isNotEmpty()) {
                    totalDelta = uncommittedTotalSeconds
                    uncommittedTotalSeconds = 0L

                    for ((k, v) in uncommittedTrackSeconds) {
                        if (v > 0L) {
                            trackDeltas[k] = v
                        }
                    }
                    uncommittedTrackSeconds.clear()
                }
            }

            if (totalDelta > 0L || trackDeltas.isNotEmpty()) {
                try {
                    commitAction(totalDelta, trackDeltas)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to commit playback stats delta: ${e.message}")
                    // Re-accumulate in case of write failure
                    synchronized(this@PlaybackStatsTracker) {
                        uncommittedTotalSeconds += totalDelta
                        for ((k, v) in trackDeltas) {
                            uncommittedTrackSeconds.compute(k) { _, old -> (old ?: 0L) + v }
                        }
                    }
                }
            }
        }
    }

    /**
     * Synchronously flushes remaining deltas in calling coroutine (for deterministic unit tests).
     */
    suspend fun flushSync() {
        var totalDelta = 0L
        val trackDeltas = mutableMapOf<String, Long>()

        synchronized(this) {
            if (uncommittedTotalSeconds > 0L || uncommittedTrackSeconds.isNotEmpty()) {
                totalDelta = uncommittedTotalSeconds
                uncommittedTotalSeconds = 0L

                for ((k, v) in uncommittedTrackSeconds) {
                    if (v > 0L) {
                        trackDeltas[k] = v
                    }
                }
                uncommittedTrackSeconds.clear()
            }
        }

        if (totalDelta > 0L || trackDeltas.isNotEmpty()) {
            commitAction(totalDelta, trackDeltas)
        }
    }

    /**
     * Resets in-memory accumulator counters.
     */
    fun resetMemoryCounters() {
        synchronized(this) {
            uncommittedTotalSeconds = 0L
            uncommittedTrackSeconds.clear()
        }
    }
}
