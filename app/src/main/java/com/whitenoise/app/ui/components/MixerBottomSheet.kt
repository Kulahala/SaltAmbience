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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.SoundTrack
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * HyperOS & Apple Control Center-inspired expandable multi-track mixer bottom sheet.
 * Displays side-by-side [VerticalCapsuleSlider] components for Master volume and currently active tracks.
 */
@Composable
fun MixerBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    masterVolume: Float,
    onMasterVolumeChange: (Float) -> Unit,
    activeTracks: List<SoundTrack>,
    onTrackVolumeChange: (String, Float) -> Unit,
    onToggleTrackMute: (String) -> Unit,
    isMasterPlaying: Boolean,
    onToggleMasterPlay: () -> Unit,
    onStopAll: () -> Unit,
    modifier: Modifier = Modifier,
    isSleepTimerRunning: Boolean = false,
    sleepTimerRemainingSeconds: Long? = null,
    onSelectSleepMinutes: (Int) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onOpenFullSleepTimer: () -> Unit = {}
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
        // Semi-transparent scrim with nonlinear fade
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

        // Sliding Bottom Sheet with Physics Spring Entry
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
                    ) { /* Consume click to prevent dismissing */ }
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Drag Pill Indicator (tap to dismiss & drag to dismiss)
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
                            .padding(top = 10.dp, bottom = 6.dp),
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

                    // Header: Title & Close Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "声音控制中心",
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SaltTheme.colors.text
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (activeTracks.isNotEmpty()) "${activeTracks.size} 轨正在混音" else "单轨主音量",
                                style = SaltTheme.textStyles.sub,
                                fontSize = 12.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.65f)
                            )
                        }

                        // Close "完成" Button
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SaltTheme.colors.subBackground)
                                .clickable { onDismiss() }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "完成",
                                style = SaltTheme.textStyles.sub,
                                fontWeight = FontWeight.Medium,
                                color = SaltTheme.colors.text,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Scrollable Horizontal Mixer Capsule Sliders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Column 1: Master Volume Capsule
                        VerticalCapsuleSlider(
                            value = masterVolume,
                            onValueChange = onMasterVolumeChange,
                            title = "主音量",
                            icon = "🎧",
                            activeColor = SaltTheme.colors.highlight
                        )

                        // Subsequent Columns: ONLY currently active playing sound tracks
                        activeTracks.forEach { track ->
                            VerticalCapsuleSlider(
                                value = if (track.isMuted) 0f else track.volume,
                                onValueChange = { onTrackVolumeChange(track.id, it) },
                                title = track.name,
                                icon = track.iconEmoji,
                                isMuted = track.isMuted,
                                activeColor = SaltTheme.colors.highlight,
                                onIconClick = { onToggleTrackMute(track.id) }
                            )
                        }

                        // Friendly Hint Card when no active tracks
                        if (activeTracks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(SaltTheme.colors.subBackground.copy(alpha = 0.5f))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "轻触主页音效卡片\n开启多轨混音",
                                    style = SaltTheme.textStyles.sub,
                                    fontSize = 12.sp,
                                    color = SaltTheme.colors.text.copy(alpha = 0.65f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Section: Action Controls (Stop All & Master Play/Pause)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        // Master Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stop All Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SaltTheme.colors.subBackground)
                                    .clickable { onStopAll() }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⏹ 全部停止",
                                    style = SaltTheme.textStyles.main,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = SaltTheme.colors.text.copy(alpha = 0.8f)
                                )
                            }

                            // Master Play/Pause Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isMasterPlaying) SaltTheme.colors.highlight
                                        else SaltTheme.colors.highlight.copy(alpha = 0.15f)
                                    )
                                    .clickable { onToggleMasterPlay() }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isMasterPlaying) "⏸ 暂停混音" else "▶ 继续混音",
                                    style = SaltTheme.textStyles.main,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isMasterPlaying) Color.White else SaltTheme.colors.highlight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
