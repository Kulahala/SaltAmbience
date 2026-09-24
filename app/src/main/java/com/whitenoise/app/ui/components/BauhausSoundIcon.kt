package com.whitenoise.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.whitenoise.app.core.model.SoundCategory

/**
 * Semantic acoustic color palette for Bauhaus minimalist vector symbols.
 * Each sound effect maps to iconic natural color associations (e.g. fire: amber-orange, storm: lightning gold,
 * forest wind: evergreen, stream: clear river blue, summer night: lunar gold + night violet, etc.)
 */
data class SoundIconPalette(
    val primary: Color,
    val secondary: Color
) {
    fun toIdlePalette(alpha: Float = 0.48f): SoundIconPalette = SoundIconPalette(
        primary = primary.copy(alpha = alpha),
        secondary = secondary.copy(alpha = alpha)
    )
}

object BauhausSoundTheme {
    fun getPalette(trackId: String, isDark: Boolean): SoundIconPalette {
        val lightPrimary = if (isDark) Color.White else Color(0xFF1E2026)
        return when (trackId) {
            "rain" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFE0F7FA) else Color(0xFF263238),
                secondary = Color(0xFF29B6F6) // 清澈雨丝蓝
            )
            "storm" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFFFFD600) // 电光金黄
            )
            "wind" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFF43A047) // 苍翠林木绿
            )
            "stream" -> SoundIconPalette(
                primary = lightPrimary, // 上部溪石白
                secondary = Color(0xFF0288D1) // 下部清流湛蓝
            )
            "fireplace" -> SoundIconPalette(
                primary = Color(0xFFFF5722), // 烈焰橙红
                secondary = Color(0xFFFFC107) // 火星金黄
            )
            "birds" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFFFFA726) // 晨光暖橙羽
            )
            "summer_night" -> SoundIconPalette(
                primary = Color(0xFFFFF59D), // 皎洁月牙金白
                secondary = Color(0xFF7E57C2) // 静谧夜空紫
            )
            "white_noise" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFF90A4AE) // 冷调全频银灰
            )
            "waves" -> SoundIconPalette(
                primary = lightPrimary, // 拍岸浪花白
                secondary = Color(0xFF1565C0) // 蔚蓝深海
            )
            "coffee_shop" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFFFF8E1) else Color(0xFF3E2723), // 奶泡暖白/浓缩深棕
                secondary = Color(0xFF8D6E63) // 烘焙焦糖棕
            )
            "train" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFECEFF1) else Color(0xFF37474F), // 钢轨银白
                secondary = Color(0xFFFF9800) // 信号灯琥珀金
            )
            "boat" -> SoundIconPalette(
                primary = Color(0xFFA1887F), // 原木暖棕
                secondary = Color(0xFF00ACC1) // 碧波湖水蓝
            )
            "pink_noise" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFFF06292) // 珊瑚柔粉
            )
            "city" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFCFD8DC) else Color(0xFF263238), // 建筑冷灰
                secondary = Color(0xFFFFB300) // 霓虹金顶
            )
            "brown_noise" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFB0BEC5) else Color(0xFF4E342E), // 岩石灰
                secondary = Color(0xFF6D4C41) // 大地泥土暖褐
            )
            else -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFF19B2A6) // 椒盐海盐冰青
            )
        }
    }

    /**
     * Compute the gentle tinted-idle palette preserving natural semantics at 48% opacity.
     */
    fun getIdlePalette(trackId: String, isDark: Boolean, alpha: Float = 0.48f): SoundIconPalette {
        return getPalette(trackId, isDark).toIdlePalette(alpha)
    }
}

/**
 * Category-level Bauhaus natural semantic color mapping.
 */
fun SoundCategory.getThemeColor(): Color = when (this) {
    SoundCategory.ALL -> Color(0xFF60A5FA)      // 系统蓝
    SoundCategory.RAIN -> Color(0xFF38BDF8)     // 天青蓝
    SoundCategory.NATURE -> Color(0xFF34D399)   // 苍翠绿
    SoundCategory.LIFE -> Color(0xFFFB923C)     // 咖啡暖褐/琥珀
    SoundCategory.NOISE -> Color(0xFFF472B6)    // 柔粉
}

