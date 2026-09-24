package com.whitenoise.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
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
            "fan" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFF00ACC1) // 微风冰青
            )
            "clock" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFFFF8E1) else Color(0xFF37474F), // 象牙白/深灰
                secondary = Color(0xFFF59E0B) // 表盘暖金
            )
            "keyboard" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFECEFF1) else Color(0xFF263238), // 键帽冷银/碳黑
                secondary = Color(0xFF818CF8) // 极客霓虹蓝紫
            )
            "wind_chimes" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFFFBBF24) // 黄铜金响
            )
            "rain_roof" -> SoundIconPalette(
                primary = if (isDark) Color(0xFFCFD8DC) else Color(0xFF37474F), // 屋檐石板灰
                secondary = Color(0xFF38BDF8) // 雨滴天青
            )
            "underwater" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFF0284C7) // 深海蔚蓝
            )
            "green_noise" -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFF10B981) // 森林翡翠绿
            )
            else -> SoundIconPalette(
                primary = lightPrimary,
                secondary = Color(0xFF19B2A6) // 椒盐海盐冰青
            )
        }
    }

    /**
     * Compute clean monochrome/grayscale idle palette (pure black/white tones without saturation)
     * restoring the peaceful sketch-like state before a sound is activated.
     */
    fun getMonochromeIdlePalette(isDark: Boolean): SoundIconPalette {
        return if (isDark) {
            SoundIconPalette(
                primary = Color.White.copy(alpha = 0.38f),
                secondary = Color.White.copy(alpha = 0.22f)
            )
        } else {
            SoundIconPalette(
                primary = Color(0xFF212121).copy(alpha = 0.38f),
                secondary = Color(0xFF212121).copy(alpha = 0.22f)
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
 * - Idle / Inactive (未激活): Pure monochrome black/white/gray sketch tones, quiet and understated.
 * - Active (激活): High-contrast full-saturation two-tone skeuomorphic natural palette corresponding to the sound's essence.
 *
 * Dynamic Morphing:
 * - Activation triggers a lively non-linear spring morphing animation (breathe scaling & parametric expansion)
 *   bringing the geometric symbols to life.
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
    val monoIdlePalette = BauhausSoundTheme.getMonochromeIdlePalette(isDark)

    val targetPrimary = tint ?: if (isPlaying) activePalette.primary else monoIdlePalette.primary
    val targetSecondary = tint ?: if (isPlaying) activePalette.secondary else monoIdlePalette.secondary

    val animatedPrimary by animateColorAsState(
        targetValue = targetPrimary,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "bauhaus_primary_color"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "bauhaus_secondary_color"
    )

    // Non-linear spring rate variation morphing animation
    val morphProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.58f, // 变奏弹性回弹（带生命力的弹簧），不呆板
            stiffness = 320f      // 敏捷响应
        ),
        label = "bauhaus_sound_morph"
    )

    Canvas(modifier = modifier) {
        val breatheScale = 0.94f + morphProgress * 0.06f + (morphProgress - 1f).coerceAtLeast(0f) * 0.20f
        scale(scale = breatheScale, pivot = center) {
            drawBauhausSymbol(
                trackId = trackId,
                primaryColor = animatedPrimary,
                secondaryColor = animatedSecondary,
                isPlaying = isPlaying,
                morphProgress = morphProgress
            )
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private fun DrawScope.drawBauhausSymbol(
    trackId: String,
    primaryColor: Color,
    secondaryColor: Color,
    isPlaying: Boolean,
    morphProgress: Float = 1.0f
) {
    val w = size.width
    val h = size.height
    val strokeWidth = (w * 0.088f).coerceAtLeast(1.8.dp.toPx())
    val strokeP = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val strokeS = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStrokeS = Stroke(width = strokeWidth * 0.72f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (trackId) {
        "rain" -> {
            // 3 Parallel rhythmic tilted rain acoustic rays: length and droplet expand dynamically
            val lenFactor = lerp(0.70f, 1.0f, morphProgress)
            val dropY = lerp(h * 0.80f, h * 0.88f, morphProgress)
            val dropRadius = strokeWidth * lerp(0.65f, 0.95f, morphProgress)

            val r1Start = Offset(lerp(w * 0.22f, w * 0.28f, lenFactor), lerp(h * 0.50f, h * 0.28f, lenFactor))
            val r1End = Offset(lerp(w * 0.22f, w * 0.16f, lenFactor), lerp(h * 0.50f, h * 0.72f, lenFactor))
            drawLine(primaryColor, r1Start, r1End, strokeWidth = strokeWidth, cap = StrokeCap.Round)

            val r2Start = Offset(lerp(w * 0.48f, w * 0.54f, lenFactor), lerp(h * 0.48f, h * 0.18f, lenFactor))
            val r2End = Offset(lerp(w * 0.48f, w * 0.42f, lenFactor), lerp(h * 0.48f, h * 0.78f, lenFactor))
            drawLine(secondaryColor, r2Start, r2End, strokeWidth = strokeWidth, cap = StrokeCap.Round)

            val r3Start = Offset(lerp(w * 0.74f, w * 0.80f, lenFactor), lerp(h * 0.52f, h * 0.30f, lenFactor))
            val r3End = Offset(lerp(w * 0.74f, w * 0.68f, lenFactor), lerp(h * 0.52f, h * 0.74f, lenFactor))
            drawLine(primaryColor, r3Start, r3End, strokeWidth = strokeWidth, cap = StrokeCap.Round)

            drawCircle(secondaryColor, radius = dropRadius, center = Offset(w * 0.40f, dropY))
        }

        "storm" -> {
            // Lightning fracture polyline extending downward with vibration
            val tipX = lerp(w * 0.50f, w * 0.42f, morphProgress)
            val tipY = lerp(h * 0.68f, h * 0.86f, morphProgress)
            val path = Path().apply {
                moveTo(w * 0.58f, h * 0.14f)
                lineTo(w * 0.36f, h * 0.46f)
                lineTo(w * 0.64f, h * 0.46f)
                lineTo(tipX, tipY)
            }
            drawPath(path, secondaryColor, style = strokeS)
            // Secondary rain slash
            drawLine(primaryColor, Offset(w * 0.20f, h * 0.38f), Offset(w * 0.14f, h * 0.74f), strokeWidth = strokeWidth * 0.8f, cap = StrokeCap.Round)
            // Surge impact dot pulsing
            val sparkRadius = strokeWidth * lerp(0.50f, 0.95f, morphProgress)
            drawCircle(primaryColor, radius = sparkRadius, center = Offset(w * 0.80f, h * 0.68f))
        }

        "wind" -> {
            // 2 Aerodynamic fluid streamlines with subtle horizontal airflow shift
            val flowShift = (1f - morphProgress) * (-w * 0.08f)
            val p1 = Path().apply {
                moveTo(w * 0.14f + flowShift, h * 0.36f)
                cubicTo(w * 0.38f, lerp(h * 0.30f, h * 0.22f, morphProgress), w * 0.62f, lerp(h * 0.40f, h * 0.48f, morphProgress), w * 0.86f + flowShift, h * 0.34f)
            }
            drawPath(p1, primaryColor, style = strokeP)
            val p2 = Path().apply {
                moveTo(w * 0.22f + flowShift, h * 0.64f)
                cubicTo(w * 0.44f, lerp(h * 0.58f, h * 0.52f, morphProgress), w * 0.66f, lerp(h * 0.68f, h * 0.74f, morphProgress), w * 0.78f + flowShift, h * 0.64f)
            }
            drawPath(p2, secondaryColor, style = strokeS)
            // Vortex dot
            val vortexX = lerp(w * 0.80f, w * 0.86f, morphProgress)
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.50f, 0.85f, morphProgress), center = Offset(vortexX, h * 0.62f))
        }

        "stream" -> {
            // Twin undulating sine ripple waves with amplitude morphing
            val waveAmp = lerp(0.60f, 1.0f, morphProgress)
            val p1 = Path().apply {
                moveTo(w * 0.14f, h * 0.42f)
                cubicTo(w * 0.32f, h * 0.42f - (h * 0.16f * waveAmp), w * 0.50f, h * 0.42f + (h * 0.14f * waveAmp), w * 0.68f, h * 0.42f)
                cubicTo(w * 0.76f, h * 0.42f - (h * 0.06f * waveAmp), w * 0.82f, h * 0.42f + (h * 0.04f * waveAmp), w * 0.86f, h * 0.42f)
            }
            drawPath(p1, primaryColor, style = strokeP)
            val p2 = Path().apply {
                moveTo(w * 0.14f, h * 0.64f)
                cubicTo(w * 0.32f, h * 0.64f - (h * 0.16f * waveAmp), w * 0.50f, h * 0.64f + (h * 0.14f * waveAmp), w * 0.68f, h * 0.64f)
                cubicTo(w * 0.76f, h * 0.64f - (h * 0.06f * waveAmp), w * 0.82f, h * 0.64f + (h * 0.04f * waveAmp), w * 0.86f, h * 0.64f)
            }
            drawPath(p2, secondaryColor, style = strokeS)
            // Suspended ripple droplet leaping up
            val dropletY = lerp(h * 0.28f, h * 0.20f, morphProgress)
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.60f, 0.90f, morphProgress), center = Offset(w * 0.50f, dropletY))
        }

        "fireplace" -> {
            // Geometric flame apex angles: Outer chevrons and leaping sparks
            val flameSpread = lerp(0.06f, 0f, morphProgress)
            val apexY = lerp(h * 0.40f, h * 0.30f, morphProgress)
            drawLine(primaryColor, Offset(w * (0.28f + flameSpread), h * 0.78f), Offset(w * 0.50f, apexY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * (0.72f - flameSpread), h * 0.78f), Offset(w * 0.50f, apexY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f, h * 0.78f), Offset(w * 0.50f, lerp(h * 0.62f, h * 0.52f, morphProgress)), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Floating spark dots rising upward
            val spark1Y = lerp(h * 0.28f, h * 0.18f, morphProgress)
            val spark2Y = lerp(h * 0.32f, h * 0.22f, morphProgress)
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.50f, 0.75f, morphProgress), center = Offset(w * 0.36f, spark1Y))
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.60f, 0.95f, morphProgress), center = Offset(w * 0.64f, spark2Y))
        }

        "birds" -> {
            // Twin ascending gull wing arcs spreading with lift
            val wingLift = lerp(h * 0.08f, 0f, morphProgress)
            val path = Path().apply {
                moveTo(w * 0.14f, h * 0.56f + wingLift)
                quadraticTo(w * 0.32f, h * 0.30f - wingLift * 0.5f, w * 0.50f, h * 0.50f)
                quadraticTo(w * 0.68f, h * 0.30f - wingLift * 0.5f, w * 0.86f, h * 0.56f + wingLift)
            }
            drawPath(path, primaryColor, style = strokeP)
            // Melodic chirp vocal tone dot blooming
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.65f, 0.95f, morphProgress), center = Offset(w * 0.50f, lerp(h * 0.32f, h * 0.26f, morphProgress)))
        }

        "summer_night" -> {
            // Crescent moon arc sweeping open
            val sweep = lerp(110f, 150f, morphProgress)
            drawArc(
                color = primaryColor,
                startAngle = -75f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(w * 0.44f, h * 0.16f),
                size = Size(w * 0.42f, h * 0.68f),
                style = strokeP
            )
            // Concentric acoustic pulse rings expanding
            val pulseScale = lerp(0.70f, 1.0f, morphProgress)
            drawArc(
                color = secondaryColor,
                startAngle = 135f,
                sweepAngle = 90f * pulseScale,
                useCenter = false,
                topLeft = Offset(w * 0.12f, h * 0.36f),
                size = Size(w * 0.40f * pulseScale, h * 0.40f * pulseScale),
                style = thinStrokeS
            )
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.60f, 0.85f, morphProgress), center = Offset(w * 0.32f, h * 0.56f))
        }

        "white_noise" -> {
            // Full-spectrum balanced frequency raster bars: dynamic expansion from center
            val scaleFactor = lerp(0.65f, 1.0f, morphProgress)
            val hw1 = w * 0.30f * scaleFactor
            val hw2 = w * 0.38f * scaleFactor
            val hw3 = w * 0.26f * scaleFactor
            val hw4 = w * 0.34f * scaleFactor
            drawLine(primaryColor, Offset(w * 0.50f - hw1, h * 0.26f), Offset(w * 0.50f + hw1, h * 0.26f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f - hw2, h * 0.42f), Offset(w * 0.50f + hw2, h * 0.42f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.50f - hw3, h * 0.58f), Offset(w * 0.50f + hw3, h * 0.58f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f - hw4, h * 0.74f), Offset(w * 0.50f + hw4, h * 0.74f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Vertical balance coordinate axis
            drawLine(primaryColor, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.86f), strokeWidth = strokeWidth * 0.5f, cap = StrokeCap.Round)
        }

        "waves" -> {
            // Swelling ocean crest wave curve
            val crestY = lerp(h * 0.34f, h * 0.24f, morphProgress)
            val wave = Path().apply {
                moveTo(w * 0.12f, h * 0.54f)
                cubicTo(w * 0.32f, h * 0.54f, w * 0.44f, crestY, w * 0.66f, crestY)
                cubicTo(w * 0.78f, crestY, w * 0.84f, h * 0.34f, w * 0.80f, h * 0.42f)
            }
            drawPath(wave, primaryColor, style = strokeP)
            // Tide horizontal baseline expanding
            val tideRetract = lerp(w * 0.18f, 0f, morphProgress)
            drawLine(secondaryColor, Offset(w * 0.12f + tideRetract * 0.5f, h * 0.76f), Offset(w * 0.88f - tideRetract * 0.5f, h * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.60f, 0.85f, morphProgress), center = Offset(w * 0.34f, h * 0.76f))
        }

        "coffee_shop" -> {
            // Cup basin arc and saucer
            val cup = Path().apply {
                moveTo(w * 0.24f, h * 0.52f)
                lineTo(w * 0.76f, h * 0.52f)
                cubicTo(w * 0.76f, h * 0.78f, w * 0.24f, h * 0.78f, w * 0.24f, h * 0.52f)
            }
            drawPath(cup, primaryColor, style = strokeP)
            drawLine(primaryColor, Offset(w * 0.18f, h * 0.84f), Offset(w * 0.82f, h * 0.84f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Rising twin steam S-curves lifting upward
            val steamLift = lerp(h * 0.08f, 0f, morphProgress)
            val s1 = Path().apply {
                moveTo(w * 0.40f, h * 0.44f)
                cubicTo(w * 0.34f, h * 0.32f + steamLift, w * 0.46f, h * 0.24f + steamLift, w * 0.40f, h * 0.14f + steamLift)
            }
            drawPath(s1, secondaryColor, style = thinStrokeS)
            val s2 = Path().apply {
                moveTo(w * 0.60f, h * 0.44f)
                cubicTo(w * 0.54f, h * 0.32f + steamLift, w * 0.66f, h * 0.24f + steamLift, w * 0.60f, h * 0.14f + steamLift)
            }
            drawPath(s2, secondaryColor, style = thinStrokeS)
        }

        "train" -> {
            // Parallel rail tracks
            drawLine(primaryColor, Offset(w * 0.32f, h * 0.14f), Offset(w * 0.32f, h * 0.86f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.68f, h * 0.14f), Offset(w * 0.68f, h * 0.86f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Transverse pulse sleepers expanding
            val sleeperSpread = lerp(0.75f, 1.0f, morphProgress)
            val sHalfW = w * 0.32f * sleeperSpread
            drawLine(secondaryColor, Offset(w * 0.50f - sHalfW, h * 0.30f), Offset(w * 0.50f + sHalfW, h * 0.30f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f - sHalfW, h * 0.50f), Offset(w * 0.50f + sHalfW, h * 0.50f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f - sHalfW, h * 0.70f), Offset(w * 0.50f + sHalfW, h * 0.70f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }

        "boat" -> {
            // Hull waterline arc
            val hull = Path().apply {
                moveTo(w * 0.16f, h * 0.58f)
                cubicTo(w * 0.40f, h * 0.76f, w * 0.60f, h * 0.76f, w * 0.84f, h * 0.52f)
            }
            drawPath(hull, primaryColor, style = strokeP)
            // Tilted oar vector paddling
            val oarX = lerp(w * 0.58f, w * 0.66f, morphProgress)
            drawLine(secondaryColor, Offset(w * 0.32f, h * 0.26f), Offset(oarX, h * 0.82f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Water ripple
            val rippleW = lerp(w * 0.18f, w * 0.26f, morphProgress)
            drawLine(secondaryColor, Offset(w * 0.50f - rippleW, h * 0.84f), Offset(w * 0.50f + rippleW, h * 0.84f), strokeWidth = strokeWidth * 0.75f, cap = StrokeCap.Round)
        }

        "pink_noise" -> {
            // 1/f descending slope curve
            val slope = Path().apply {
                moveTo(w * 0.16f, h * 0.26f)
                cubicTo(w * 0.38f, h * 0.44f, w * 0.62f, h * 0.64f, w * 0.84f, h * 0.74f)
            }
            drawPath(slope, primaryColor, style = strokeP)
            // Stepped frequency energy bars expanding
            val stepW1 = lerp(w * 0.10f, w * 0.22f, morphProgress)
            val stepW2 = lerp(w * 0.12f, w * 0.24f, morphProgress)
            drawLine(secondaryColor, Offset(w * 0.24f, h * 0.46f), Offset(w * 0.24f + stepW1, h * 0.46f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.44f, h * 0.60f), Offset(w * 0.44f + stepW2, h * 0.60f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.60f, 0.85f, morphProgress), center = Offset(w * 0.84f, h * 0.74f))
        }

        "city" -> {
            // 3 Bauhaus staggered skyline towers: expanding upward from base
            val growth = lerp(0.70f, 1.0f, morphProgress)
            val h1 = h * 0.36f * growth
            val h2 = h * 0.62f * growth
            val h3 = h * 0.24f * growth
            val base = h * 0.82f
            drawRect(primaryColor, Offset(w * 0.18f, base - h1), Size(w * 0.18f, h1), style = strokeP)
            drawRect(secondaryColor, Offset(w * 0.42f, base - h2), Size(w * 0.18f, h2), style = strokeS)
            drawRect(primaryColor, Offset(w * 0.66f, base - h3), Size(w * 0.18f, h3), style = strokeP)
            drawLine(primaryColor, Offset(w * 0.10f, base), Offset(w * 0.90f, base), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.50f, 0.75f, morphProgress), center = Offset(w * 0.51f, base - h2 - h * 0.08f))
        }

        "brown_noise" -> {
            // Heavyweight foundation bar
            val baseLine = h * 0.76f
            drawLine(primaryColor, Offset(w * 0.14f, baseLine), Offset(w * 0.86f, baseLine), strokeWidth = strokeWidth * 1.8f, cap = StrokeCap.Round)
            // Slow low-frequency deep sine wave undulating
            val deepAmp = lerp(0.55f, 1.0f, morphProgress)
            val deepWave = Path().apply {
                moveTo(w * 0.14f, h * 0.36f)
                cubicTo(w * 0.32f, h * 0.36f - (h * 0.20f * deepAmp), w * 0.50f, h * 0.36f + (h * 0.20f * deepAmp), w * 0.68f, h * 0.36f)
                cubicTo(w * 0.76f, h * 0.36f - (h * 0.08f * deepAmp), w * 0.82f, h * 0.36f + (h * 0.04f * deepAmp), w * 0.86f, h * 0.36f)
            }
            drawPath(deepWave, secondaryColor, style = strokeS)
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.70f, 1.1f, morphProgress), center = Offset(w * 0.50f, h * 0.54f))
        }

        "fan" -> {
            // Rotating aerodynamic 3-blade fan propeller with protective circular rim
            val rimRadius = w * 0.38f
            drawCircle(primaryColor, radius = rimRadius, center = center, style = strokeP)
            val rotAngle = lerp(0f, 60f, morphProgress)
            rotate(degrees = rotAngle, pivot = center) {
                for (angle in listOf(0f, 120f, 240f)) {
                    val rad = Math.toRadians(angle.toDouble())
                    val tipX = center.x + (rimRadius * 0.82f) * Math.cos(rad).toFloat()
                    val tipY = center.y + (rimRadius * 0.82f) * Math.sin(rad).toFloat()
                    drawLine(secondaryColor, center, Offset(tipX, tipY), strokeWidth = strokeWidth * 1.1f, cap = StrokeCap.Round)
                }
            }
            drawCircle(secondaryColor, radius = strokeWidth * 0.9f, center = center)
        }

        "clock" -> {
            // Bauhaus clock dial + 90-degree hands + suspension top crown
            val dialRadius = w * 0.34f
            drawCircle(primaryColor, radius = dialRadius, center = center, style = strokeP)
            // Top crown
            drawLine(primaryColor, Offset(center.x, center.y - dialRadius), Offset(center.x, center.y - dialRadius - h * 0.08f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Hour hand (pointing to 12)
            drawLine(secondaryColor, center, Offset(center.x, center.y - dialRadius * 0.58f), strokeWidth = strokeWidth * 1.1f, cap = StrokeCap.Round)
            // Minute hand (pointing to 3 with slight morph tick)
            val tickAngle = lerp(0f, 15f, morphProgress)
            val minLen = dialRadius * 0.78f
            val minRad = Math.toRadians(tickAngle.toDouble())
            val minX = center.x + minLen * Math.cos(minRad).toFloat()
            val minY = center.y + minLen * Math.sin(minRad).toFloat()
            drawLine(secondaryColor, center, Offset(minX, minY), strokeWidth = strokeWidth * 0.85f, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * 0.65f, center = center)
        }

        "keyboard" -> {
            // Stepped isometric mechanical keycaps with actuation bounce
            val keyW = w * 0.28f
            val keyH = h * 0.30f
            val corner = CornerRadius(4.dp.toPx())
            // Key 1 (Base key on left)
            drawRoundRect(primaryColor, topLeft = Offset(w * 0.16f, h * 0.34f), size = Size(keyW, keyH), cornerRadius = corner, style = strokeP)
            // Key 2 (Active key on right pressed down with bounce)
            val key2Y = lerp(h * 0.26f, h * 0.36f, morphProgress)
            drawRoundRect(secondaryColor, topLeft = Offset(w * 0.54f, key2Y), size = Size(keyW, keyH), cornerRadius = corner, style = strokeS)
            // Bottom chassis line
            val baseLine = h * 0.78f
            drawLine(primaryColor, Offset(w * 0.12f, baseLine), Offset(w * 0.88f, baseLine), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Actuation spark dot
            drawCircle(secondaryColor, radius = strokeWidth * 0.60f, center = Offset(w * 0.68f, key2Y + keyH * 0.50f))
        }

        "wind_chimes" -> {
            // Horizontal suspension bar + 3 staggered vertical chime tubes + swinging pendulum clapper
            val barY = h * 0.22f
            drawLine(primaryColor, Offset(w * 0.16f, barY), Offset(w * 0.84f, barY), strokeWidth = strokeWidth * 1.1f, cap = StrokeCap.Round)
            // 3 chime rods
            drawLine(secondaryColor, Offset(w * 0.30f, barY), Offset(w * 0.30f, h * 0.54f), strokeWidth = strokeWidth * 0.85f, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f, barY), Offset(w * 0.50f, h * 0.68f), strokeWidth = strokeWidth * 0.85f, cap = StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.70f, barY), Offset(w * 0.70f, h * 0.46f), strokeWidth = strokeWidth * 0.85f, cap = StrokeCap.Round)
            // Center swinging string & leaf pendant
            val swingX = lerp(w * 0.50f, w * 0.58f, morphProgress)
            drawLine(primaryColor, Offset(w * 0.50f, h * 0.68f), Offset(swingX, h * 0.84f), strokeWidth = strokeWidth * 0.55f, cap = StrokeCap.Round)
            drawCircle(secondaryColor, radius = strokeWidth * 0.75f, center = Offset(swingX, h * 0.84f))
        }

        "rain_roof" -> {
            // Slanted roof eaves line with eaves edge
            val roofLine = Path().apply {
                moveTo(w * 0.12f, h * 0.32f)
                lineTo(w * 0.84f, h * 0.42f)
                lineTo(w * 0.80f, h * 0.52f)
            }
            drawPath(roofLine, primaryColor, style = strokeP)
            // 3 Dripping raindrops falling with vertical morphing
            val drop1Y = lerp(h * 0.52f, h * 0.68f, morphProgress)
            drawLine(secondaryColor, Offset(w * 0.28f, h * 0.46f), Offset(w * 0.28f, drop1Y), strokeWidth = strokeWidth * 0.80f, cap = StrokeCap.Round)
            val drop2Y = lerp(h * 0.60f, h * 0.78f, morphProgress)
            drawCircle(secondaryColor, radius = strokeWidth * 0.75f, center = Offset(w * 0.52f, drop2Y))
            val drop3Y = lerp(h * 0.56f, h * 0.74f, morphProgress)
            drawLine(secondaryColor, Offset(w * 0.76f, h * 0.52f), Offset(w * 0.76f, drop3Y), strokeWidth = strokeWidth * 0.80f, cap = StrokeCap.Round)
        }

        "underwater" -> {
            // Twin deep ocean undulating curves + rising aeration bubbles
            val waveAmp = lerp(0.65f, 1.0f, morphProgress)
            val wave1 = Path().apply {
                moveTo(w * 0.14f, h * 0.62f)
                cubicTo(w * 0.36f, h * 0.62f - (h * 0.12f * waveAmp), w * 0.64f, h * 0.62f + (h * 0.12f * waveAmp), w * 0.86f, h * 0.62f)
            }
            drawPath(wave1, primaryColor, style = strokeP)
            val wave2 = Path().apply {
                moveTo(w * 0.18f, h * 0.78f)
                cubicTo(w * 0.40f, h * 0.78f - (h * 0.08f * waveAmp), w * 0.62f, h * 0.78f + (h * 0.08f * waveAmp), w * 0.82f, h * 0.78f)
            }
            drawPath(wave2, secondaryColor, style = strokeS)
            // Rising bubbles
            val b1Y = lerp(h * 0.48f, h * 0.28f, morphProgress)
            drawCircle(secondaryColor, radius = strokeWidth * 0.80f, center = Offset(w * 0.36f, b1Y), style = strokeS)
            val b2Y = lerp(h * 0.58f, h * 0.40f, morphProgress)
            drawCircle(secondaryColor, radius = strokeWidth * 0.55f, center = Offset(w * 0.68f, b2Y), style = strokeS)
        }

        "green_noise" -> {
            // Natural 500Hz bell-curve spectrum bars dancing with spring morphing
            val baseLine = h * 0.80f
            val bars = listOf(
                w * 0.20f to 0.22f,
                w * 0.35f to 0.42f,
                w * 0.50f to 0.62f, // Central resonant peak
                w * 0.65f to 0.42f,
                w * 0.80f to 0.22f
            )
            // Base horizontal axis
            drawLine(primaryColor, Offset(w * 0.12f, baseLine), Offset(w * 0.88f, baseLine), strokeWidth = strokeWidth * 0.8f, cap = StrokeCap.Round)
            // Pulsing frequency energy bars
            for ((x, maxHFactor) in bars) {
                val currentH = h * maxHFactor * lerp(0.60f, 1.0f, morphProgress)
                val isPeak = x == w * 0.50f
                val barColor = if (isPeak) secondaryColor else primaryColor
                drawLine(barColor, Offset(x, baseLine), Offset(x, baseLine - currentH), strokeWidth = strokeWidth * 0.90f, cap = StrokeCap.Round)
            }
            // Energy peak dot
            drawCircle(secondaryColor, radius = strokeWidth * 0.75f, center = Offset(w * 0.50f, baseLine - h * 0.62f * lerp(0.60f, 1.0f, morphProgress) - strokeWidth))
        }

        else -> {
            // Master / Headphone acoustic symbol: Headband White, Harmonic arc & center dot Cyan
            drawArc(
                color = primaryColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.20f, h * 0.18f),
                size = Size(w * 0.60f, h * 0.60f),
                style = strokeP
            )
            drawLine(primaryColor, Offset(w * 0.20f, h * 0.48f), Offset(w * 0.20f, h * 0.74f), strokeWidth = strokeWidth * 1.4f, cap = StrokeCap.Round)
            drawLine(primaryColor, Offset(w * 0.80f, h * 0.48f), Offset(w * 0.80f, h * 0.74f), strokeWidth = strokeWidth * 1.4f, cap = StrokeCap.Round)
            val rippleScale = lerp(0.65f, 1.0f, morphProgress)
            drawArc(
                color = secondaryColor,
                startAngle = 30f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(w * (0.50f - 0.16f * rippleScale), h * 0.48f),
                size = Size(w * 0.32f * rippleScale, h * 0.24f * rippleScale),
                style = thinStrokeS
            )
            drawCircle(secondaryColor, radius = strokeWidth * lerp(0.60f, 0.80f, morphProgress), center = Offset(w * 0.50f, h * 0.54f))
        }
    }
}
