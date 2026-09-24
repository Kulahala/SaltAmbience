package com.whitenoise.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme

/**
 * Bauhaus UI Vector Symbols.
 * Replaces system emojis and crude Unicode text hacks with pure geometric points, lines, and planes.
 */
enum class BauhausUiSymbol {
    Play,
    Pause,
    Clear,
    ThemeSystem,
    ThemeLight,
    ThemeDark,
    Close,
    Check,
    Timer,
    Add,
    Restore,
    Import
}

/**
 * Maps ThemeMode to the corresponding Bauhaus UI vector symbol.
 */
fun com.whitenoise.app.core.model.ThemeMode.toBauhausSymbol(): BauhausUiSymbol = when (this) {
    com.whitenoise.app.core.model.ThemeMode.SYSTEM -> BauhausUiSymbol.ThemeSystem
    com.whitenoise.app.core.model.ThemeMode.LIGHT -> BauhausUiSymbol.ThemeLight
    com.whitenoise.app.core.model.ThemeMode.DARK -> BauhausUiSymbol.ThemeDark
}

/**
 * Semantic two-tone color palette for Bauhaus UI vector symbols.
 */
data class BauhausUiPalette(
    val primary: Color,
    val secondary: Color
)

object BauhausUiTheme {
    fun getPalette(symbol: BauhausUiSymbol, isDark: Boolean): BauhausUiPalette = when (symbol) {
        BauhausUiSymbol.ThemeLight -> BauhausUiPalette(
            primary = Color(0xFFF59E0B), // 暖阳金
            secondary = Color(0xFFFDE68A) // 光子亮黄
        )
        BauhausUiSymbol.ThemeDark -> BauhausUiPalette(
            primary = Color(0xFFFBBF24), // 月牙金
            secondary = Color(0xFFA78BFA) // 幽紫星芒
        )
        BauhausUiSymbol.ThemeSystem -> BauhausUiPalette(
            primary = Color(0xFF38BDF8), // 冰蓝
            secondary = if (isDark) Color.White else Color(0xFF475569) // 纯白 / 深灰
        )
        BauhausUiSymbol.Timer -> BauhausUiPalette(
            primary = Color(0xFF10B981), // 薄荷翡翠绿
            secondary = Color(0xFF34D399) // 亮薄荷绿
        )
        BauhausUiSymbol.Clear -> BauhausUiPalette(
            primary = Color(0xFFEF4444), // 珊瑚赤红 (深)
            secondary = Color(0xFFF87171) // 珊瑚赤红 (亮)
        )
        BauhausUiSymbol.Import -> BauhausUiPalette(
            primary = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), // 电光青蓝
            secondary = if (isDark) Color(0xFF7DD3FC) else Color(0xFF38BDF8)
        )
        BauhausUiSymbol.Add -> BauhausUiPalette(
            primary = Color(0xFF22C55E), // 活力亮绿
            secondary = Color(0xFF34D399) // 亮绿
        )
        BauhausUiSymbol.Restore -> BauhausUiPalette(
            primary = Color(0xFF60A5FA), // 灵动天青蓝
            secondary = Color(0xFF93C5FD) // 柔白天青蓝
        )
        BauhausUiSymbol.Play -> BauhausUiPalette(
            primary = Color(0xFF3B82F6), // 强调蓝
            secondary = Color.White // 纯白
        )
        BauhausUiSymbol.Pause -> BauhausUiPalette(
            primary = Color(0xFF3B82F6), // 强调蓝
            secondary = if (isDark) Color.White else Color(0xFF60A5FA) // 纯白 / 天蓝
        )
        BauhausUiSymbol.Close -> BauhausUiPalette(
            primary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), // 优雅灰度
            secondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF94A3B8)
        )
        BauhausUiSymbol.Check -> BauhausUiPalette(
            primary = Color(0xFF10B981), // 完成绿
            secondary = Color(0xFF34D399) // 完成亮绿
        )
    }
}

/**
 * Unified Bauhaus UI Vector Icon Component.
 * Pure Canvas rendering using geometric primitives with crisp aesthetic proportions and semantic two-tone palettes.
 */