/**
 * High-readability content color for text/icons ensuring WCAG compliance across light/dark themes.
 */
fun SoundCategory.getContentColor(isDark: Boolean): Color {
    val base = getThemeColor()
    return if (isDark) {
        base
    } else {
        when (this) {
            SoundCategory.ALL -> Color(0xFF2563EB)
            SoundCategory.RAIN -> Color(0xFF0284C7)
            SoundCategory.NATURE -> Color(0xFF059669)
            SoundCategory.LIFE -> Color(0xFFD97706)
            SoundCategory.NOISE -> Color(0xFFDB2777)
        }
    }
}

/**
 * Unified Bauhaus acoustic minimalist vector symbol system.
 * Replaces colorful Unicode emojis with clean geometric acoustic elements (points, lines, planes, waveforms)
 * mapped to skeuomorphic natural color palettes.
 *
 * Color Specification:
 * - Idle / Inactive (未激活): Tinted Idle (呼吸微色) preserving the sound's semantic two-tone palette at 48% opacity,
 *   presenting gentle luminescent accents in dark mode and soft watercolor tones in light mode.
 * - Active (激活): High-contrast 100% full-saturation two-tone skeuomorphic natural palette corresponding to the sound's real essence.
 */
@Composable
fun BauhausSoundIcon(
    trackId: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier.size(28.dp),
    tint: Color? = null
) {
    val isDark = SaltTheme.configs.isDarkTheme
    val activePalette = BauhausSoundTheme.getPalette(trackId, isDark)
    val idlePalette = activePalette.toIdlePalette(0.48f)

    val targetPrimary = tint ?: if (isPlaying) activePalette.primary else idlePalette.primary
    val targetSecondary = tint ?: if (isPlaying) activePalette.secondary else idlePalette.secondary

    val animatedPrimary by animateColorAsState(
        targetValue = targetPrimary,
        animationSpec = tween(durationMillis = 180),
        label = "bauhaus_primary_color"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = 180),
        label = "bauhaus_secondary_color"
    )

    Canvas(modifier = modifier) {
        drawBauhausSymbol(
            trackId = trackId,
            primaryColor = animatedPrimary,
            secondaryColor = animatedSecondary,
            isPlaying = isPlaying
        )
    }
}

