package com.whitenoise.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Standard SaltUI bottom sheet container adhering to SaltAmbience specifications.
 * Features:
 * - 24.dp top rounded corners with depth surface (#1B1D24 in dark mode) and subtle hairline border
 * - Adaptive scrim with nonlinear FastOutSlowIn fade
 * - Physics-based spring entry (dampingRatio = 0.82f) & snappy FastOutLinear exit
 * - Top drag pill handle with SaltTheme.colors.text.copy(alpha = 0.2f)
 * - Coexisting tap-to-dismiss & natural downward swipe/drag-to-dismiss gesture with spring recovery
 * - Window insets handling (.navigationBarsPadding() & .imePadding())
 */
@Composable
fun SaltBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (isVisible) {
        BackHandler(onBack = onDismiss)
    }

    val coroutineScope = rememberCoroutineScope()
    val animatedOffsetY = remember { Animatable(0f) }
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 70.dp.toPx() }
    val isDark = SaltTheme.configs.isDarkTheme
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val sheetBackgroundColor = if (isDark) Color(0xFF1B1D24) else SaltTheme.colors.background
    val sheetBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedOffsetY.snapTo(0f)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Scrim with nonlinear fade
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (isDark) 0.55f else 0.40f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        // Bottom Sheet Surface with Physics Spring Entry
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f
                ),
                initialOffsetY = { it }
            ) + fadeIn(
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing),
                targetOffsetY = { it }
            ) + fadeOut(
                animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, animatedOffsetY.value.roundToInt().coerceAtLeast(0)) }
                    .shadow(elevation = 24.dp, shape = shape)
                    .clip(shape)
                    .border(width = 1.dp, color = sheetBorderColor, shape = shape)
                    .background(sheetBackgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume click to prevent dismissing through sheet body */ }
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pill handle drag area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = { /* started */ },
                                    onDragEnd = {
                                        if (animatedOffsetY.value > dismissThreshold) {
                                            onDismiss()
                                        } else {
                                            coroutineScope.launch {
                                                animatedOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            animatedOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                        }
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        coroutineScope.launch {
                                            animatedOffsetY.snapTo((animatedOffsetY.value + dragAmount).coerceAtLeast(0f))
                                        }
                                    }
                                )
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDismiss() }
                            .padding(top = 10.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(SaltTheme.colors.text.copy(alpha = 0.2f))
                        )
                    }

                    // Sheet Body
                    content()
                }
            }
        }
    }
}
