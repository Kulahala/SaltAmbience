package com.whitenoise.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import kotlin.math.roundToInt

/**
 * Modern vertical capsule slider inspired by Xiaomi HyperOS and Apple Control Center.
 * Supports fluid vertical drag gestures, tap repositioning, animated fill, and embedded icon & percentage.
 */
@Composable
fun VerticalCapsuleSlider(
    value: Float, // 0.0f to 1.0f
    onValueChange: (Float) -> Unit,
    title: String,
    icon: String,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    enabled: Boolean = true,
    width: Dp = 68.dp,
    height: Dp = 180.dp,
    activeColor: Color = SaltTheme.colors.highlight,
    inactiveColor: Color = SaltTheme.colors.subBackground,
    onIconClick: (() -> Unit)? = null
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val cornerRadius = 24.dp
    val shape = RoundedCornerShape(cornerRadius)

    val effectiveValue = if (isMuted) 0f else value.coerceIn(0f, 1f)

    // Smooth animation for external value updates
    val animatedFill by animateFloatAsState(
        targetValue = effectiveValue,
        animationSpec = tween(durationMillis = 80),
        label = "capsule_fill_animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Capsule Container
        BoxWithConstraints(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(shape)
                .background(inactiveColor)
                .border(
                    width = 1.dp,
                    color = SaltTheme.colors.text.copy(alpha = 0.05f),
                    shape = shape
                )
                .pointerInput(enabled, isMuted) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val totalH = size.height.toFloat()
                        if (totalH > 0f) {
                            val initialVal = ((totalH - down.position.y) / totalH).coerceIn(0f, 1f)
                            currentOnValueChange(initialVal)
                        }
                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.find { it.id == pointerId } ?: event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            change.consume()
                            if (totalH > 0f) {
                                val currentVal = ((totalH - change.position.y) / totalH).coerceIn(0f, 1f)
                                currentOnValueChange(currentVal)
                            }
                        }
                    }
                }
        ) {
            // Dynamic Active Fill Box anchored at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedFill)
                    .align(Alignment.BottomCenter)
                    .background(activeColor)
            )

            // Top: Percentage Indicator
            val percentageText = when {
                isMuted -> "静音"
                animatedFill <= 0.001f -> "0%"
                else -> "${(animatedFill * 100).roundToInt()}%"
            }

            Text(
                text = percentageText,
                style = SaltTheme.textStyles.sub,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (animatedFill > 0.82f) Color.White else SaltTheme.colors.text,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            )

            // Bottom: Sound Emoji / Master Headphone Icon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .then(
                        if (onIconClick != null) Modifier.clickable { onIconClick() } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Title Label
        Text(
            text = title,
            style = SaltTheme.textStyles.sub,
            color = SaltTheme.colors.text,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(width)
        )
    }
}
