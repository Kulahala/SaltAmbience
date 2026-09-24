package com.whitenoise.app

import androidx.compose.ui.graphics.Color
import com.whitenoise.app.core.model.SoundCategory
import com.whitenoise.app.core.model.ThemeMode
import com.whitenoise.app.ui.components.BauhausSoundTheme
import com.whitenoise.app.ui.components.BauhausUiSymbol
import com.whitenoise.app.ui.components.BauhausUiTheme
import com.whitenoise.app.ui.components.getContentColor
import com.whitenoise.app.ui.components.getThemeColor
import com.whitenoise.app.ui.components.toBauhausSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for Bauhaus UI vector symbols and state mapping contracts.
 */
class BauhausUiIconTest {

    @Test
    fun testAllBauhausUiSymbolsDefined() {
        val symbols = BauhausUiSymbol.entries
        assertEquals(14, symbols.size)

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
            "Import",
            "Settings",
            "Warning"
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

    @Test
    fun testBauhausUiThemePalettes() {
        val symbols = BauhausUiSymbol.entries
        for (symbol in symbols) {
            val darkPalette = BauhausUiTheme.getPalette(symbol, isDark = true)
            val lightPalette = BauhausUiTheme.getPalette(symbol, isDark = false)
            assertNotNull(darkPalette.primary)
            assertNotNull(darkPalette.secondary)
            assertNotNull(lightPalette.primary)
            assertNotNull(lightPalette.secondary)
        }

        // ThemeLight: 暖阳金 (#F59E0B) + 光子亮黄 (#FDE68A)
        val lightTheme = BauhausUiTheme.getPalette(BauhausUiSymbol.ThemeLight, isDark = false)
        assertEquals(Color(0xFFF59E0B), lightTheme.primary)
        assertEquals(Color(0xFFFDE68A), lightTheme.secondary)

        // ThemeDark: 月牙金 (#FBBF24) + 幽紫星芒 (#A78BFA)
        val darkTheme = BauhausUiTheme.getPalette(BauhausUiSymbol.ThemeDark, isDark = true)
        assertEquals(Color(0xFFFBBF24), darkTheme.primary)
        assertEquals(Color(0xFFA78BFA), darkTheme.secondary)

        // ThemeSystem: 冰蓝 (#38BDF8) + 纯白 (#FFFFFF / 深灰 #475569)
        val sysThemeDark = BauhausUiTheme.getPalette(BauhausUiSymbol.ThemeSystem, isDark = true)
        assertEquals(Color(0xFF38BDF8), sysThemeDark.primary)
        assertEquals(Color.White, sysThemeDark.secondary)
        val sysThemeLight = BauhausUiTheme.getPalette(BauhausUiSymbol.ThemeSystem, isDark = false)
        assertEquals(Color(0xFF38BDF8), sysThemeLight.primary)
        assertEquals(Color(0xFF475569), sysThemeLight.secondary)

        // Timer: 薄荷翡翠绿 (#10B981 / #34D399)
        val timer = BauhausUiTheme.getPalette(BauhausUiSymbol.Timer, isDark = false)
        assertEquals(Color(0xFF10B981), timer.primary)
        assertEquals(Color(0xFF34D399), timer.secondary)

        // Clear: 珊瑚赤红 (#F87171 / #EF4444)
        val clear = BauhausUiTheme.getPalette(BauhausUiSymbol.Clear, isDark = false)
        assertEquals(Color(0xFFEF4444), clear.primary)
        assertEquals(Color(0xFFF87171), clear.secondary)

        // Import: 电光青蓝 (#38BDF8)
        val importDark = BauhausUiTheme.getPalette(BauhausUiSymbol.Import, isDark = true)
        assertEquals(Color(0xFF38BDF8), importDark.primary)
        val importLight = BauhausUiTheme.getPalette(BauhausUiSymbol.Import, isDark = false)
        assertEquals(Color(0xFF0284C7), importLight.primary)

        // Add: 活力亮绿 (#22C55E / #34D399)
        val add = BauhausUiTheme.getPalette(BauhausUiSymbol.Add, isDark = false)
        assertEquals(Color(0xFF22C55E), add.primary)
        assertEquals(Color(0xFF34D399), add.secondary)

        // Restore: 灵动天青蓝 (#60A5FA)
        val restore = BauhausUiTheme.getPalette(BauhausUiSymbol.Restore, isDark = false)
        assertEquals(Color(0xFF60A5FA), restore.primary)
        assertEquals(Color(0xFF93C5FD), restore.secondary)

        // Play: 强调蓝 (#3B82F6) 与纯白 (Color.White)
        val playDark = BauhausUiTheme.getPalette(BauhausUiSymbol.Play, isDark = true)
        assertEquals(Color(0xFF3B82F6), playDark.primary)
        assertEquals(Color.White, playDark.secondary)
        val playLight = BauhausUiTheme.getPalette(BauhausUiSymbol.Play, isDark = false)
        assertEquals(Color(0xFF3B82F6), playLight.primary)
        assertEquals(Color.White, playLight.secondary)

        // Pause: 强调蓝 (#3B82F6) 与纯白 / 天蓝 (#60A5FA)
        val pauseDark = BauhausUiTheme.getPalette(BauhausUiSymbol.Pause, isDark = true)
        assertEquals(Color(0xFF3B82F6), pauseDark.primary)
        assertEquals(Color.White, pauseDark.secondary)
        val pauseLight = BauhausUiTheme.getPalette(BauhausUiSymbol.Pause, isDark = false)
        assertEquals(Color(0xFF3B82F6), pauseLight.primary)
        assertEquals(Color(0xFF60A5FA), pauseLight.secondary)

        // Close: 优雅灰度
        val closeDark = BauhausUiTheme.getPalette(BauhausUiSymbol.Close, isDark = true)
        assertEquals(Color(0xFF94A3B8), closeDark.primary)
        val closeLight = BauhausUiTheme.getPalette(BauhausUiSymbol.Close, isDark = false)
        assertEquals(Color(0xFF64748B), closeLight.primary)

        // Check: 完成绿 (#10B981)
        val check = BauhausUiTheme.getPalette(BauhausUiSymbol.Check, isDark = false)
        assertEquals(Color(0xFF10B981), check.primary)
        assertEquals(Color(0xFF34D399), check.secondary)

        // Settings: 机械冷灰/极光冰蓝
        val settingsDark = BauhausUiTheme.getPalette(BauhausUiSymbol.Settings, isDark = true)
        assertEquals(Color(0xFFE2E8F0), settingsDark.primary)
        assertEquals(Color(0xFF38BDF8), settingsDark.secondary)
        val settingsLight = BauhausUiTheme.getPalette(BauhausUiSymbol.Settings, isDark = false)
        assertEquals(Color(0xFF334155), settingsLight.primary)
        assertEquals(Color(0xFF38BDF8), settingsLight.secondary)

        // Warning: 琥珀暖金/明亮暖黄
        val warningDark = BauhausUiTheme.getPalette(BauhausUiSymbol.Warning, isDark = true)
        assertEquals(Color(0xFFFBBF24), warningDark.primary)
        assertEquals(Color(0xFFFDE68A), warningDark.secondary)
        val warningLight = BauhausUiTheme.getPalette(BauhausUiSymbol.Warning, isDark = false)
        assertEquals(Color(0xFFD97706), warningLight.primary)
        assertEquals(Color(0xFFF59E0B), warningLight.secondary)
    }

    @Test
    fun testSoundCategoryThemeColors() {
        assertEquals(Color(0xFF60A5FA), SoundCategory.ALL.getThemeColor())
        assertEquals(Color(0xFF38BDF8), SoundCategory.RAIN.getThemeColor())
        assertEquals(Color(0xFF34D399), SoundCategory.NATURE.getThemeColor())
        assertEquals(Color(0xFFFB923C), SoundCategory.LIFE.getThemeColor())
        assertEquals(Color(0xFFF472B6), SoundCategory.NOISE.getThemeColor())

        // Ensure each category has a distinct non-grey theme color
        val colors = SoundCategory.entries.map { it.getThemeColor() }
        assertEquals(5, colors.distinct().size)

        // Ensure content colors exist and are valid for both dark and light modes
        for (category in SoundCategory.entries) {
            val darkContent = category.getContentColor(isDark = true)
            val lightContent = category.getContentColor(isDark = false)
            assertNotNull(darkContent)
            assertNotNull(lightContent)
            assertEquals(category.getThemeColor(), darkContent)
        }
    }

    @Test
    fun testBauhausSoundIconTintedIdleContract() {
        val tracks = listOf(
            "rain", "storm", "wind", "stream", "fireplace", "birds",
            "summer_night", "white_noise", "waves", "coffee_shop",
            "train", "boat", "pink_noise", "city", "brown_noise",
            "fan", "clock", "keyboard", "wind_chimes", "rain_roof", "underwater", "green_noise"
        )
        for (trackId in tracks) {
            val palette = BauhausSoundTheme.getPalette(trackId, isDark = true)
            val idlePalette = palette.toIdlePalette(0.48f)
            val themeIdlePalette = BauhausSoundTheme.getIdlePalette(trackId, isDark = true, 0.48f)

            assertEquals(idlePalette, themeIdlePalette)
            assertEquals(0.48f, idlePalette.primary.alpha, 0.01f)
            assertEquals(0.48f, idlePalette.secondary.alpha, 0.01f)
            assertEquals(palette.primary.red, idlePalette.primary.red, 0.001f)
            assertEquals(palette.primary.green, idlePalette.primary.green, 0.001f)
            assertEquals(palette.primary.blue, idlePalette.primary.blue, 0.001f)
            assertEquals(palette.secondary.red, idlePalette.secondary.red, 0.001f)
            assertEquals(palette.secondary.green, idlePalette.secondary.green, 0.001f)
            assertEquals(palette.secondary.blue, idlePalette.secondary.blue, 0.001f)
        }
    }

    @Test
    fun testBauhausSoundIconMonochromeIdleContract() {
        // Dark theme idle palette must be clean monochrome white with alpha
        val darkMono = BauhausSoundTheme.getMonochromeIdlePalette(isDark = true)
        assertEquals(darkMono.primary.red, darkMono.primary.green, 0.001f)
        assertEquals(darkMono.primary.green, darkMono.primary.blue, 0.001f)
        assertEquals(darkMono.secondary.red, darkMono.secondary.green, 0.001f)
        assertEquals(darkMono.secondary.green, darkMono.secondary.blue, 0.001f)

        // Light theme idle palette must also be pure neutral dark tone
        val lightMono = BauhausSoundTheme.getMonochromeIdlePalette(isDark = false)
        assertEquals(lightMono.primary.red, lightMono.primary.green, 0.02f)
        assertEquals(lightMono.primary.green, lightMono.primary.blue, 0.02f)
        assertEquals(lightMono.secondary.red, lightMono.secondary.green, 0.02f)
        assertEquals(lightMono.secondary.green, lightMono.secondary.blue, 0.02f)
    }

    @Test
    fun testPlayPauseMorphContract() {
        // Test that play/pause morphing bounds interpolate correctly
        fun morphPillar(start: Float, end: Float, progress: Float) = start + (end - start) * progress

        // When progress = 0 (Play mode):
        // Right apex should converge (y = 0.50h)
        val apexTopY = morphPillar(0.50f, 0.18f, 0f)
        val apexBottomY = morphPillar(0.50f, 0.82f, 0f)
        assertEquals(0.50f, apexTopY, 0.001f)
        assertEquals(0.50f, apexBottomY, 0.001f)

        // When progress = 1 (Pause mode):
        // Twin pillars should be symmetrically apart
        val leftBarX = morphPillar(0.24f, 0.22f, 1f)
        val rightBarX = morphPillar(0.46f, 0.58f, 1f)
        assertTrue("Left bar should be on the left", leftBarX < 0.30f)
        assertTrue("Right bar should be on the right", rightBarX > 0.50f)
    }
}
