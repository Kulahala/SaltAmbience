package com.whitenoise.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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

    // Audio Focus
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    var onSleepTimerCompleted: (() -> Unit)? = null

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

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val request = audioFocusRequest ?: return false
        val result = audioManager.requestAudioFocus(request)
        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        hasAudioFocus = false
    }

    /**
     * Set master playing state. If true, starts playing all enabled tracks; if false, pauses them.
     */
    fun setMasterPlaying(play: Boolean) {
        val currentTracks = _tracksState.value
        val hasActiveTracks = currentTracks.any { it.isPlaying && !it.isMuted }

        if (play && !hasActiveTracks) {
            // If master play is requested but no track is active, enable default track (e.g. rain)
            val updated = currentTracks.map { track ->
                if (track.id == "rain") track.copy(isPlaying = true) else track
            }
            _tracksState.value = updated
        }

        if (play) {
            if (!_playbackState.value.isSleepTimerRunning) {
                _playbackState.update { it.copy(sleepFadeFraction = 1.0f) }
            }
            requestAudioFocus()
            resumeAllActiveInternal()
        } else {
            pauseAllInternal()
        }

        _playbackState.update {
            it.copy(
                isMasterPlaying = play,
                activeTrackCount = _tracksState.value.count { t -> t.isPlaying && !t.isMuted }
            )
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
     * Toggle track playing state
     */
    fun setTrackPlaying(trackId: String, isPlaying: Boolean) {
        _tracksState.update { list ->
            list.map { if (it.id == trackId) it.copy(isPlaying = isPlaying) else it }
        }
        val track = _tracksState.value.find { it.id == trackId } ?: return

        if (isPlaying) {
            val player = getOrCreatePlayer(track)
            if (!_playbackState.value.isMasterPlaying) {
                setMasterPlaying(true)
            } else {
                val actualVolume = calculateVolumeForTrack(track)
                player.volume = actualVolume
                requestAudioFocus()
                player.playWhenReady = true
            }
        } else {
            playerPool[trackId]?.playWhenReady = false
        }

        val activeCount = _tracksState.value.count { it.isPlaying && !it.isMuted }
        _playbackState.update { it.copy(activeTrackCount = activeCount) }

        if (activeCount == 0 && _playbackState.value.isMasterPlaying) {
            // Auto pause master when all tracks are turned off
            setMasterPlaying(false)
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
        }
        _playbackState.update {
            it.copy(activeTrackCount = _tracksState.value.count { t -> t.isPlaying && !t.isMuted })
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
        }
        _playbackState.update {
            it.copy(activeTrackCount = _tracksState.value.count { t -> t.isPlaying && !t.isMuted })
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

        // Apply to players
        _tracksState.value.forEach { track ->
            if (track.isPlaying) {
                val player = getOrCreatePlayer(track)
                player.volume = calculateVolumeForTrack(track)
                if (_playbackState.value.isMasterPlaying) {
                    player.playWhenReady = true
                }
            } else {
                playerPool[track.id]?.playWhenReady = false
            }
        }

        val activeCount = _tracksState.value.count { it.isPlaying && !it.isMuted }
        _playbackState.update {
            it.copy(
                isMasterPlaying = true,
                activeTrackCount = activeCount
            )
        }
        requestAudioFocus()
        resumeAllActiveInternal()
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
        _playbackState.update {
            it.copy(isMasterPlaying = false, activeTrackCount = 0)
        }
        abandonAudioFocus()
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
                updateAllVolumes()
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
        _playbackState.update {
            it.copy(activeTrackCount = _tracksState.value.count { t -> t.isPlaying && !t.isMuted })
        }
        // Don't auto-play on initial restore; user taps Play to begin
    }

    private fun getOrCreatePlayer(track: SoundTrack): ExoPlayer {
        return playerPool.getOrPut(track.id) {
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .build()

            ExoPlayer.Builder(context)
                .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ false)
                .build().apply {
                    val uri = "asset:///sounds/${track.assetFileName}"
                    setMediaItem(MediaItem.fromUri(uri))
                    repeatMode = Player.REPEAT_MODE_ONE
                    addListener(object : Player.Listener {
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

    fun release() {
        cancelSleepTimer()
        abandonAudioFocus()
        releasePlayers()
        scope.cancel()
    }
}
