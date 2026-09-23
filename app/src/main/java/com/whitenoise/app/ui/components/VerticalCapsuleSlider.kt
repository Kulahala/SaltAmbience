package com.whitenoise.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
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
 * Properly discriminates vertical dragging from parent horizontal scrolling.
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

    var isDragging by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    var lastHapticZone by remember { mutableIntStateOf(-1) }

    val effectiveValue = if (isMuted) 0f else value.coerceIn(0f, 1f)

    fun updateValueWithNotches(rawRatio: Float) {
        val clamped = rawRatio.coerceIn(0f, 1f)
        val snappedVal = when {
            clamped <= 0.015f -> 0f
            clamped >= 0.985f -> 1f
            kotlin.math.abs(clamped - 0.5f) <= 0.015f -> 0.5f
            else -> clamped
        }

        val currentZone = when {
            snappedVal == 0f -> 0
            snappedVal == 0.5f -> 50
            snappedVal == 1f -> 100
            else -> -1
        }

        if (currentZone != -1 && currentZone != lastHapticZone) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        lastHapticZone = currentZone

        currentOnValueChange(snappedVal)
    }

    // Direct 1:1 tracking during drag with zero latency; smooth tween for taps and external state updates
    val animatedFill by animateFloatAsState(
        targetValue = effectiveValue,
        animationSpec = if (isDragging) snap() else tween(durationMillis = 120),
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
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures { offset ->
                        val totalH = size.height.toFloat()
                        if (totalH > 0f) {
                            updateValueWithNotches((totalH - offset.y) / totalH)
                        }
                    }
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            lastHapticZone = -1
                            val totalH = size.height.toFloat()
                            if (totalH > 0f) {
                                updateValueWithNotches((totalH - offset.y) / totalH)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            lastHapticZone = -1
                        },
                        onDragCancel = {
                            isDragging = false
                            lastHapticZone = -1
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            val totalH = size.height.toFloat()
                            if (totalH > 0f) {
                                updateValueWithNotches((totalH - change.position.y) / totalH)
                            }
                        }
                    )
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

            // Top: Percentage Indicator with pill background badge to prevent reading blind spots
            val percentageText = when {
                isMuted -> "静音"
                effectiveValue <= 0.001f -> "0%"
                else -> "${(effectiveValue * 100).roundToInt()}%"
            }

            val isFilledOverText = animatedFill >= 0.85f

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilledOverText) {
                            Color.Black.copy(alpha = 0.28f)
                        } else {
                            SaltTheme.colors.background
                        }
                    )
                    .border(
                        width = 0.5.dp,
                        color = if (isFilledOverText) Color.White.copy(alpha = 0.15f) else SaltTheme.colors.text.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = percentageText,
                    style = SaltTheme.textStyles.sub,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isFilledOverText) Color.White else SaltTheme.colors.text
                )
            }

            // Bottom: Sound Emoji / Master Headphone Icon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .then(
                        if (onIconClick != null) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onIconClick() }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.then(
                        if (isMuted) Modifier.alpha(0.4f) else Modifier
                    )
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
