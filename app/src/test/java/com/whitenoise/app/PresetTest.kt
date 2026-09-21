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
}
