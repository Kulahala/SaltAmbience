package com.whitenoise.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme

/**
 * Lightweight pure Compose Switch adhering to SaltUI minimalist aesthetics.
 * Features:
 * - 46x26dp tactile pill track with subtle border
 * - Physics-based damped spring animation (dampingRatio = 0.78f, stiffness = 420f)
 * - Semantic highlight color activation with smooth color crossfade
 * - Built-in tactile haptic feedback on state flip
 */
@Composable
fun SaltSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val isDark = SaltTheme.configs.isDarkTheme

    val trackWidth = 46.dp
    val trackHeight = 26.dp
    val thumbSize = 20.dp
    val thumbPadding = 3.dp
    val targetOffset = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding

    val animatedOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
        label = "switch_thumb_offset"
    )

    val targetTrackColor = when {
        !enabled -> SaltTheme.colors.subBackground.copy(alpha = 0.5f)
        checked -> SaltTheme.colors.highlight
        isDark -> Color(0xFF2B2E36)
        else -> Color(0xFFE2E8F0)
    }

    val animatedTrackColor by animateColorAsState(
        targetValue = targetTrackColor,
        animationSpec = tween(180),
        label = "switch_track_color"
    )

    val trackBorderColor = if (!checked && enabled) {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .border(width = 1.dp, color = trackBorderColor, shape = CircleShape)
            .background(animatedTrackColor)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Floating thumb with soft elevation
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .size(thumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
