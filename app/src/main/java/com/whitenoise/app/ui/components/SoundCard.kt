package com.whitenoise.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.SoundTrack
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

/**
 * Modern Bento-style sound tile card for 2-column grid.
 * Supports tap to toggle playback and long-press horizontal drag to adjust volume with tactile feedback.
 */
@Composable
fun SoundTileCard(
    track: SoundTrack,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
    onVolumeChange: (Float) -> Unit = {}
) {
    val shape = RoundedCornerShape(20.dp)
    val haptic = LocalHapticFeedback.current

    var isAdjustingVolume by remember { mutableStateOf(false) }
    var initialVolumeOnDrag by remember { mutableFloatStateOf(0f) }
    var cardWidthPx by remember { mutableFloatStateOf(0f) }
    var lastHapticVolume by remember { mutableFloatStateOf(track.volume) }

    // Dynamic scale during long-press adjustment
    val cardScale by animateFloatAsState(
        targetValue = if (isAdjustingVolume) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
        label = "card_scale"
    )

    // Capsule scale animation when adjusting volume
    val capsuleScale by animateFloatAsState(
        targetValue = if (isAdjustingVolume) 1.18f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
        label = "capsule_scale"
    )

    val targetBgColor = if (track.isPlaying) {
        SaltTheme.colors.highlight.copy(alpha = if (isAdjustingVolume) 0.18f else 0.12f)
    } else {
        SaltTheme.colors.subBackground
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(150),
        label = "tile_bg"
    )

    val targetBorderColor = if (track.isPlaying) {
        SaltTheme.colors.highlight
    } else {
        SaltTheme.colors.subBackground
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(150),
        label = "tile_border"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width.toFloat() }
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(shape)
            .border(
                width = if (track.isPlaying) 1.5.dp else 1.dp,
                color = animatedBorderColor,
                shape = shape
            )
            .background(animatedBgColor)
            .pointerInput(track.id, track.isPlaying) {
                if (!track.isPlaying) {
                    detectTapGestures(
                        onTap = { onTogglePlay() }
                    )
                } else {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var isLongPressTriggered = false

                        var shouldTogglePlay = false
                        var shouldCancelGesture = false

                        try {
                            withTimeout(220L) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (change.changedToUp()) {
                                        change.consume()
                                        shouldTogglePlay = true
                                        break
                                    }
                                    val dragDistance = (change.position - down.position).getDistance()
                                    if (dragDistance > viewConfiguration.touchSlop) {
                                        shouldCancelGesture = true
                                        break
                                    }
                                }
                            }
                        } catch (_: PointerEventTimeoutCancellationException) {
                            isLongPressTriggered = true
                        }

                        if (shouldTogglePlay) {
                            onTogglePlay()
                            return@awaitEachGesture
                        }
                        if (shouldCancelGesture) {
                            return@awaitEachGesture
                        }

                        if (isLongPressTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isAdjustingVolume = true
                            initialVolumeOnDrag = track.volume
                            lastHapticVolume = track.volume

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (change.changedToUp()) {
                                        change.consume()
                                        break
                                    }
                                    val dragDeltaX = change.position.x - down.position.x
                                    change.consume()

                                    val newVol = SoundVolumeGestureMath.calculateNewVolume(
                                        initialVolume = initialVolumeOnDrag,
                                        dragDeltaXPx = dragDeltaX,
                                        cardWidthPx = cardWidthPx
                                    )
                                    if (SoundVolumeGestureMath.isHittingBoundary(lastHapticVolume, newVol)) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    lastHapticVolume = newVol
                                    onVolumeChange(newVol)
                                }
                            } finally {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isAdjustingVolume = false
                            }
                        }
                    }
                }
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Emoji Icon on Left, Status Badge on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BauhausSoundIcon(
                    trackId = track.id,
                    isPlaying = track.isPlaying,
                    modifier = Modifier.size(32.dp)
                )

                if (track.isPlaying) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = capsuleScale
                                scaleY = capsuleScale
                            }
                            .clip(CircleShape)
                            .background(SaltTheme.colors.highlight)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (track.isMuted) "静音" else "${(track.volume * 100).roundToInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SaltTheme.colors.text.copy(alpha = 0.18f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sound Name Title
            Text(
                text = track.name,
                style = SaltTheme.textStyles.main,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (track.isPlaying) SaltTheme.colors.highlight else SaltTheme.colors.text
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Sound Subtitle (High contrast, eliminating washed-out grey)
            Text(
                text = track.subtitle,
                style = SaltTheme.textStyles.sub,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (track.isPlaying) SaltTheme.colors.highlight.copy(alpha = 0.85f) else SaltTheme.colors.text.copy(alpha = 0.65f)
            )
        }
    }
}
