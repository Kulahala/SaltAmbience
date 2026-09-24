package com.whitenoise.app

import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.SoundCategory
import com.whitenoise.app.data.repository.PresetRepository
import com.whitenoise.app.data.repository.SoundRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundCategoryAndPresetManagementTest {

    @Test
    fun testSoundRepositoryTotalTracksAndBrownNoise() {
        val tracks = SoundRepository.ALL_TRACKS
        assertEquals("Total tracks should be exactly 15", 15, tracks.size)

        // Ensure all track IDs are unique
        val uniqueIds = tracks.map { it.id }.toSet()
        assertEquals("All track IDs must be unique", 15, uniqueIds.size)

        // Verify all tracks have valid metadata and .ogg extension
        for (track in tracks) {
            assertTrue("Track id should not be empty", track.id.isNotBlank())
            assertTrue("Track name should not be empty", track.name.isNotBlank())
            assertTrue("Track subtitle should not be empty", track.subtitle.isNotBlank())
            assertTrue("Asset file name must end with .ogg: ${track.assetFileName}", track.assetFileName.endsWith(".ogg"))
            assertTrue("Track volume should be in 0..1", track.volume in 0f..1f)
        }

        // Verify brown_noise specifically
        val brownNoise = SoundRepository.getTrackById("brown_noise")
        assertNotNull("brown_noise should exist in repository", brownNoise)
        assertEquals("棕色噪音", brownNoise!!.name)
        assertEquals("brown_noise.ogg", brownNoise.assetFileName)
        assertEquals("🪐", brownNoise.iconEmoji)
        assertEquals(SoundCategory.NOISE, brownNoise.category)
    }

    @Test
    fun testSoundCategoryMappingCoverage() {
        // 1. Category definitions check
        assertEquals(setOf("rain", "storm", "stream", "waves", "boat"), SoundCategory.RAIN.trackIds)
        assertEquals(setOf("wind", "fireplace", "birds", "summer_night"), SoundCategory.NATURE.trackIds)
        assertEquals(setOf("coffee_shop", "train", "city"), SoundCategory.LIFE.trackIds)
        assertEquals(setOf("white_noise", "pink_noise", "brown_noise"), SoundCategory.NOISE.trackIds)

        // 2. All 15 tracks must belong to exactly one non-ALL category
        val nonAllCategories = listOf(
            SoundCategory.RAIN,
            SoundCategory.NATURE,
            SoundCategory.LIFE,
            SoundCategory.NOISE
        )

        for (track in SoundRepository.ALL_TRACKS) {
            val matchingCategories = nonAllCategories.filter { it.matches(track.id) }
            assertEquals(
                "Track '${track.id}' should belong to exactly one non-ALL category",
                1,
                matchingCategories.size
            )
            assertEquals("Track category property must match", matchingCategories.first(), track.category)
        }

        // 3. ALL category matches everything
        for (track in SoundRepository.ALL_TRACKS) {
            assertTrue("ALL category must match '${track.id}'", SoundCategory.ALL.matches(track.id))
        }

        // 4. Test unknown track handling
        assertFalse(SoundCategory.RAIN.matches("unknown_sound"))
        assertTrue(SoundCategory.ALL.matches("unknown_sound"))
        assertEquals(SoundCategory.ALL, SoundCategory.fromTrackId("unknown_sound"))
    }

    @Test
    fun testPresetAssemblyCustomPresetsReversedAndAtFront() {
        val customPresets = listOf(
            Preset(id = "c1", name = "方案1", trackVolumes = mapOf("rain" to 0.5f)),
            Preset(id = "c2", name = "方案2", trackVolumes = mapOf("wind" to 0.4f)),
            Preset(id = "c3", name = "方案3", trackVolumes = mapOf("brown_noise" to 0.6f))
        )

        val assembled = PresetRepository.assemblePresets(
            customPresets = customPresets,
            deletedDefaultIds = emptySet()
        )

        // Custom presets should be at the front, in reversed order: c3, c2, c1
        assertEquals("c3", assembled[0].id)
        assertEquals("c2", assembled[1].id)
        assertEquals("c1", assembled[2].id)

        // Total size should be 3 custom + all default presets
        assertEquals(3 + Preset.DEFAULT_PRESETS.size, assembled.size)

        // Defaults should follow custom presets in their original order
        val expectedDefaults = Preset.DEFAULT_PRESETS
        for (i in expectedDefaults.indices) {
            assertEquals(expectedDefaults[i].id, assembled[3 + i].id)
        }
    }

    @Test
    fun testDefaultPresetSoftDeletionAndRestoration() {
        val defaults = Preset.DEFAULT_PRESETS
        val toDelete = setOf(defaults[0].id, defaults[2].id)

        // 1. Soft deletion: assembled list should not contain deleted default IDs
        val withDeleted = PresetRepository.assemblePresets(
            customPresets = emptyList(),
            deletedDefaultIds = toDelete
        )

        assertEquals(defaults.size - 2, withDeleted.size)
        assertFalse(withDeleted.any { it.id == defaults[0].id })
        assertFalse(withDeleted.any { it.id == defaults[2].id })
        assertTrue(withDeleted.any { it.id == defaults[1].id })

        // 2. Restoration: when deletedDefaultIds is empty, all defaults reappear
        val restored = PresetRepository.assemblePresets(
            customPresets = emptyList(),
            deletedDefaultIds = emptySet()
        )
        assertEquals(defaults.size, restored.size)
        assertEquals(defaults.map { it.id }, restored.map { it.id })
    }

    @Test
    fun testFlowBasedPresetRepository() = runBlocking {
        val customList = listOf(
            Preset(id = "c_alpha", name = "Alpha", trackVolumes = mapOf("rain" to 0.5f)),
            Preset(id = "c_beta", name = "Beta", trackVolumes = mapOf("storm" to 0.7f))
        )
        val deletedIds = setOf("deep_night_storm")

        val repository = PresetRepository(
            customPresetsFlowProvider = flowOf(customList),
            deletedDefaultIdsFlowProvider = flowOf(deletedIds)
        )

        val result = repository.allPresetsFlow.first()

        // Beta (latest) should be first, then Alpha
        assertEquals("c_beta", result[0].id)
        assertEquals("c_alpha", result[1].id)

        // "deep_night_storm" must be excluded
        assertFalse("deep_night_storm should be excluded", result.any { it.id == "deep_night_storm" })

        // Other defaults must be present
        assertTrue("corner_cafe should still be present", result.any { it.id == "corner_cafe" })
    }

    @Test
    fun testSingleDefaultPresetRestoration() {
        val defaults = Preset.DEFAULT_PRESETS
        val deletedSet = mutableSetOf(defaults[0].id, defaults[1].id)

        // Initial soft delete state
        val initialList = PresetRepository.assemblePresets(emptyList(), deletedSet)
        assertFalse(initialList.any { it.id == defaults[0].id })
        assertFalse(initialList.any { it.id == defaults[1].id })

        // Single restore defaults[0]
        deletedSet.remove(defaults[0].id)
        val partiallyRestored = PresetRepository.assemblePresets(emptyList(), deletedSet)
        assertTrue("defaults[0] should be restored", partiallyRestored.any { it.id == defaults[0].id })
        assertFalse("defaults[1] should still be deleted", partiallyRestored.any { it.id == defaults[1].id })
    }
}
