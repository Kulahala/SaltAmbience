package com.whitenoise.app

import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.SoundTrack
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testDefaultPresetsNotEmpty() {
        val defaults = Preset.DEFAULT_PRESETS
        assertTrue(defaults.size >= 4)
        val deepStorm = defaults.find { it.id == "deep_night_storm" }
        assertNotNull(deepStorm)
        assertTrue(deepStorm!!.trackVolumes.containsKey("rain"))
        assertTrue(deepStorm.trackVolumes.containsKey("storm"))
    }

    @Test
    fun testPresetSerializationRoundTrip() {
        val original = Preset(
            id = "custom_test_1",
            name = "午后露台",
            description = "自定义测试",
            trackVolumes = mapOf("birds" to 0.7f, "wind" to 0.4f),
            isDefault = false
        )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<Preset>(encoded)

        assertEquals(original.id, decoded.id)
        assertEquals(original.name, decoded.name)
        assertEquals(original.trackVolumes["birds"], decoded.trackVolumes["birds"])
        assertEquals(original.trackVolumes["wind"], decoded.trackVolumes["wind"])
        assertEquals(false, decoded.isDefault)
    }

    @Test
    fun testSoundTrackEffectiveVolume() {
        val playingTrack = SoundTrack(
            id = "rain",
            name = "细雨",
            subtitle = "秋雨",
            assetFileName = "rain.ogg",
            volume = 0.65f,
            isPlaying = true,
            isMuted = false
        )
        assertEquals(0.65f, playingTrack.effectiveTrackVolume, 0.001f)

        val mutedTrack = playingTrack.copy(isMuted = true)
        assertEquals(0.0f, mutedTrack.effectiveTrackVolume, 0.001f)

        val stoppedTrack = playingTrack.copy(isPlaying = false)
        assertEquals(0.0f, stoppedTrack.effectiveTrackVolume, 0.001f)
    }

    @Test
    fun testTrackSaveStateSerializationRoundTrip() {
        val saveMap = mapOf(
            "rain" to com.whitenoise.app.data.datastore.PreferencesManager.TrackSaveState(
                volume = 0.85f,
                isPlaying = true,
                isMuted = false
            ),
            "wind" to com.whitenoise.app.data.datastore.PreferencesManager.TrackSaveState(
                volume = 0.40f,
                isPlaying = false,
                isMuted = true
            )
        )

        val encoded = json.encodeToString(saveMap)
        val decoded = json.decodeFromString<Map<String, com.whitenoise.app.data.datastore.PreferencesManager.TrackSaveState>>(encoded)

        assertEquals(2, decoded.size)
        assertEquals(0.85f, decoded["rain"]!!.volume, 0.001f)
        assertEquals(true, decoded["rain"]!!.isPlaying)
        assertEquals(false, decoded["rain"]!!.isMuted)
        assertEquals(0.40f, decoded["wind"]!!.volume, 0.001f)
        assertEquals(false, decoded["wind"]!!.isPlaying)
        assertEquals(true, decoded["wind"]!!.isMuted)
    }

    @Test
    fun testSoundTrackIconEmojiMapping() {
        val expectedEmojis = mapOf(
            "rain" to "🌧️",
            "storm" to "⛈️",
            "wind" to "🌲",
            "stream" to "💧",
            "fireplace" to "🪵",
            "birds" to "🐦",
            "summer_night" to "🦗",
            "white_noise" to "📻",
            "waves" to "🌊",
            "coffee_shop" to "☕",
            "train" to "🚂",
            "boat" to "🛶",
            "pink_noise" to "🌸",
            "city" to "🏙️",
            "brown_noise" to "🪐"
        )

        for ((id, expectedEmoji) in expectedEmojis) {
            val track = SoundTrack(
                id = id,
                name = id,
                subtitle = id,
                assetFileName = "$id.ogg"
            )
            assertEquals("Track $id should have emoji $expectedEmoji", expectedEmoji, track.iconEmoji)
        }

        val unknownTrack = SoundTrack(
            id = "unknown_sound",
            name = "未知音效",
            subtitle = "未知",
            assetFileName = "unknown.ogg"
        )
        assertEquals("🎵", unknownTrack.iconEmoji)
    }

    @Test
    fun testImportPresetUnknownTracksFilteringAndRename() {
        val knownTrackIds = setOf("rain", "storm", "wind")
        val payload = com.whitenoise.app.core.model.PresetSharePayload(
            name = "深夜暴雨", // Matches default preset
            description = "导入测试",
            volumes = mapOf(
                "rain" to 0.7f,
                "future_magic_sound" to 0.9f, // Unknown sound, should be dropped
                "wind" to -0.5f // Negative volume, should be dropped
            )
        )

        val validVolumes = payload.volumes
            .filter { (id, vol) -> id in knownTrackIds && vol > 0f }
            .mapValues { it.value.coerceIn(0f, 1f) }

        assertEquals(1, validVolumes.size)
        assertTrue(validVolumes.containsKey("rain"))
        assertEquals(false, validVolumes.containsKey("future_magic_sound"))
        assertEquals(false, validVolumes.containsKey("wind"))

        // Duplicate name test
        val existingNames = setOf("深夜暴雨", "深夜暴雨(导入1)")
        var finalName = payload.name.trim()
        if (finalName in existingNames) {
            var counter = 1
            while ("$finalName(导入$counter)" in existingNames) {
                counter++
            }
            finalName = "$finalName(导入$counter)"
        }
        assertEquals("深夜暴雨(导入2)", finalName)
    }

    @Test
    fun testPresetMatchesTracks() {
        val preset = Preset(
            id = "test_preset",
            name = "测试预设",
            trackVolumes = mapOf(
                "rain" to 0.8f,
                "storm" to 0.6f,
                "wind" to 0.35f
            )
        )

        // 1. Exact match
        assertTrue(preset.matchesTracks(mapOf("rain" to 0.8f, "storm" to 0.6f, "wind" to 0.35f)))

        // 2. Slight floating point tolerance (< 0.02f)
        assertTrue(preset.matchesTracks(mapOf("rain" to 0.805f, "storm" to 0.595f, "wind" to 0.352f)))

        // 3. Volume mismatch (> 0.02f)
        assertEquals(false, preset.matchesTracks(mapOf("rain" to 0.5f, "storm" to 0.6f, "wind" to 0.35f)))

        // 4. Missing one track
        assertEquals(false, preset.matchesTracks(mapOf("rain" to 0.8f, "storm" to 0.6f)))

        // 5. Extra active track
        assertEquals(false, preset.matchesTracks(mapOf("rain" to 0.8f, "storm" to 0.6f, "wind" to 0.35f, "waves" to 0.4f)))

        // 6. Empty active tracks
        assertEquals(false, preset.matchesTracks(emptyMap()))

        // 7. Empty preset tracks
        val emptyPreset = Preset(id = "empty", name = "空预设", trackVolumes = emptyMap())
        assertEquals(false, emptyPreset.matchesTracks(mapOf("rain" to 0.8f)))
        assertEquals(false, emptyPreset.matchesTracks(emptyMap()))

        // 8. Preset containing 0.0f volume entries
        val presetWithZero = Preset(
            id = "with_zero",
            name = "含静音轨预设",
            trackVolumes = mapOf(
                "rain" to 0.8f,
                "storm" to 0.6f,
                "wind" to 0.35f,
                "stream" to 0.0f
            )
        )
        assertTrue(presetWithZero.matchesTracks(mapOf("rain" to 0.8f, "storm" to 0.6f, "wind" to 0.35f)))

        // 9. Preset with only 0.0f tracks should not match anything
        val allZeroPreset = Preset(id = "all_zero", name = "全0预设", trackVolumes = mapOf("rain" to 0.0f))
        assertEquals(false, allZeroPreset.matchesTracks(mapOf("rain" to 0.8f)))
        assertEquals(false, allZeroPreset.matchesTracks(emptyMap()))
    }
}
