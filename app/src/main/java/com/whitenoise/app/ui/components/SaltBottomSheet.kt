package com.whitenoise.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
 * - 24.dp top rounded corners on SaltTheme.colors.background
 * - 0.45f black scrim with tap-to-dismiss and fade animation
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

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedOffsetY.snapTo(0f)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        // Bottom Sheet Surface
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, animatedOffsetY.value.roundToInt().coerceAtLeast(0)) }
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(SaltTheme.colors.background)
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
