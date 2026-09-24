package com.whitenoise.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.SoundTrack
import com.whitenoise.app.data.repository.SoundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "white_noise_prefs")

class PreferencesManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private val KEY_MASTER_VOLUME = floatPreferencesKey("master_volume")
        private val KEY_CUSTOM_PRESETS = stringPreferencesKey("custom_presets_json")
        private val KEY_ACTIVE_TRACKS_STATE = stringPreferencesKey("active_tracks_state_json")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_PRESET_HINT_DISMISSED = booleanPreferencesKey("preset_hint_dismissed")
        private val KEY_DELETED_DEFAULT_PRESETS = stringSetPreferencesKey("deleted_default_preset_ids")
    }

    val deletedDefaultPresetIdsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_DELETED_DEFAULT_PRESETS] ?: emptySet()
    }

    suspend fun saveDeletedDefaultPresetIds(ids: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DELETED_DEFAULT_PRESETS] = ids
        }
    }

    suspend fun addDeletedDefaultPresetId(presetId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_DELETED_DEFAULT_PRESETS] ?: emptySet()
            preferences[KEY_DELETED_DEFAULT_PRESETS] = current + presetId
        }
    }

    suspend fun removeDeletedDefaultPresetId(presetId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_DELETED_DEFAULT_PRESETS] ?: emptySet()
            preferences[KEY_DELETED_DEFAULT_PRESETS] = current - presetId
        }
    }

    suspend fun resetDeletedDefaultPresets() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_DELETED_DEFAULT_PRESETS)
        }
    }

    val presetHintDismissedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_PRESET_HINT_DISMISSED] ?: false
    }

    suspend fun setPresetHintDismissed(dismissed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PRESET_HINT_DISMISSED] = dismissed
        }
    }

    val themeModeFlow: Flow<com.whitenoise.app.core.model.ThemeMode> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_THEME_MODE] ?: com.whitenoise.app.core.model.ThemeMode.SYSTEM.name
        try {
            com.whitenoise.app.core.model.ThemeMode.valueOf(raw)
        } catch (e: Exception) {
            com.whitenoise.app.core.model.ThemeMode.SYSTEM
        }
    }

    suspend fun saveThemeMode(mode: com.whitenoise.app.core.model.ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    val masterVolumeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_MASTER_VOLUME] ?: 1.0f
    }

    suspend fun saveMasterVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MASTER_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }

    val customPresetsFlow: Flow<List<Preset>> = context.dataStore.data.map { preferences ->
        val rawJson = preferences[KEY_CUSTOM_PRESETS] ?: ""
        if (rawJson.isBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<Preset>>(rawJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveCustomPresets(presets: List<Preset>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CUSTOM_PRESETS] = json.encodeToString(presets)
        }
    }

    /**
     * Track state map: trackId -> TrackSaveState(volume, isPlaying, isMuted)
     */
    @kotlinx.serialization.Serializable
    data class TrackSaveState(
        val volume: Float,
        val isPlaying: Boolean,
        val isMuted: Boolean
    )

    val savedTracksStateFlow: Flow<Map<String, TrackSaveState>> = context.dataStore.data.map { preferences ->
        val rawJson = preferences[KEY_ACTIVE_TRACKS_STATE] ?: ""
        if (rawJson.isBlank()) {
            emptyMap()
        } else {
            try {
                json.decodeFromString<Map<String, TrackSaveState>>(rawJson)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    suspend fun saveTracksState(tracks: List<SoundTrack>) {
        val stateMap = tracks.associate { track ->
            track.id to TrackSaveState(
                volume = track.volume,
                isPlaying = track.isPlaying,
                isMuted = track.isMuted
            )
        }
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_TRACKS_STATE] = json.encodeToString(stateMap)
        }
    }
}
