package com.whitenoise.app.data.repository

import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.PresetSharePayload
import com.whitenoise.app.data.datastore.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

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

    /**
     * Import a PresetSharePayload as a new custom preset.
     * Automatically filters unknown tracks, resolves duplicate names, and persists.
     */
    suspend fun importPresetPayload(
        payload: PresetSharePayload,
        currentCustomPresets: List<Preset>,
        knownTrackIds: Set<String> = SoundRepository.ALL_TRACKS.map { it.id }.toSet()
    ): Preset {
        val validVolumes = payload.volumes
            .filter { (id, vol) -> id in knownTrackIds && vol > 0f }
            .mapValues { it.value.coerceIn(0f, 1f) }

        val allExistingNames = (Preset.DEFAULT_PRESETS + currentCustomPresets).map { it.name }.toSet()
        var finalName = payload.name.trim().ifEmpty { "导入的混音方案" }
        if (finalName in allExistingNames) {
            var counter = 1
            while ("$finalName(导入$counter)" in allExistingNames) {
                counter++
            }
            finalName = "$finalName(导入$counter)"
        }

        val newPreset = Preset(
            id = UUID.randomUUID().toString(),
            name = finalName,
            description = payload.description.trim(),
            trackVolumes = validVolumes,
            isDefault = false
        )

        saveCustomPreset(newPreset, currentCustomPresets)
        return newPreset
    }
}
