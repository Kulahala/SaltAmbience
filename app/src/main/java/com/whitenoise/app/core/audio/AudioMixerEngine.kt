package com.whitenoise.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.whitenoise.app.core.model.PlaybackState
import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.SoundTrack
import com.whitenoise.app.data.repository.SoundRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioMixerEngine private constructor(private val context: Context) {

    companion object {
        private const val TAG = "AudioMixerEngine"

        @Volatile
        private var instance: AudioMixerEngine? = null

        fun getInstance(context: Context): AudioMixerEngine {
            return instance ?: synchronized(this) {
                instance ?: AudioMixerEngine(context.applicationContext).also { instance = it }
            }
        }

        /**
         * Factory method to create an independent DefaultLoadControl instance for each player.
         * DefaultLoadControl in Media3 asserts single-thread affinity in onPrepared (threadId == -1 || threadId == currentThreadId).
         * Sharing a singleton instance across multiple ExoPlayers running on separate playback threads causes
         * IllegalStateException, rendering all subsequent tracks silent.
         */
        fun createLowLatencyLoadControl(): DefaultLoadControl {
            return DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    /* minBufferMs = */ 1000,
                    /* maxBufferMs = */ 2000,
                    /* bufferForPlaybackMs = */ 50,
                    /* bufferForPlaybackAfterRebufferMs = */ 100
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Player Pool: soundId -> ExoPlayer
    private val playerPool = mutableMapOf<String, ExoPlayer>()

    // Track state
    private val _tracksState = MutableStateFlow(SoundRepository.ALL_TRACKS)
    val tracksState: StateFlow<List<SoundTrack>> = _tracksState.asStateFlow()

    // Playback state
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // Internal states
    private var isDucked = false
    private var pausedDueToAudioFocus = false
    private var sleepTimerJob: Job? = null
    private var totalSleepDurationSeconds: Long = 0L

    // Tracks initialized with anti-loop fatigue (Scheme A offset & Scheme B drift) in the current session
    private val activeSessionTracks = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    // Tracks whose initial random seek is pending until STATE_READY because duration was C.TIME_UNSET
    private val pendingInitialSeekTracks = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    // Audio Focus
    private var audioFocusRequest: AudioFocusRequest? = null
    @Volatile
    private var hasAudioFocus = false
    private var focusJob: Job? = null

    var onSleepTimerCompleted: (() -> Unit)? = null

    // Low latency load control specifically tuned for local asset audio:
    // bufferForPlaybackMs = 50ms ensures virtually instantaneous audio start without waiting for network buffers.
    // Each ExoPlayer instance must have its own DefaultLoadControl to avoid single-thread affinity assertions.
    fun createLowLatencyLoadControl(): DefaultLoadControl = Companion.createLowLatencyLoadControl()

    init {
        setupAudioFocus()
    }

    private fun setupAudioFocus() {
        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            Log.d(TAG, "Audio focus changed: $focusChange")
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    isDucked = false
                    updateAllVolumes()
                    if (pausedDueToAudioFocus) {
                        pausedDueToAudioFocus = false
                        setMasterPlaying(true)
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    isDucked = true
                    updateAllVolumes()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (_playbackState.value.isMasterPlaying) {
                        pausedDueToAudioFocus = true
                        pauseAllInternal()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    pausedDueToAudioFocus = false
                    setMasterPlaying(false)
                }
            }
        }

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
    }

    @Synchronized
    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val request = audioFocusRequest ?: return false
        val result = audioManager.requestAudioFocus(request)
        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    @Synchronized
    private fun abandonAudioFocus() {
        if (hasAudioFocus) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            hasAudioFocus = false
        }
        pausedDueToAudioFocus = false
        isDucked = false
    }

    private fun dispatchAsyncRequestAudioFocus() {
        focusJob?.cancel()
        focusJob = scope.launch(Dispatchers.IO) {
            val granted = requestAudioFocus()
            if (!isActive) return@launch
            if (!_playbackState.value.isMasterPlaying) {
                abandonAudioFocus()
                return@launch
            }
            if (!granted) {
                withContext(Dispatchers.Main) {
                    if (_playbackState.value.isMasterPlaying) {
                        pauseAllInternal()
                        _playbackState.update { it.copy(isMasterPlaying = false) }
                    }
                }
            }
        }
    }

    private fun dispatchAsyncAbandonAudioFocus() {
        focusJob?.cancel()
        focusJob = scope.launch(Dispatchers.IO) {
            abandonAudioFocus()
        }
    }

    /**
     * Set master playing state. If true, starts playing all enabled tracks; if false, pauses them.
     * Uses 0ms optimistic state update so UI controls flip immediately without waiting for hardware/Binder.
     */
    fun setMasterPlaying(play: Boolean) {
        val currentTracks = _tracksState.value
        val hasActiveTracks = currentTracks.any { it.isPlaying && !it.isMuted }

        if (play && !hasActiveTracks) {
            // If master play is requested but no track is active, enable default track (e.g. rain)
            val updated = currentTracks.map { track ->
                if (track.id == "rain") track.copy(isPlaying = true, isMuted = false) else track
            }
            _tracksState.value = updated
        }

        val activeTracks = _tracksState.value.filter { it.isPlaying && !it.isMuted }
        val activeCount = activeTracks.size
        val primaryId = activeTracks.maxByOrNull { it.volume }?.id

        // 1. Optimistic 0ms UI flip
        _playbackState.update {
            it.copy(
                isMasterPlaying = play,
                activeTrackCount = activeCount,
                primaryTrackId = primaryId,
                sleepFadeFraction = if (play && !it.isSleepTimerRunning) 1.0f else it.sleepFadeFraction
            )
        }

        // 2. Play or pause players immediately & non-blocking Binder IPC
        if (play) {
            resumeAllActiveInternal()
            dispatchAsyncRequestAudioFocus()
        } else {
            pauseAllInternal()
            dispatchAsyncAbandonAudioFocus()
        }
    }

    /**
     * Master volume adjustment (0.0 .. 1.0)
     */
    fun setMasterVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _playbackState.update { it.copy(masterVolume = clamped) }
        updateAllVolumes()
    }

    /**
     * Toggle track playing state with 0ms optimistic UI flip
     */
    fun setTrackPlaying(trackId: String, isPlaying: Boolean) {
        val wasMasterPlaying = _playbackState.value.isMasterPlaying

        _tracksState.update { list ->
            list.map { if (it.id == trackId) it.copy(isPlaying = isPlaying) else it }
        }

        val activeTracks = _tracksState.value.filter { it.isPlaying && !it.isMuted }
        val activeCount = activeTracks.size
        val primaryId = activeTracks.maxByOrNull { it.volume }?.id

        val track = _tracksState.value.find { it.id == trackId } ?: return

        if (isPlaying) {
            val player = getOrCreatePlayer(track)
            val actualVolume = calculateVolumeForTrack(track)
            player.volume = actualVolume

            // Turning on track: initialize random offset and speed drift
            applyRandomStartAndDrift(track, player)
            activeSessionTracks.add(track.id)

            if (!wasMasterPlaying) {
                // If master was paused, turning on a track initiates master play, resuming all active tracks together
                setMasterPlaying(true)
            } else {
                _playbackState.update {
                    it.copy(
                        activeTrackCount = activeCount,
                        primaryTrackId = primaryId
                    )
                }
                player.playWhenReady = true
                dispatchAsyncRequestAudioFocus()
            }
        } else {
            activeSessionTracks.remove(trackId)
            pendingInitialSeekTracks.remove(trackId)
            playerPool[trackId]?.playWhenReady = false
            if (activeCount == 0 && wasMasterPlaying) {
                // Auto pause master when all tracks are turned off
                setMasterPlaying(false)
            } else {
                _playbackState.update {
                    it.copy(
                        activeTrackCount = activeCount,
                        primaryTrackId = primaryId
                    )
                }
            }
        }
    }

    /**
     * Set individual track volume
     */
    fun setTrackVolume(trackId: String, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _tracksState.update { list ->
            list.map {
                if (it.id == trackId) {
                    val unmuted = if (it.isMuted && clamped > 0f) false else it.isMuted
                    it.copy(volume = clamped, isMuted = unmuted)
                } else it
            }
        }
        val track = _tracksState.value.find { it.id == trackId } ?: return
        playerPool[trackId]?.let { player ->
            player.volume = calculateVolumeForTrack(track)
            if (track.isPlaying && !track.isMuted && _playbackState.value.isMasterPlaying) {
                if (!activeSessionTracks.contains(track.id)) {
                    applyRandomStartAndDrift(track, player)
                    activeSessionTracks.add(track.id)
                }
                player.playWhenReady = true
            }
        }
        val activeTracks = _tracksState.value.filter { t -> t.isPlaying && !t.isMuted }
        val primaryId = activeTracks.maxByOrNull { it.volume }?.id
        _playbackState.update {
            it.copy(
                activeTrackCount = activeTracks.size,
                primaryTrackId = primaryId
            )
        }
        if (activeTracks.isNotEmpty() && _playbackState.value.isMasterPlaying) {
            dispatchAsyncRequestAudioFocus()
        }
    }

    /**
     * Toggle track mute state
     */
    fun setTrackMuted(trackId: String, isMuted: Boolean) {
        _tracksState.update { list ->
            list.map { if (it.id == trackId) it.copy(isMuted = isMuted) else it }
        }
        val track = _tracksState.value.find { it.id == trackId } ?: return
        playerPool[trackId]?.let { player ->
            player.volume = calculateVolumeForTrack(track)
            if (track.isPlaying && !track.isMuted && _playbackState.value.isMasterPlaying) {
                if (!activeSessionTracks.contains(track.id)) {
                    applyRandomStartAndDrift(track, player)
                    activeSessionTracks.add(track.id)
                }
                player.playWhenReady = true
            } else if (isMuted) {
                player.playWhenReady = false
            }
        }
        val activeTracks = _tracksState.value.filter { t -> t.isPlaying && !t.isMuted }
        val primaryId = activeTracks.maxByOrNull { it.volume }?.id
        _playbackState.update {
            it.copy(
                activeTrackCount = activeTracks.size,
                primaryTrackId = primaryId
            )
        }
        if (activeTracks.isNotEmpty() && _playbackState.value.isMasterPlaying) {
            dispatchAsyncRequestAudioFocus()
        }
    }

    /**
     * Apply a preset configuration
     */
    fun applyPreset(preset: Preset) {
        _tracksState.update { list ->
            list.map { track ->
                val targetVol = preset.trackVolumes[track.id]
                if (targetVol != null && targetVol > 0f) {
                    track.copy(volume = targetVol, isPlaying = true, isMuted = false)
                } else {
                    track.copy(isPlaying = false, isMuted = false)
                }
            }
        }

        val activeTracks = _tracksState.value.filter { it.isPlaying && !it.isMuted }
        val activeCount = activeTracks.size
        val primaryId = activeTracks.maxByOrNull { it.volume }?.id

        _playbackState.update {
            it.copy(
                isMasterPlaying = activeCount > 0,
                activeTrackCount = activeCount,
                primaryTrackId = primaryId
            )
        }

        // Apply to players
        _tracksState.value.forEach { track ->
            if (track.isPlaying) {
                val player = getOrCreatePlayer(track)
                player.volume = calculateVolumeForTrack(track)
                // Preset activation: randomize start offset and speed drift for a fresh scene experience
                applyRandomStartAndDrift(track, player)
                activeSessionTracks.add(track.id)
                player.playWhenReady = true
            } else {
                activeSessionTracks.remove(track.id)
                pendingInitialSeekTracks.remove(track.id)
                playerPool[track.id]?.playWhenReady = false
            }
        }

        if (activeCount > 0) {
            dispatchAsyncRequestAudioFocus()
        } else {
            dispatchAsyncAbandonAudioFocus()
        }
    }

    /**
     * Stop all tracks and pause master
     */
    fun stopAll() {
        cancelSleepTimer()
        _tracksState.update { list ->
            list.map { it.copy(isPlaying = false) }
        }
        playerPool.values.forEach { it.playWhenReady = false }
        activeSessionTracks.clear()
        pendingInitialSeekTracks.clear()
        _playbackState.update {
            it.copy(isMasterPlaying = false, activeTrackCount = 0, primaryTrackId = null)
        }
        dispatchAsyncAbandonAudioFocus()
    }

    /**
     * Start sleep timer with smooth logarithmic/quadratic decay
     */
    fun startSleepTimer(durationMinutes: Int) {
        startSleepTimerSeconds(durationMinutes * 60L)
    }

    fun startSleepTimerSeconds(durationSeconds: Long) {
        cancelSleepTimer()
        if (durationSeconds <= 0L) return

        totalSleepDurationSeconds = durationSeconds
        val fadeDuration = VolumeCalculator.determineFadeDuration(durationSeconds)

        _playbackState.update {
            it.copy(
                sleepTimerRemainingSeconds = durationSeconds,
                isSleepTimerRunning = true,
                sleepFadeFraction = 1.0f
            )
        }

        sleepTimerJob = scope.launch {
            var remaining = durationSeconds
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining -= 1
                val fadeMultiplier = VolumeCalculator.calculateSleepFadeMultiplier(remaining, fadeDuration)
                _playbackState.update {
                    it.copy(
                        sleepTimerRemainingSeconds = remaining,
                        sleepFadeFraction = fadeMultiplier
                    )
                }
                if (remaining <= fadeDuration) {
                    updateAllVolumes()
                }
            }

            if (isActive && remaining <= 0L) {
                Log.i(TAG, "Sleep timer expired. Pausing playback and releasing resources.")
                stopAll()
                _playbackState.update {
                    it.copy(
                        sleepTimerRemainingSeconds = null,
                        isSleepTimerRunning = false,
                        sleepFadeFraction = 1.0f,
                        isMasterPlaying = false
                    )
                }
                updateAllVolumes()
                releasePlayers()
                onSleepTimerCompleted?.invoke()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        totalSleepDurationSeconds = 0L
        _playbackState.update {
            it.copy(
                sleepTimerRemainingSeconds = null,
                isSleepTimerRunning = false,
                sleepFadeFraction = 1.0f
            )
        }
        updateAllVolumes()
    }

    /**
     * Restore tracks state from DataStore memory
     */
    fun restoreTracksState(savedState: Map<String, com.whitenoise.app.data.datastore.PreferencesManager.TrackSaveState>, savedMasterVolume: Float) {
        _playbackState.update { it.copy(masterVolume = savedMasterVolume.coerceIn(0f, 1f)) }
        if (savedState.isNotEmpty()) {
            _tracksState.update { list ->
                list.map { track ->
                    val saved = savedState[track.id]
                    if (saved != null) {
                        track.copy(
                            volume = saved.volume,
                            isPlaying = saved.isPlaying,
                            isMuted = saved.isMuted
                        )
                    } else {
                        track
                    }
                }
            }
        }
        val activeTracks = _tracksState.value.filter { t -> t.isPlaying && !t.isMuted }
        val primaryId = activeTracks.maxByOrNull { it.volume }?.id
        _playbackState.update {
            it.copy(
                activeTrackCount = activeTracks.size,
                primaryTrackId = primaryId
            )
        }
        // Pre-warm ExoPlayers in memory so they are in STATE_READY and start instantly on play
        val active = _tracksState.value.filter { it.isPlaying }
        if (active.isNotEmpty()) {
            active.forEach { track ->
                val player = getOrCreatePlayer(track)
                player.volume = calculateVolumeForTrack(track)
                applyRandomStartAndDrift(track, player)
                activeSessionTracks.add(track.id)
                player.playWhenReady = false
            }
        } else {
            // Pre-warm fallback default track "rain" for cold starts (player instance & buffers prepared)
            _tracksState.value.find { it.id == "rain" }?.let { rainTrack ->
                val player = getOrCreatePlayer(rainTrack)
                player.volume = calculateVolumeForTrack(rainTrack)
                player.playWhenReady = false
            }
        }
    }

    private fun getOrCreatePlayer(track: SoundTrack): ExoPlayer {
        return playerPool.getOrPut(track.id) {
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .build()

            ExoPlayer.Builder(context)
                .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ false)
                .setLoadControl(createLowLatencyLoadControl())
                .build().apply {
                    val uri = "asset:///sounds/${track.assetFileName}"
                    setMediaItem(MediaItem.fromUri(uri))
                    repeatMode = Player.REPEAT_MODE_ONE
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                if (pendingInitialSeekTracks.remove(track.id)) {
                                    val dur = LoopFatigueHelper.getEffectiveDuration(track.id, duration)
                                    if (dur > 0L) {
                                        val offset = LoopFatigueHelper.calculateRandomStartOffset(dur)
                                        if (offset > 0L) {
                                            seekTo(offset)
                                        }
                                        Log.d(TAG, "Deferred random seek applied for ${track.id} at STATE_READY: ${offset}ms (duration: ${dur}ms)")
                                    }
                                }
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            Log.e(TAG, "Playback error for ${track.id}: ${error.message}", error)
                        }
                    })
                    prepare()
                }
        }
    }

    private fun calculateVolumeForTrack(track: SoundTrack): Float {
        val state = _playbackState.value
        return VolumeCalculator.calculateActualVolume(
            trackVolume = track.volume,
            masterVolume = state.masterVolume,
            sleepFadeMultiplier = state.sleepFadeFraction,
            isDucked = isDucked,
            isPlaying = track.isPlaying,
            isMuted = track.isMuted
        )
    }

    private fun updateAllVolumes() {
        _tracksState.value.forEach { track ->
            val player = playerPool[track.id]
            if (player != null) {
                player.volume = calculateVolumeForTrack(track)
            }
        }
    }

    private fun resumeAllActiveInternal() {
        _tracksState.value.forEach { track ->
            if (track.isPlaying && !track.isMuted) {
                val player = getOrCreatePlayer(track)
                player.volume = calculateVolumeForTrack(track)
                // If track hasn't been initialized in this playback session, apply random start & micro-speed drift
                if (!activeSessionTracks.contains(track.id)) {
                    applyRandomStartAndDrift(track, player)
                    activeSessionTracks.add(track.id)
                }
                player.playWhenReady = true
            } else {
                playerPool[track.id]?.playWhenReady = false
            }
        }
    }

    private fun pauseAllInternal() {
        playerPool.values.forEach { it.playWhenReady = false }
    }

    fun releasePlayers() {
        activeSessionTracks.clear()
        pendingInitialSeekTracks.clear()
        playerPool.values.forEach { player ->
            try {
                player.stop()
                player.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing ExoPlayer: ${e.message}")
            }
        }
        playerPool.clear()
    }

    /**
     * Applies Scheme A (Random Start Offset) and Scheme B (Micro-Speed Drift) to a track's player.
     * Called when a track is first activated/turned on or when switching presets.
     */
    private fun applyRandomStartAndDrift(track: SoundTrack, player: ExoPlayer) {
        // Scheme B: Natural micro-speed drift [0.98f, 1.02f] with speed == pitch (resampling mode, no FFT artifacts)
        try {
            val playbackParams = LoopFatigueHelper.createDriftPlaybackParameters()
            player.playbackParameters = playbackParams
        } catch (e: Exception) {
            Log.w(TAG, "Failed to apply playback parameters for ${track.id}: ${e.message}")
            player.playbackParameters = PlaybackParameters.DEFAULT
        }

        // Scheme A: Safe random start offset
        // CRITICAL GUARD: Seeking before Player.STATE_READY (while STATE_IDLE or STATE_BUFFERING) stalls
        // or crashes ExoPlayer's OGG Vorbis decoder on local assets.
        // Therefore, if the player is not yet STATE_READY, seek MUST be safely deferred to onPlaybackStateChanged(STATE_READY).
        if (player.playbackState == Player.STATE_READY) {
            val effectiveDuration = LoopFatigueHelper.getEffectiveDuration(track.id, player.duration)
            if (effectiveDuration > 0L) {
                val offset = LoopFatigueHelper.calculateRandomStartOffset(effectiveDuration)
                if (offset > 0L) {
                    player.seekTo(offset)
                }
                pendingInitialSeekTracks.remove(track.id)
                Log.d(TAG, "Applied start offset: ${offset}ms / ${effectiveDuration}ms for track '${track.id}' (already STATE_READY)")
            }
        } else {
            // Duration and Vorbis codebooks not ready yet, defer seek to STATE_READY in Player.Listener
            pendingInitialSeekTracks.add(track.id)
            Log.d(TAG, "Player not ready for track '${track.id}' (state: ${player.playbackState}), queued initial seek for STATE_READY")
        }
    }

    internal fun isTrackInActiveSession(trackId: String): Boolean = activeSessionTracks.contains(trackId)
    internal fun isInitialSeekPending(trackId: String): Boolean = pendingInitialSeekTracks.contains(trackId)
    internal fun getPlayer(trackId: String): ExoPlayer? = playerPool[trackId]

    fun release() {
        cancelSleepTimer()
        abandonAudioFocus()
        releasePlayers()
        scope.cancel()
    }
}
