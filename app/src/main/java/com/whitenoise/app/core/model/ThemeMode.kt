package com.whitenoise.app.core.model

/**
 * App theme mode preference.
 */
enum class ThemeMode(val label: String, val iconEmoji: String) {
    SYSTEM("跟随系统", "🌓"),
    LIGHT("浅色模式", "☀️"),
    DARK("深色模式", "🌙")
}
