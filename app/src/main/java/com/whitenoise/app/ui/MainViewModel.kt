package com.whitenoise.app.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whitenoise.app.core.audio.AudioMixerEngine
import com.whitenoise.app.core.model.PlaybackState
import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.PresetShareCode
import com.whitenoise.app.core.model.PresetSharePayload
import com.whitenoise.app.core.model.SoundTrack
import com.whitenoise.app.core.service.WhiteNoiseMediaService
import com.whitenoise.app.data.datastore.PreferencesManager
import com.whitenoise.app.data.repository.PresetRepository
import com.whitenoise.app.data.repository.SoundRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val presetRepository = PresetRepository(preferencesManager)

    // Unified AudioMixerEngine singleton
    val engine: AudioMixerEngine = AudioMixerEngine.getInstance(application)

    // Direct zero-latency state exposure from AudioMixerEngine
    val tracks: StateFlow<List<SoundTrack>> = engine.tracksState
    val playbackState: StateFlow<PlaybackState> = engine.playbackState

    val presets: StateFlow<List<Preset>> = presetRepository.allPresetsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Preset.DEFAULT_PRESETS
    )

    val hasDeletedDefaultPresets: StateFlow<Boolean> = preferencesManager.deletedDefaultPresetIdsFlow
        .map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    val themeMode: StateFlow<com.whitenoise.app.core.model.ThemeMode> = preferencesManager.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = com.whitenoise.app.core.model.ThemeMode.SYSTEM
    )

    // Dialog display states
    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

    private val _showSavePresetDialog = MutableStateFlow(false)
    val showSavePresetDialog: StateFlow<Boolean> = _showSavePresetDialog.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog: StateFlow<Boolean> = _showThemeDialog.asStateFlow()

    private val _showImportDialog = MutableStateFlow(false)
    val showImportDialog: StateFlow<Boolean> = _showImportDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    val keepScreenOn: StateFlow<Boolean> = preferencesManager.keepScreenOnFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val backgroundPlaybackEnabled: StateFlow<Boolean> = preferencesManager.backgroundPlaybackEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    // Detected clipboard preset payload for banner
    private val _clipboardDetectedPayload = MutableStateFlow<PresetSharePayload?>(null)
    val clipboardDetectedPayload: StateFlow<PresetSharePayload?> = _clipboardDetectedPayload.asStateFlow()
    private val dismissedShareCodeHashes = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()

    val isPresetHintDismissed: StateFlow<Boolean> = preferencesManager.presetHintDismissedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    // Toast event flow for UI feedback
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var isPreferencesRestored = false

    init {
        startMediaService()
        restorePreferencesAndObserve()
    }

    private fun startMediaService() {
        val intent = Intent(getApplication(), WhiteNoiseMediaService::class.java)
        try {
            getApplication<Application>().startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun restorePreferencesAndObserve() {
        viewModelScope.launch {
            val savedTracks = preferencesManager.savedTracksStateFlow.first()
            val savedMasterVol = preferencesManager.masterVolumeFlow.first()
            engine.restoreTracksState(savedTracks, savedMasterVol)
            isPreferencesRestored = true
        }

        // Debounced persistence to avoid disk I/O thrashing during slider dragging
        viewModelScope.launch {
            engine.tracksState
                .debounce(500L)
                .collectLatest { list ->
                    if (isPreferencesRestored) {
                        preferencesManager.saveTracksState(list)
                    }
                }
        }

        // Debounced master volume persistence
        viewModelScope.launch {
            engine.playbackState
                .map { it.masterVolume }
                .distinctUntilChanged()
                .debounce(500L)
                .collectLatest { vol ->
                    if (isPreferencesRestored) {
                        preferencesManager.saveMasterVolume(vol)
                    }
                }
        }
    }

    fun toggleMasterPlay() {
        val currentState = engine.playbackState.value.isMasterPlaying
        engine.setMasterPlaying(!currentState)
    }

    fun pauseMasterPlay() {
        engine.setMasterPlaying(false)
    }

    fun setMasterVolume(volume: Float) {
        engine.setMasterVolume(volume)
    }

    fun toggleTrackPlay(trackId: String) {
        val track = engine.tracksState.value.find { it.id == trackId } ?: return
        engine.setTrackPlaying(trackId, !track.isPlaying)
    }

    fun setTrackVolume(trackId: String, volume: Float) {
        engine.setTrackVolume(trackId, volume)
    }

    fun toggleTrackMute(trackId: String) {
        val track = engine.tracksState.value.find { it.id == trackId } ?: return
        engine.setTrackMuted(trackId, !track.isMuted)
    }

    fun applyPreset(preset: Preset) {
        engine.applyPreset(preset)
    }

    fun stopAll() {
        engine.stopAll()
    }

    fun saveCurrentAsPreset(name: String) {
        if (name.isBlank()) return
        val currentTracks = engine.tracksState.value
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

    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            if (preset.isDefault) {
                presetRepository.deleteDefaultPreset(preset.id)
                _toastMessage.emit("已移除默认方案【${preset.name}】")
            } else {
                val currentCustom = preferencesManager.customPresetsFlow.first()
                presetRepository.deleteCustomPreset(preset.id, currentCustom)
                _toastMessage.emit("已删除混音方案【${preset.name}】")
            }
        }
    }

    fun deletePreset(presetId: String) {
        val defaultPreset = Preset.DEFAULT_PRESETS.find { it.id == presetId }
        if (defaultPreset != null) {
            deletePreset(defaultPreset)
        } else {
            viewModelScope.launch {
                val currentCustom = preferencesManager.customPresetsFlow.first()
                val custom = currentCustom.find { it.id == presetId }
                presetRepository.deleteCustomPreset(presetId, currentCustom)
                if (custom != null) {
                    _toastMessage.emit("已删除混音方案【${custom.name}】")
                }
            }
        }
    }

    fun restoreSingleDefaultPreset(presetId: String) {
        val defaultPreset = Preset.DEFAULT_PRESETS.find { it.id == presetId } ?: return
        viewModelScope.launch {
            presetRepository.restoreDefaultPreset(presetId)
            _toastMessage.emit("已恢复默认方案【${defaultPreset.name}】")
        }
    }

    fun notifyPresetAlreadyExists(presetName: String) {
        viewModelScope.launch {
            _toastMessage.emit("方案【$presetName】已在列表中，无需重复添加")
        }
    }

    fun restoreDefaultPresets() {
        viewModelScope.launch {
            presetRepository.restoreDefaultPresets()
            _toastMessage.emit("已恢复所有默认预设方案")
        }
    }

    fun startSleepTimer(minutes: Int) {
        engine.startSleepTimer(minutes)
        _showSleepTimerDialog.value = false
    }

    fun cancelSleepTimer() {
        engine.cancelSleepTimer()
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

    fun setShowThemeDialog(show: Boolean) {
        _showThemeDialog.value = show
    }

    fun setShowImportDialog(show: Boolean) {
        _showImportDialog.value = show
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveKeepScreenOn(enabled)
        }
    }

    fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveBackgroundPlaybackEnabled(enabled)
        }
    }

    fun dismissClipboardBanner() {
        _clipboardDetectedPayload.value?.let { payload ->
            dismissedShareCodeHashes.add(payload.hashCode())
        }
        _clipboardDetectedPayload.value = null
    }

    fun dismissPresetHint() {
        viewModelScope.launch {
            preferencesManager.setPresetHintDismissed(true)
        }
    }

    fun copyPresetShareCode(preset: Preset) {
        val soundNames = SoundRepository.ALL_TRACKS.associate { it.id to it.name }
        val shareText = PresetShareCode.generateShareText(preset, soundNames)
        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("SaltAmbience混音方案", shareText)
            clipboard.setPrimaryClip(clip)
            PresetShareCode.parseShareText(shareText)?.let {
                dismissedShareCodeHashes.add(it.hashCode())
            }
            viewModelScope.launch {
                _toastMessage.emit("已复制【${preset.name}】混音口令，可直接发给微信好友！")
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _toastMessage.emit("复制失败，请稍后重试")
            }
        }
    }

    fun inspectClipboard(clipboardText: String?) {
        if (clipboardText.isNullOrBlank()) return
        val payload = PresetShareCode.parseShareText(clipboardText) ?: return
        if (dismissedShareCodeHashes.contains(payload.hashCode())) return
        if (payload != _clipboardDetectedPayload.value) {
            _clipboardDetectedPayload.value = payload
        }
    }

    fun importPreset(payload: PresetSharePayload, applyImmediately: Boolean = true) {
        viewModelScope.launch {
            val currentCustom = preferencesManager.customPresetsFlow.first()
            val createdPreset = presetRepository.importPresetPayload(payload, currentCustom)
            if (applyImmediately) {
                engine.applyPreset(createdPreset)
            }
            _showImportDialog.value = false
            dismissedShareCodeHashes.add(payload.hashCode())
            _clipboardDetectedPayload.value = null
            _toastMessage.emit("成功导入混音方案【${createdPreset.name}】！")
        }
    }

    fun setThemeMode(mode: com.whitenoise.app.core.model.ThemeMode) {
        viewModelScope.launch {
            preferencesManager.saveThemeMode(mode)
        }
    }
}
