package com.whitenoise.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.SoundTrack
import kotlin.math.roundToInt

/**
 * Modern Bento-style sound tile card for 2-column grid.
 * Clean, tactile, and responsive. Clicking toggles playback state with a SaltUI active glow/tint.
 */
@Composable
fun SoundTileCard(
    track: SoundTrack,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    val targetBgColor = if (track.isPlaying) {
        SaltTheme.colors.highlight.copy(alpha = 0.12f)
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
            .clip(shape)
            .border(
                width = if (track.isPlaying) 1.5.dp else 1.dp,
                color = animatedBorderColor,
                shape = shape
            )
            .background(animatedBgColor)
            .clickable { onTogglePlay() }
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

/**
 * Backward compatibility alias for SoundCard.
 */
@Deprecated("Use SoundTileCard instead", ReplaceWith("SoundTileCard(track, onTogglePlay, modifier)"))
@Composable
fun SoundCard(
    track: SoundTrack,
    onTogglePlay: () -> Unit,
    onVolumeChange: (Float) -> Unit = {},
    onToggleMute: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    SoundTileCard(
        track = track,
        onTogglePlay = onTogglePlay,
        modifier = modifier
    )
}
