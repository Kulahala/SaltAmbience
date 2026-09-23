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

/**
 * Unified Bauhaus acoustic minimalist vector symbol system.
 * Replaces colorful Unicode emojis with clean geometric acoustic elements (points, lines, planes, waveforms).
 *
 * Color Specification:
 * - Inactive (未激活): Matte mist grey (SaltTheme.colors.text with alpha 0.42f) across all strokes.
 * - Active (激活): High-contrast two-tone Bauhaus acoustic palette:
 *   - Primary structure / upper line (主结构线/上线): Pure White (Color.White in dark mode, deep charcoal in light mode).
 *   - Water wave / ripple / node / secondary line (水波/微流/节点/下线): Sea Salt Ice Cyan (SaltTheme.colors.highlight).
 */
@Composable
fun BauhausSoundIcon(
    trackId: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier.size(28.dp),
    tint: Color? = null
) {
    val isDark = SaltTheme.configs.isDarkTheme
    val idleColor = SaltTheme.colors.text.copy(alpha = 0.42f)
    val primaryActiveColor = if (isDark) Color.White else Color(0xFF1E2026)
    val secondaryActiveColor = SaltTheme.colors.highlight

    val targetPrimary = tint ?: if (isPlaying) primaryActiveColor else idleColor
    val targetSecondary = tint ?: if (isPlaying) secondaryActiveColor else idleColor

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
            // Water ripple (White)
            drawLine(primaryColor, Offset(w * 0.24f, h * 0.84f), Offset(w * 0.76f, h * 0.84f), strokeWidth = strokeWidth * 0.75f, cap = StrokeCap.Round)
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