private fun DrawScope.drawBauhausSymbol(
    trackId: String,
    primaryColor: Color,
    secondaryColor: Color,
    isPlaying: Boolean
) {
    val w = size.width
    val h = size.height
    val strokeWidth = (w * 0.088f).coerceAtLeast(1.8.dp.toPx())
    val strokeP = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val strokeS = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStrokeS = Stroke(width = strokeWidth * 0.72f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (trackId) {
        "rain" -> {
            // 3 Parallel rhythmic tilted rain acoustic rays (-60 degrees): Outer 2 White, Center Cyan
            drawLine(primaryColor, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.16f, h * 0.72f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.54f, h * 0.18f), Offset(w * 0.42f, h * 0.78f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.80f, h * 0.30f), Offset(w * 0.68f, h * 0.74f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Rain impact droplet node (Cyan)
            drawCircle(secondaryColor, radius = strokeWidth * 0.95f, center = Offset(w * 0.40f, h * 0.88f))
        }

        "storm" -> {
            // Lightning fracture polyline (Cyan)
            val path = Path().apply {
                moveTo(w * 0.58f, h * 0.14f)
                lineTo(w * 0.36f, h * 0.46f)
                lineTo(w * 0.64f, h * 0.46f)
                lineTo(w * 0.42f, h * 0.86f)
            }
            drawPath(path, secondaryColor, style = strokeS)
            // Secondary rain slash (White)
            drawLine(primaryColor, Offset(w * 0.20f, h * 0.38f), Offset(w * 0.14f, h * 0.74f), strokeWidth = strokeWidth * 0.8f, cap = StrokeCap.Round)
            // Surge impact dot (White)
            drawCircle(primaryColor, radius = strokeWidth * 0.9f, center = Offset(w * 0.80f, h * 0.68f))
        }

        "wind" -> {
            // 2 Aerodynamic fluid streamlines: Upper White, Lower Cyan
            val p1 = Path().apply {
                moveTo(w * 0.14f, h * 0.36f)
                cubicTo(w * 0.38f, h * 0.22f, w * 0.62f, h * 0.48f, w * 0.86f, h * 0.34f)
            }
            drawPath(p1, primaryColor, style = strokeP)
            val p2 = Path().apply {
                moveTo(w * 0.22f, h * 0.64f)
                cubicTo(w * 0.44f, h * 0.52f, w * 0.66f, h * 0.74f, w * 0.78f, h * 0.64f)
            }
            drawPath(p2, secondaryColor, style = strokeS)
            // Vortex dot (Cyan)
            drawCircle(secondaryColor, radius = strokeWidth * 0.8f, center = Offset(w * 0.86f, h * 0.62f))
        }

        "stream" -> {
            // Twin undulating sine ripple waves: Upper White (上白), Lower Cyan (下蓝)
            val p1 = Path().apply {
                moveTo(w * 0.14f, h * 0.42f)
                cubicTo(w * 0.32f, h * 0.26f, w * 0.50f, h * 0.56f, w * 0.68f, h * 0.42f)
                cubicTo(w * 0.76f, h * 0.36f, w * 0.82f, h * 0.46f, w * 0.86f, h * 0.42f)
            }
            drawPath(p1, primaryColor, style = strokeP)
            val p2 = Path().apply {
                moveTo(w * 0.14f, h * 0.64f)
                cubicTo(w * 0.32f, h * 0.48f, w * 0.50f, h * 0.78f, w * 0.68f, h * 0.64f)
                cubicTo(w * 0.76f, h * 0.58f, w * 0.82f, h * 0.68f, w * 0.86f, h * 0.64f)
            }
            drawPath(p2, secondaryColor, style = strokeS)
            // Suspended ripple droplet (Cyan)
            drawCircle(secondaryColor, radius = strokeWidth * 0.9f, center = Offset(w * 0.50f, h * 0.20f))
        }

        "fireplace" -> {
            // Geometric flame apex angles: Outer chevrons White, Center ray Cyan
            drawLine(primaryColor, Offset(w * 0.28f, h * 0.78f), Offset(w * 0.50f, h * 0.30f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.72f, h * 0.78f), Offset(w * 0.50f, h * 0.30f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f, h * 0.78f), Offset(w * 0.50f, h * 0.52f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Floating spark dots (Cyan)
            drawCircle(secondaryColor, radius = strokeWidth * 0.7f, center = Offset(w * 0.36f, h * 0.18f))
            drawCircle(secondaryColor, radius = strokeWidth * 0.9f, center = Offset(w * 0.64f, h * 0.22f))
        }

        "birds" -> {
            // Twin ascending gull wing arcs (White)
            val path = Path().apply {
                moveTo(w * 0.14f, h * 0.56f)
                quadraticTo(w * 0.32f, h * 0.30f, w * 0.50f, h * 0.50f)
                quadraticTo(w * 0.68f, h * 0.30f, w * 0.86f, h * 0.56f)
            }
            drawPath(path, primaryColor, style = strokeP)
            // Melodic chirp vocal tone dot (Cyan)
            drawCircle(secondaryColor, radius = strokeWidth * 0.95f, center = Offset(w * 0.50f, h * 0.26f))
        }

        "summer_night" -> {
            // Slender crescent moon arc on right (White)
            drawArc(
                color = primaryColor,
                startAngle = -75f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * 0.44f, h * 0.16f),
                size = Size(w * 0.42f, h * 0.68f),
                style = strokeP
            )
            // Concentric acoustic pulse rings on left (Cyan)
            drawArc(
                color = secondaryColor,
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.12f, h * 0.36f),
                size = Size(w * 0.40f, h * 0.40f),
                style = thinStrokeS
            )
            drawCircle(secondaryColor, radius = strokeWidth * 0.85f, center = Offset(w * 0.32f, h * 0.56f))
        }

        "white_noise" -> {
            // Full-spectrum balanced frequency raster bars: Alternating White and Cyan
            drawLine(primaryColor, Offset(w * 0.20f, h * 0.26f), Offset(w * 0.80f, h * 0.26f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.12f, h * 0.42f), Offset(w * 0.88f, h * 0.42f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.24f, h * 0.58f), Offset(w * 0.76f, h * 0.58f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.16f, h * 0.74f), Offset(w * 0.84f, h * 0.74f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Vertical balance coordinate axis (White)
            drawLine(primaryColor, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.86f), strokeWidth = strokeWidth * 0.5f, cap = StrokeCap.Round)
        }

        "waves" -> {
            // Swelling ocean crest wave curve (White)
            val wave = Path().apply {
                moveTo(w * 0.12f, h * 0.54f)
                cubicTo(w * 0.32f, h * 0.54f, w * 0.44f, h * 0.24f, w * 0.66f, h * 0.24f)
                cubicTo(w * 0.78f, h * 0.24f, w * 0.84f, h * 0.34f, w * 0.80f, h * 0.42f)
            }
            drawPath(wave, primaryColor, style = strokeP)
            // Tide horizontal baseline (Cyan)
            drawLine(secondaryColor, Offset(w * 0.12f, h * 0.76f), Offset(w * 0.88f, h * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * 0.85f, center = Offset(w * 0.34f, h * 0.76f))
        }

        "coffee_shop" -> {
            // Cup basin arc and saucer (White)
            val cup = Path().apply {
                moveTo(w * 0.24f, h * 0.52f)
                lineTo(w * 0.76f, h * 0.52f)
                cubicTo(w * 0.76f, h * 0.78f, w * 0.24f, h * 0.78f, w * 0.24f, h * 0.52f)
            }
            drawPath(cup, primaryColor, style = strokeP)
            drawLine(primaryColor, Offset(w * 0.18f, h * 0.84f), Offset(w * 0.82f, h * 0.84f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Rising twin steam S-curves (Cyan)
            val s1 = Path().apply {
                moveTo(w * 0.40f, h * 0.44f)
                cubicTo(w * 0.34f, h * 0.32f, w * 0.46f, h * 0.24f, w * 0.40f, h * 0.14f)
            }
            drawPath(s1, secondaryColor, style = thinStrokeS)
            val s2 = Path().apply {
                moveTo(w * 0.60f, h * 0.44f)
                cubicTo(w * 0.54f, h * 0.32f, w * 0.66f, h * 0.24f, w * 0.60f, h * 0.14f)
            }
            drawPath(s2, secondaryColor, style = thinStrokeS)
        }

        "train" -> {
            // Parallel rail tracks (White)
            drawLine(primaryColor, Offset(w * 0.32f, h * 0.14f), Offset(w * 0.32f, h * 0.86f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.68f, h * 0.14f), Offset(w * 0.68f, h * 0.86f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Transverse pulse sleepers (Cyan)
            drawLine(secondaryColor, Offset(w * 0.18f, h * 0.30f), Offset(w * 0.82f, h * 0.30f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.18f, h * 0.50f), Offset(w * 0.82f, h * 0.50f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.18f, h * 0.70f), Offset(w * 0.82f, h * 0.70f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }

        "boat" -> {
            // Hull waterline arc (White)
            val hull = Path().apply {
                moveTo(w * 0.16f, h * 0.58f)
                cubicTo(w * 0.40f, h * 0.76f, w * 0.60f, h * 0.76f, w * 0.84f, h * 0.52f)
            }
            drawPath(hull, primaryColor, style = strokeP)
            // Tilted oar vector (Cyan)
            drawLine(secondaryColor, Offset(w * 0.32f, h * 0.26f), Offset(w * 0.66f, h * 0.82f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Water ripple (Lake Blue)
            drawLine(secondaryColor, Offset(w * 0.24f, h * 0.84f), Offset(w * 0.76f, h * 0.84f), strokeWidth = strokeWidth * 0.75f, cap = StrokeCap.Round)
        }

        "pink_noise" -> {
            // 1/f descending slope curve (White)
            val slope = Path().apply {
                moveTo(w * 0.16f, h * 0.26f)
                cubicTo(w * 0.38f, h * 0.44f, w * 0.62f, h * 0.64f, w * 0.84f, h * 0.74f)
            }
            drawPath(slope, primaryColor, style = strokeP)
            // Stepped frequency energy bars & dot (Cyan)
            drawLine(secondaryColor, Offset(w * 0.24f, h * 0.46f), Offset(w * 0.46f, h * 0.46f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.44f, h * 0.60f), Offset(w * 0.68f, h * 0.60f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * 0.85f, center = Offset(w * 0.84f, h * 0.74f))
        }

        "city" -> {
            // 3 Bauhaus staggered skyline towers: Outer towers White, Center high tower Cyan
            val h1 = h * 0.36f
            val h2 = h * 0.62f
            val h3 = h * 0.24f
            val base = h * 0.82f
            drawRect(primaryColor, Offset(w * 0.18f, base - h1), Size(w * 0.18f, h1), style = strokeP)
            drawRect(secondaryColor, Offset(w * 0.42f, base - h2), Size(w * 0.18f, h2), style = strokeS)
            drawRect(primaryColor, Offset(w * 0.66f, base - h3), Size(w * 0.18f, h3), style = strokeP)
            drawLine(primaryColor, Offset(w * 0.10f, base), Offset(w * 0.90f, base), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * 0.75f, center = Offset(w * 0.51f, base - h2 - h * 0.08f))
        }

        "brown_noise" -> {
            // Heavyweight foundation bar (White)
            val baseLine = h * 0.76f
            drawLine(primaryColor, Offset(w * 0.14f, baseLine), Offset(w * 0.86f, baseLine), strokeWidth = strokeWidth * 1.8f, cap = StrokeCap.Round)
            // Slow low-frequency deep sine wave (Cyan)
            val deepWave = Path().apply {
                moveTo(w * 0.14f, h * 0.36f)
                cubicTo(w * 0.32f, h * 0.16f, w * 0.50f, h * 0.56f, w * 0.68f, h * 0.36f)
                cubicTo(w * 0.76f, h * 0.28f, w * 0.82f, h * 0.40f, w * 0.86f, h * 0.36f)
            }
            drawPath(deepWave, secondaryColor, style = strokeS)
            drawCircle(secondaryColor, radius = strokeWidth * 1.1f, center = Offset(w * 0.50f, h * 0.54f))
        }

        else -> {
            // Master / Headphone acoustic symbol: Headband White, Harmonic arc & center dot Cyan
            // Headphone arc (White)
            drawArc(
                color = primaryColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.20f, h * 0.18f),
                size = Size(w * 0.60f, h * 0.60f),
                style = strokeP
            )
            // Left & Right earcups (White)
            drawLine(primaryColor, Offset(w * 0.20f, h * 0.48f), Offset(w * 0.20f, h * 0.74f), strokeWidth = strokeWidth * 1.4f, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.80f, h * 0.48f), Offset(w * 0.80f, h * 0.74f), strokeWidth = strokeWidth * 1.4f, cap = StrokeCap.Round)
            // Central harmonic ripple arc (Cyan)
            drawArc(
                color = secondaryColor,
                startAngle = 30f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(w * 0.34f, h * 0.48f),
                size = Size(w * 0.32f, h * 0.24f),
                style = thinStrokeS
            )
            drawCircle(secondaryColor, radius = strokeWidth * 0.8f, center = Offset(w * 0.50f, h * 0.54f))
        }
    }
}
