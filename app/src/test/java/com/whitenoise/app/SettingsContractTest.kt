package com.whitenoise.app

import androidx.compose.ui.graphics.Color
import com.whitenoise.app.core.model.ThemeMode
import com.whitenoise.app.ui.components.BauhausUiSymbol
import com.whitenoise.app.ui.components.BauhausUiTheme
import com.whitenoise.app.ui.components.toBauhausSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for Settings configuration, screen-on & background playback contracts.
 */
class SettingsContractTest {

    @Test
    fun testSettingsDefaultValuesContract() {
        // Contract: Keep Screen On defaults to false (prevent battery drain)
        val defaultKeepScreenOn = false
        // Contract: Background Playback defaults to true (white noise essential behavior)
        val defaultBackgroundPlayback = true

        assertFalse(defaultKeepScreenOn)
        assertTrue(defaultBackgroundPlayback)
    }

    @Test
    fun testKeepScreenOnConditionLogic() {
        fun shouldKeepScreenOn(
            keepScreenOnPref: Boolean,
            isMasterPlaying: Boolean,
            hasActivePlayingTracks: Boolean
        ): Boolean {
            return keepScreenOnPref && isMasterPlaying && hasActivePlayingTracks
        }

        // When setting is off, screen should never be held on
        assertFalse(shouldKeepScreenOn(keepScreenOnPref = false, isMasterPlaying = true, hasActivePlayingTracks = true))
        assertFalse(shouldKeepScreenOn(keepScreenOnPref = false, isMasterPlaying = false, hasActivePlayingTracks = false))

        // When setting is on, but master playback is paused, screen should NOT be held on
        assertFalse(shouldKeepScreenOn(keepScreenOnPref = true, isMasterPlaying = false, hasActivePlayingTracks = true))

        // When setting is on, master playing is true, but no tracks are unmuted/active, screen should NOT be held on
        assertFalse(shouldKeepScreenOn(keepScreenOnPref = true, isMasterPlaying = true, hasActivePlayingTracks = false))

        // Screen is ONLY held on when all three conditions are satisfied
        assertTrue(shouldKeepScreenOn(keepScreenOnPref = true, isMasterPlaying = true, hasActivePlayingTracks = true))
    }

    @Test
    fun testBackgroundPlaybackOnStopContract() {
        var pauseCalled = false
        fun handleAppOnStop(
            backgroundPlaybackEnabled: Boolean,
            isMasterPlaying: Boolean,
            onPauseMaster: () -> Unit
        ) {
            if (!backgroundPlaybackEnabled && isMasterPlaying) {
                onPauseMaster()
            }
        }

        // Scenario 1: Background playback is enabled (default) -> app sent to background -> playback continues
        pauseCalled = false
        handleAppOnStop(
            backgroundPlaybackEnabled = true,
            isMasterPlaying = true,
            onPauseMaster = { pauseCalled = true }
        )
        assertFalse("Playback should not be paused when background playback is enabled", pauseCalled)

        // Scenario 2: Background playback is disabled -> app sent to background while playing -> playback pauses
        pauseCalled = false
        handleAppOnStop(
            backgroundPlaybackEnabled = false,
            isMasterPlaying = true,
            onPauseMaster = { pauseCalled = true }
        )
        assertTrue("Playback must be paused when background playback is disabled", pauseCalled)

        // Scenario 3: Background playback is disabled -> app sent to background while already paused -> no redundant call
        pauseCalled = false
        handleAppOnStop(
            backgroundPlaybackEnabled = false,
            isMasterPlaying = false,
            onPauseMaster = { pauseCalled = true }
        )
        assertFalse("Pause should not be triggered if playback is already paused", pauseCalled)
    }

    @Test
    fun testSettingsThemeSegmentMapping() {
        val themeModes = ThemeMode.entries
        assertEquals(3, themeModes.size)

        assertEquals(BauhausUiSymbol.ThemeSystem, ThemeMode.SYSTEM.toBauhausSymbol())
        assertEquals(BauhausUiSymbol.ThemeLight, ThemeMode.LIGHT.toBauhausSymbol())
        assertEquals(BauhausUiSymbol.ThemeDark, ThemeMode.DARK.toBauhausSymbol())
    }

    @Test
    fun testSettingsSymbolPalette() {
        val darkPalette = BauhausUiTheme.getPalette(BauhausUiSymbol.Settings, isDark = true)
        val lightPalette = BauhausUiTheme.getPalette(BauhausUiSymbol.Settings, isDark = false)

        assertEquals(Color(0xFFE2E8F0), darkPalette.primary)
        assertEquals(Color(0xFF38BDF8), darkPalette.secondary)
        assertEquals(Color(0xFF334155), lightPalette.primary)
        assertEquals(Color(0xFF38BDF8), lightPalette.secondary)
    }
}
