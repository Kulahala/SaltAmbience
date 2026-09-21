package com.whitenoise.app.data.repository

import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.data.datastore.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PresetRepository(private val preferencesManager: PreferencesManager) {

    val allPresetsFlow: Flow<List<Preset>> = preferencesManager.customPresetsFlow.map { customPresets ->
        Preset.DEFAULT_PRESETS + customPresets
    }

    suspend fun saveCustomPreset(preset: Preset, currentCustomPresets: List<Preset>) {
        val updated = currentCustomPresets.filterNot { it.id == preset.id } + preset
        preferencesManager.saveCustomPresets(updated)
    }

    suspend fun deleteCustomPreset(presetId: String, currentCustomPresets: List<Preset>) {
        val updated = currentCustomPresets.filterNot { it.id == presetId }
        preferencesManager.saveCustomPresets(updated)
    }
}