@Composable
fun BauhausUiIcon(
    symbol: BauhausUiSymbol,
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color? = null
) {
    val isDark = SaltTheme.configs.isDarkTheme
    val palette = BauhausUiTheme.getPalette(symbol, isDark)
    val primaryColor = tint ?: palette.primary
    val secondaryColor = tint ?: palette.secondary

    Canvas(modifier = modifier) {
        drawBauhausUiSymbol(
            symbol = symbol,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }
}

/**
 * Bauhaus Acoustic Play <-> Pause Shape Morphing Icon.
 * Seamless geometric path morphing with non-linear spring physics:
 * - Play state (isPlaying = false): Single focused dynamic triangle pointing right.
 * - Pause state (isPlaying = true): Split into twin symmetric vertical Bauhaus pillars.
 * Rate variation spring animation gives it an organic mechanical tactile response.
 */
@Composable
fun BauhausPlayPauseMorphIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color? = null
) {
    val isDark = SaltTheme.configs.isDarkTheme
    val playPalette = BauhausUiTheme.getPalette(BauhausUiSymbol.Play, isDark)
    val pausePalette = BauhausUiTheme.getPalette(BauhausUiSymbol.Pause, isDark)

    val primaryColor = tint ?: (if (isPlaying) pausePalette.primary else playPalette.primary)
    val secondaryColor = tint ?: (if (isPlaying) pausePalette.secondary else playPalette.secondary)

    val morphProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.65f, // 变奏回弹，动感十足，非线性速度变化
            stiffness = 380f
        ),
        label = "play_pause_morph_progress"
    )

    Canvas(modifier = modifier) {
        drawPlayPauseMorph(
            progress = morphProgress,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

fun DrawScope.drawPlayPauseMorph(
    progress: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val w = size.width
    val h = size.height
    val strokeWidth = (w * 0.088f).coerceAtLeast(1.5f)

    // Left half / Pillar 1:
    // progress = 0 (Play): Left trapezoid base of triangle
    // progress = 1 (Pause): Left upright rounded bar
    val lTopLeft = Offset(lerp(w * 0.24f, w * 0.22f, progress), lerp(h * 0.20f, h * 0.18f, progress))
    val lTopRight = Offset(lerp(w * 0.46f, w * 0.42f, progress), lerp(h * 0.33f, h * 0.18f, progress))
    val lBottomRight = Offset(lerp(w * 0.46f, w * 0.42f, progress), lerp(h * 0.67f, h * 0.82f, progress))
    val lBottomLeft = Offset(lerp(w * 0.24f, w * 0.22f, progress), lerp(h * 0.80f, h * 0.82f, progress))

    val pathLeft = Path().apply {
        moveTo(lTopLeft.x, lTopLeft.y)
        lineTo(lTopRight.x, lTopRight.y)
        lineTo(lBottomRight.x, lBottomRight.y)
        lineTo(lBottomLeft.x, lBottomLeft.y)
        close()
    }
    drawPath(pathLeft, primaryColor)
    drawPath(pathLeft, primaryColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Right half / Pillar 2:
    // progress = 0 (Play): Right triangle apex (top & bottom right converge at (0.82w, 0.50h))
    // progress = 1 (Pause): Right upright rounded bar
    val rTopLeft = Offset(lerp(w * 0.46f, w * 0.58f, progress), lerp(h * 0.33f, h * 0.18f, progress))
    val rTopRight = Offset(lerp(w * 0.82f, w * 0.78f, progress), lerp(h * 0.50f, h * 0.18f, progress))
    val rBottomRight = Offset(lerp(w * 0.82f, w * 0.78f, progress), lerp(h * 0.50f, h * 0.82f, progress))
    val rBottomLeft = Offset(lerp(w * 0.46f, w * 0.58f, progress), lerp(h * 0.67f, h * 0.82f, progress))

    val pathRight = Path().apply {
        moveTo(rTopLeft.x, rTopLeft.y)
        lineTo(rTopRight.x, rTopRight.y)
        lineTo(rBottomRight.x, rBottomRight.y)
        lineTo(rBottomLeft.x, rBottomLeft.y)
        close()
    }
    drawPath(pathRight, secondaryColor)
    drawPath(pathRight, secondaryColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawBauhausUiSymbol(
    symbol: BauhausUiSymbol,
    primaryColor: Color,
    secondaryColor: Color
) {
    val w = size.width
    val h = size.height
    val strokeWidth = (w * 0.10f).coerceAtLeast(1.5f)

    when (symbol) {
        BauhausUiSymbol.Play -> {
            drawPlayPauseMorph(
                progress = 0f,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }

        BauhausUiSymbol.Pause -> {
            drawPlayPauseMorph(
                progress = 1f,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }

        BauhausUiSymbol.Clear -> {
            // Bauhaus geometric clear / trash / sweep geometry
            // Top lid handle
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.38f, h * 0.16f),
                end = Offset(w * 0.62f, h * 0.16f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Horizontal rim
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.16f, h * 0.28f),
                end = Offset(w * 0.84f, h * 0.28f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Body container
            val bodyPath = Path().apply {
                moveTo(w * 0.24f, h * 0.34f)
                lineTo(w * 0.30f, h * 0.82f)
                cubicTo(w * 0.30f, h * 0.88f, w * 0.36f, h * 0.88f, w * 0.42f, h * 0.88f)
                lineTo(w * 0.58f, h * 0.88f)
                cubicTo(w * 0.64f, h * 0.88f, w * 0.70f, h * 0.88f, w * 0.70f, h * 0.82f)
                lineTo(w * 0.76f, h * 0.34f)
            }
            drawPath(
                path = bodyPath,
                color = primaryColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Interior acoustic ribs
            drawLine(
                color = secondaryColor,
                start = Offset(w * 0.38f, h * 0.44f),
                end = Offset(w * 0.38f, h * 0.74f),
                strokeWidth = strokeWidth * 0.85f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondaryColor,
                start = Offset(w * 0.62f, h * 0.44f),
                end = Offset(w * 0.62f, h * 0.74f),
                strokeWidth = strokeWidth * 0.85f,
                cap = StrokeCap.Round
            )
        }

        BauhausUiSymbol.ThemeSystem -> {
            // Bauhaus bisection solid/hollow hemisphere (Yin-Yang day/night division)
            val center = Offset(w * 0.5f, h * 0.5f)
            val radius = w * 0.38f
            // Left filled hemisphere
            drawArc(
                color = primaryColor,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f)
            )
            // Outer circular boundary
            drawCircle(
                color = secondaryColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            // Center dividing diameter
            drawLine(
                color = secondaryColor,
                start = Offset(w * 0.5f, center.y - radius),
                end = Offset(w * 0.5f, center.y + radius),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        BauhausUiSymbol.ThemeLight -> {
            // Central sun ring + 4 orthogonal Bauhaus endpoints
            val center = Offset(w * 0.5f, h * 0.5f)
            val radius = w * 0.22f
            drawCircle(
                color = primaryColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            // 4 Orthogonal endpoints (Top, Bottom, Left, Right)
            drawLine(secondaryColor, Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.18f), strokeWidth, StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.50f, h * 0.82f), Offset(w * 0.50f, h * 0.92f), strokeWidth, StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.08f, h * 0.50f), Offset(w * 0.18f, h * 0.50f), strokeWidth, StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.82f, h * 0.50f), Offset(w * 0.92f, h * 0.50f), strokeWidth, StrokeCap.Round)
        }

        BauhausUiSymbol.ThemeDark -> {
            // Boolean intersection crescent moon + floating geometric star
            val moonPath = Path().apply {
                moveTo(w * 0.54f, h * 0.14f)
                cubicTo(w * 0.24f, h * 0.20f, w * 0.14f, h * 0.72f, w * 0.58f, h * 0.88f)
                cubicTo(w * 0.32f, h * 0.74f, w * 0.32f, h * 0.32f, w * 0.54f, h * 0.14f)
                close()
            }
            drawPath(moonPath, primaryColor)
            // Floating 4-point diamond star
            val starPath = Path().apply {
                moveTo(w * 0.74f, h * 0.24f)
                lineTo(w * 0.77f, h * 0.32f)
                lineTo(w * 0.85f, h * 0.34f)
                lineTo(w * 0.77f, h * 0.36f)
                lineTo(w * 0.74f, h * 0.44f)
                lineTo(w * 0.71f, h * 0.36f)
                lineTo(w * 0.63f, h * 0.34f)
                lineTo(w * 0.71f, h * 0.32f)
                close()
            }
            drawPath(starPath, secondaryColor)
        }

        BauhausUiSymbol.Close -> {
            // 45 degree geometric micro-rounded intersecting cross
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.24f, h * 0.24f),
                end = Offset(w * 0.76f, h * 0.76f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondaryColor,
                start = Offset(w * 0.76f, h * 0.24f),
                end = Offset(w * 0.24f, h * 0.76f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        BauhausUiSymbol.Check -> {
            // Geometric checkmark
            val checkPath = Path().apply {
                moveTo(w * 0.20f, h * 0.52f)
                lineTo(w * 0.42f, h * 0.74f)
                lineTo(w * 0.80f, h * 0.26f)
            }
            drawPath(
                path = checkPath,
                color = primaryColor,
                style = Stroke(width = strokeWidth * 1.15f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            if (primaryColor != secondaryColor) {
                // Terminus geometric dot
                drawCircle(
                    color = secondaryColor,
                    radius = strokeWidth * 0.75f,
                    center = Offset(w * 0.80f, h * 0.26f)
                )
            }
        }

        BauhausUiSymbol.Timer -> {
            // Minimalist dial silhouette + center pivot + clock hands
            val center = Offset(w * 0.50f, h * 0.50f)
            val radius = w * 0.38f
            drawCircle(
                color = primaryColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            // Center pivot
            drawCircle(primaryColor, radius = strokeWidth * 0.85f, center = center)
            // Minute hand (vertical to 12 o'clock)
            drawLine(secondaryColor, center, Offset(center.x, center.y - radius * 0.65f), strokeWidth, StrokeCap.Round)
            // Hour hand (pointing to 2 o'clock)
            drawLine(secondaryColor, center, Offset(center.x + radius * 0.48f, center.y - radius * 0.28f), strokeWidth, StrokeCap.Round)
        }

        BauhausUiSymbol.Add -> {
            // Equal-length orthogonal cross
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.20f, h * 0.50f),
                end = Offset(w * 0.80f, h * 0.50f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondaryColor,
                start = Offset(w * 0.50f, h * 0.20f),
                end = Offset(w * 0.50f, h * 0.80f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        BauhausUiSymbol.Restore -> {
            // Bauhaus counter-clockwise 270-degree sweep arc + terminal arrowhead pointing left
            val arcCenter = Offset(w * 0.50f, h * 0.50f)
            val arcRadius = w * 0.32f
            val topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius)
            val arcSize = Size(arcRadius * 2f, arcRadius * 2f)
            // Arc sweeps from 9 o'clock (180°) counter-clockwise by -270° to 12 o'clock (-90°)
            drawArc(
                color = primaryColor,
                startAngle = 180f,
                sweepAngle = -270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Arrowhead placed precisely at the arc terminal point (0.50w, 0.18h) pointing left
            val arrowTip = Offset(w * 0.50f, h * 0.18f)
            drawLine(secondaryColor, arrowTip, Offset(w * 0.61f, h * 0.08f), strokeWidth, StrokeCap.Round)
            drawLine(secondaryColor, arrowTip, Offset(w * 0.61f, h * 0.28f), strokeWidth, StrokeCap.Round)
        }

        BauhausUiSymbol.Import -> {
            // Bottom tray + downward arrow
            val trayPath = Path().apply {
                moveTo(w * 0.20f, h * 0.58f)
                lineTo(w * 0.20f, h * 0.84f)
                lineTo(w * 0.80f, h * 0.84f)
                lineTo(w * 0.80f, h * 0.58f)
            }
            drawPath(
                path = trayPath,
                color = primaryColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Downward arrow
            drawLine(secondaryColor, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.62f), strokeWidth, StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.34f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), strokeWidth, StrokeCap.Round)
            drawLine(secondaryColor, Offset(w * 0.66f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), strokeWidth, StrokeCap.Round)
        }
    }
}
