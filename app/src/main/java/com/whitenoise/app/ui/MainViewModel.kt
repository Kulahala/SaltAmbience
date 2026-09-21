package com.whitenoise.app.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whitenoise.app.core.audio.AudioMixerEngine
import com.whitenoise.app.core.model.PlaybackState
import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.SoundTrack
import com.whitenoise.app.core.service.WhiteNoiseMediaService
import com.whitenoise.app.data.datastore.PreferencesManager
import com.whitenoise.app.data.repository.PresetRepository
import com.whitenoise.app.data.repository.SoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val presetRepository = PresetRepository(preferencesManager)

    // In-process fallback or bound engine
    private var boundEngine: AudioMixerEngine? = null
    private val fallbackEngine = AudioMixerEngine(application)

    private val activeEngine: AudioMixerEngine
        get() = boundEngine ?: WhiteNoiseMediaService.instance?.audioEngine ?: fallbackEngine

    private val _tracks = MutableStateFlow(SoundRepository.ALL_TRACKS)
    val tracks: StateFlow<List<SoundTrack>> = _tracks.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    val presets: StateFlow<List<Preset>> = presetRepository.allPresetsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Preset.DEFAULT_PRESETS
    )

    // Dialog display states
    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

    private val _showSavePresetDialog = MutableStateFlow(false)
    val showSavePresetDialog: StateFlow<Boolean> = _showSavePresetDialog.asStateFlow()

    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? WhiteNoiseMediaService.LocalBinder
            boundEngine = binder?.engine
            observeActiveEngine()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundEngine = null
        }
    }

    init {
        bindMediaService()
        restorePreferences()
        observeActiveEngine()
    }

    private fun bindMediaService() {
        val intent = Intent(getApplication(), WhiteNoiseMediaService::class.java)
        try {
            getApplication<Application>().startService(intent)
            getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            isServiceBound = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restorePreferences() {
        viewModelScope.launch {
            val savedTracks = preferencesManager.savedTracksStateFlow.first()
            val savedMasterVol = preferencesManager.masterVolumeFlow.first()
            activeEngine.restoreTracksState(savedTracks, savedMasterVol)
        }
    }

    private fun observeActiveEngine() {
        viewModelScope.launch {
            activeEngine.tracksState.collectLatest { list ->
                _tracks.value = list
                preferencesManager.saveTracksState(list)
            }
        }
        viewModelScope.launch {
            activeEngine.playbackState.collectLatest { state ->
                _playbackState.value = state
                preferencesManager.saveMasterVolume(state.masterVolume)
            }
        }
    }

    fun toggleMasterPlay() {
        val currentState = activeEngine.playbackState.value.isMasterPlaying
        activeEngine.setMasterPlaying(!currentState)
    }

    fun setMasterVolume(volume: Float) {
        activeEngine.setMasterVolume(volume)
    }

    fun toggleTrackPlay(trackId: String) {
        val track = activeEngine.tracksState.value.find { it.id == trackId } ?: return
        activeEngine.setTrackPlaying(trackId, !track.isPlaying)
    }

    fun setTrackVolume(trackId: String, volume: Float) {
        activeEngine.setTrackVolume(trackId, volume)
    }

    fun toggleTrackMute(trackId: String) {
        val track = activeEngine.tracksState.value.find { it.id == trackId } ?: return
        activeEngine.setTrackMuted(trackId, !track.isMuted)
    }

    fun applyPreset(preset: Preset) {
        activeEngine.applyPreset(preset)
    }

    fun stopAll() {
        activeEngine.stopAll()
    }

    fun saveCurrentAsPreset(name: String) {
        if (name.isBlank()) return
        val currentTracks = activeEngine.tracksState.value
        val volumeMap = currentTracks
            .filter { it.isPlaying && it.volume > 0f }
            .associate { it.id to it.volume }

        if (volumeMap.isEmpty()) return

        val newPreset = Preset(
            id = "custom_${UUID.randomUUID()}",
            name = name.trim(),
            description = "自定义混音方案",
            trackVolumes = volumeMap,
            isDefault = false
        )

        viewModelScope.launch {
            val currentCustom = preferencesManager.customPresetsFlow.first()
            presetRepository.saveCustomPreset(newPreset, currentCustom)
        }
    }

    fun deletePreset(presetId: String) {
        viewModelScope.launch {
            val currentCustom = preferencesManager.customPresetsFlow.first()
            presetRepository.deleteCustomPreset(presetId, currentCustom)
        }
    }

    fun startSleepTimer(minutes: Int) {
        activeEngine.startSleepTimer(minutes)
        _showSleepTimerDialog.value = false
    }

    fun cancelSleepTimer() {
        activeEngine.cancelSleepTimer()
        _showSleepTimerDialog.value = false
    }

    fun setShowSleepTimerDialog(show: Boolean) {
        _showSleepTimerDialog.value = show
    }

    fun setShowAboutDialog(show: Boolean) {
        _showAboutDialog.value = show
    }

    fun setShowSavePresetDialog(show: Boolean) {
        _showSavePresetDialog.value = show
    }

    override fun onCleared() {
        super.onCleared()
        if (isServiceBound) {
            try {
                getApplication<Application>().unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isServiceBound = false
        }
        if (boundEngine == null && WhiteNoiseMediaService.instance == null) {
            fallbackEngine.release()
        }
    }
}
