package com.whitenoise.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme

/**
 * Lightweight horizontal track slider styled for SaltUI aesthetic.
 * Replaces heavy Material3 Slider dependency with pure Compose Canvas & touch gesture detection.
 */
@Composable
fun SaltHorizontalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    activeColor: Color = SaltTheme.colors.highlight,
    inactiveColor: Color = SaltTheme.colors.subBackground
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val w = size.width.toFloat()
                    if (w > 0f) {
                        val fraction = (offset.x / w).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        currentOnValueChange(newValue)
                    }
                }
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val w = size.width.toFloat()
                    if (w > 0f) {
                        val fraction = (change.position.x / w).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        currentOnValueChange(newValue)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
        val fraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
        val trackHeight = 6.dp
        val thumbRadius = 9.dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
        ) {
            val h = size.height
            val w = size.width
            val corner = CornerRadius(h / 2f, h / 2f)

            // Inactive track
            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = corner
            )

            // Active track
            val activeWidth = (w * fraction).coerceAtLeast(0f)
            if (activeWidth > 0f) {
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(activeWidth, h),
                    cornerRadius = corner
                )
            }
        }

        // Thumb indicator
        val density = LocalDensity.current
        val totalWidthPx = with(density) { maxWidth.toPx() }
        val thumbDiameterPx = with(density) { thumbRadius.toPx() * 2f }
        val thumbOffsetXPx = (totalWidthPx * fraction - with(density) { thumbRadius.toPx() })
            .coerceIn(0f, (totalWidthPx - thumbDiameterPx).coerceAtLeast(0f))
        val thumbOffsetXDp = with(density) { thumbOffsetXPx.toDp() }

        Box(
            modifier = Modifier
                .offset(x = thumbOffsetXDp)
                .size(thumbRadius * 2)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(activeColor)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}
