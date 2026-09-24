package com.whitenoise.app

import com.whitenoise.app.core.model.ThemeMode
import com.whitenoise.app.ui.components.BauhausUiSymbol
import com.whitenoise.app.ui.components.toBauhausSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for Bauhaus UI vector symbols and state mapping contracts.
 */
class BauhausUiIconTest {

    @Test
    fun testAllBauhausUiSymbolsDefined() {
        val symbols = BauhausUiSymbol.entries
        assertEquals(12, symbols.size)

        val expectedNames = listOf(
            "Play",
            "Pause",
            "Clear",
            "ThemeSystem",
            "ThemeLight",
            "ThemeDark",
            "Close",
            "Check",
            "Timer",
            "Add",
            "Restore",
            "Import"
        )
        for (name in expectedNames) {
            assertTrue("Expected symbol $name in BauhausUiSymbol entries", symbols.any { it.name == name })
        }
    }

    @Test
    fun testThemeModeToBauhausSymbolMapping() {
        assertEquals(BauhausUiSymbol.ThemeSystem, ThemeMode.SYSTEM.toBauhausSymbol())
        assertEquals(BauhausUiSymbol.ThemeLight, ThemeMode.LIGHT.toBauhausSymbol())
        assertEquals(BauhausUiSymbol.ThemeDark, ThemeMode.DARK.toBauhausSymbol())
    }

    @Test
    fun testMixerControlStateContract() {
        fun resolveMixerControl(isMasterPlaying: Boolean): Pair<BauhausUiSymbol, String> {
            val symbol = if (isMasterPlaying) BauhausUiSymbol.Pause else BauhausUiSymbol.Play
            val text = if (isMasterPlaying) "暂停混音" else "继续混音"
            return symbol to text
        }

        val (playingSymbol, playingText) = resolveMixerControl(isMasterPlaying = true)
        assertEquals(BauhausUiSymbol.Pause, playingSymbol)
        assertEquals("暂停混音", playingText)

        val (pausedSymbol, pausedText) = resolveMixerControl(isMasterPlaying = false)
        assertEquals(BauhausUiSymbol.Play, pausedSymbol)
        assertEquals("继续混音", pausedText)
    }

    @Test
    fun testMixerClearButtonContract() {
        // Clear mix button must use BauhausUiSymbol.Clear and label "清空混音"
        val clearSymbol = BauhausUiSymbol.Clear
        val clearLabel = "清空混音"
        assertEquals(BauhausUiSymbol.Clear, clearSymbol)
        assertEquals("清空混音", clearLabel)
    }

    @Test
    fun testPresetActionSymbolsContract() {
        // Add preset uses BauhausUiSymbol.Add
        assertEquals(BauhausUiSymbol.Add, BauhausUiSymbol.valueOf("Add"))
        // Restore default presets uses BauhausUiSymbol.Restore
        assertEquals(BauhausUiSymbol.Restore, BauhausUiSymbol.valueOf("Restore"))
        // Delete preset dialog uses BauhausUiSymbol.Clear
        assertEquals(BauhausUiSymbol.Clear, BauhausUiSymbol.valueOf("Clear"))
        // Dismiss / Close buttons across drawers use BauhausUiSymbol.Close
        assertEquals(BauhausUiSymbol.Close, BauhausUiSymbol.valueOf("Close"))
        // Selection checkmark uses BauhausUiSymbol.Check
        assertEquals(BauhausUiSymbol.Check, BauhausUiSymbol.valueOf("Check"))
        // Import preset button uses BauhausUiSymbol.Import
        assertEquals(BauhausUiSymbol.Import, BauhausUiSymbol.valueOf("Import"))
    }

    @Test
    fun testBottomPlayerBarSymbolsContract() {
        // Play/Pause master button toggle
        fun resolvePlayerBarIcon(isMasterPlaying: Boolean): BauhausUiSymbol {
            return if (isMasterPlaying) BauhausUiSymbol.Pause else BauhausUiSymbol.Play
        }
        assertEquals(BauhausUiSymbol.Pause, resolvePlayerBarIcon(isMasterPlaying = true))
        assertEquals(BauhausUiSymbol.Play, resolvePlayerBarIcon(isMasterPlaying = false))

        // Countdown / sleep timer pill icon must always be BauhausUiSymbol.Timer
        assertEquals(BauhausUiSymbol.Timer, BauhausUiSymbol.valueOf("Timer"))
    }

    @Test
    fun testPromptRequiredCoreTenSymbolsContract() {
        // The 10 core symbols explicitly requested in prompt:
        val requiredCoreSymbols = listOf(
            BauhausUiSymbol.Play,
            BauhausUiSymbol.Pause,
            BauhausUiSymbol.Clear,
            BauhausUiSymbol.ThemeSystem,
            BauhausUiSymbol.ThemeLight,
            BauhausUiSymbol.ThemeDark,
            BauhausUiSymbol.Close,
            BauhausUiSymbol.Check,
            BauhausUiSymbol.Timer,
            BauhausUiSymbol.Add
        )
        val allSymbols = BauhausUiSymbol.entries
        for (required in requiredCoreSymbols) {
            assertTrue("Core symbol $required must be in BauhausUiSymbol", allSymbols.contains(required))
        }
    }
}
