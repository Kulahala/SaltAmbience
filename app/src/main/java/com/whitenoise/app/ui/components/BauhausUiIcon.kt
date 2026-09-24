package com.whitenoise.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
 * Unified Bauhaus UI Vector Icon Component.
 * Pure Canvas rendering using geometric primitives with crisp aesthetic proportions.
 */
@Composable
fun BauhausUiIcon(
    symbol: BauhausUiSymbol,
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = SaltTheme.colors.text
) {
    Canvas(modifier = modifier) {
        drawBauhausUiSymbol(symbol = symbol, color = tint)
    }
}

private fun DrawScope.drawBauhausUiSymbol(symbol: BauhausUiSymbol, color: Color) {
    val w = size.width
    val h = size.height
    val strokeWidth = (w * 0.10f).coerceAtLeast(1.5f)

    when (symbol) {
        BauhausUiSymbol.Play -> {
            // Bauhaus solid geometric rounded triangle (optically offset slightly to the right)
            val path = Path().apply {
                moveTo(w * 0.28f, h * 0.20f)
                lineTo(w * 0.80f, h * 0.50f)
                lineTo(w * 0.28f, h * 0.80f)
                close()
            }
            drawPath(path, color)
            // Stroke overlay to give slightly rounded corners
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        BauhausUiSymbol.Pause -> {
            // Parallel symmetric vertical thick rounded bars
            val barW = w * 0.22f
            val barH = h * 0.60f
            val corner = CornerRadius(barW / 2f, barW / 2f)
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.22f, h * 0.20f),
                size = Size(barW, barH),
                cornerRadius = corner
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.56f, h * 0.20f),
                size = Size(barW, barH),
                cornerRadius = corner
            )
        }

        BauhausUiSymbol.Clear -> {
            // Bauhaus geometric clear / trash / sweep geometry
            // Top lid handle
            drawLine(
                color = color,
                start = Offset(w * 0.38f, h * 0.16f),
                end = Offset(w * 0.62f, h * 0.16f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Horizontal rim
            drawLine(
                color = color,
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
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Interior acoustic ribs
            drawLine(
                color = color,
                start = Offset(w * 0.38f, h * 0.44f),
                end = Offset(w * 0.38f, h * 0.74f),
                strokeWidth = strokeWidth * 0.85f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
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
                color = color,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f)
            )
            // Outer circular boundary
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            // Center dividing diameter
            drawLine(
                color = color,
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
                color = color,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            // 4 Orthogonal endpoints (Top, Bottom, Left, Right)
            drawLine(color, Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.18f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(w * 0.50f, h * 0.82f), Offset(w * 0.50f, h * 0.92f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(w * 0.08f, h * 0.50f), Offset(w * 0.18f, h * 0.50f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(w * 0.82f, h * 0.50f), Offset(w * 0.92f, h * 0.50f), strokeWidth, StrokeCap.Round)
        }

        BauhausUiSymbol.ThemeDark -> {
            // Boolean intersection crescent moon + floating geometric star
            val moonPath = Path().apply {
                moveTo(w * 0.54f, h * 0.14f)
                cubicTo(w * 0.24f, h * 0.20f, w * 0.14f, h * 0.72f, w * 0.58f, h * 0.88f)
                cubicTo(w * 0.32f, h * 0.74f, w * 0.32f, h * 0.32f, w * 0.54f, h * 0.14f)
                close()
            }
            drawPath(moonPath, color)
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
            drawPath(starPath, color)
        }

        BauhausUiSymbol.Close -> {
            // 45 degree geometric micro-rounded intersecting cross
            drawLine(
                color = color,
                start = Offset(w * 0.24f, h * 0.24f),
                end = Offset(w * 0.76f, h * 0.76f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
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
                color = color,
                style = Stroke(width = strokeWidth * 1.15f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        BauhausUiSymbol.Timer -> {
            // Minimalist dial silhouette + center pivot + clock hands
            val center = Offset(w * 0.50f, h * 0.50f)
            val radius = w * 0.38f
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            // Center pivot
            drawCircle(color, radius = strokeWidth * 0.85f, center = center)
            // Minute hand (vertical to 12 o'clock)
            drawLine(color, center, Offset(center.x, center.y - radius * 0.65f), strokeWidth, StrokeCap.Round)
            // Hour hand (pointing to 2 o'clock)
            drawLine(color, center, Offset(center.x + radius * 0.48f, center.y - radius * 0.28f), strokeWidth, StrokeCap.Round)
        }

        BauhausUiSymbol.Add -> {
            // Equal-length orthogonal cross
            drawLine(
                color = color,
                start = Offset(w * 0.20f, h * 0.50f),
                end = Offset(w * 0.80f, h * 0.50f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
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
                color = color,
                startAngle = 180f,
                sweepAngle = -270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Arrowhead placed precisely at the arc terminal point (0.50w, 0.18h) pointing left
            val arrowTip = Offset(w * 0.50f, h * 0.18f)
            drawLine(color, arrowTip, Offset(w * 0.61f, h * 0.08f), strokeWidth, StrokeCap.Round)
            drawLine(color, arrowTip, Offset(w * 0.61f, h * 0.28f), strokeWidth, StrokeCap.Round)
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
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Downward arrow
            drawLine(color, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.62f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(w * 0.34f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(w * 0.66f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), strokeWidth, StrokeCap.Round)
        }
    }
}
