package com.whitenoise.app

import com.whitenoise.app.core.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeModeTest {

    @Test
    fun testThemeModeEnumValues() {
        assertEquals(3, ThemeMode.entries.size)
        assertEquals("跟随系统", ThemeMode.SYSTEM.label)
        assertEquals("浅色模式", ThemeMode.LIGHT.label)
        assertEquals("深色模式", ThemeMode.DARK.label)
    }

    @Test
    fun testThemeModeParsingAndFallback() {
        val parsedSystem = try {
            ThemeMode.valueOf("SYSTEM")
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        assertEquals(ThemeMode.SYSTEM, parsedSystem)

        val parsedLight = try {
            ThemeMode.valueOf("LIGHT")
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        assertEquals(ThemeMode.LIGHT, parsedLight)

        val parsedDark = try {
            ThemeMode.valueOf("DARK")
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        assertEquals(ThemeMode.DARK, parsedDark)

        val fallback = try {
            ThemeMode.valueOf("INVALID_UNKNOWN_MODE")
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        assertEquals(ThemeMode.SYSTEM, fallback)
    }

    @Test
    fun testThemeModeDarkDecision() {
        // ThemeMode.SYSTEM follows isSystemDark
        fun computeIsDark(mode: ThemeMode, isSystemDark: Boolean): Boolean {
            return when (mode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
        }

        assertEquals(true, computeIsDark(ThemeMode.SYSTEM, isSystemDark = true))
        assertEquals(false, computeIsDark(ThemeMode.SYSTEM, isSystemDark = false))
        assertEquals(false, computeIsDark(ThemeMode.LIGHT, isSystemDark = true))
        assertEquals(false, computeIsDark(ThemeMode.LIGHT, isSystemDark = false))
        assertEquals(true, computeIsDark(ThemeMode.DARK, isSystemDark = true))
        assertEquals(true, computeIsDark(ThemeMode.DARK, isSystemDark = false))
    }
}
