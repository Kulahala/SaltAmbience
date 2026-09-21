package com.whitenoise.app.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.PlaybackState
import com.whitenoise.app.core.model.SoundTrack
import kotlin.math.roundToInt

/**
 * Compact floating bottom player bar inspired by Xiaomi HyperOS & Apple Control Center mini pill player.
 * Shows active sound badges/emojis, master play/pause toggle, and a trigger button for the expandable mixer sheet.
 */
@Composable
fun BottomPlayerBar(
    playbackState: PlaybackState,
    activeTracks: List<SoundTrack>,
    onToggleMasterPlay: () -> Unit,
    onOpenMixer: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSleepTimer: () -> Unit = onOpenMixer,
    onStopAll: () -> Unit = {},
    onMasterVolumeChange: (Float) -> Unit = {}
) {
    val shape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(elevation = 12.dp, shape = shape)
            .clip(shape)
            .border(
                width = 1.dp,
                color = SaltTheme.colors.subBackground.copy(alpha = 0.8f),
                shape = shape
            )
            .background(SaltTheme.colors.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Master Play / Pause Circular Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (playbackState.isMasterPlaying) SaltTheme.colors.highlight
                        else SaltTheme.colors.subBackground
                    )
                    .clickable { onToggleMasterPlay() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (playbackState.isMasterPlaying) "⏸" else "▶",
                    color = if (playbackState.isMasterPlaying) Color.White else SaltTheme.colors.highlight,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center Info area: tapping opens the expandable mixer sheet
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenMixer() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                // Top line: active badges / emojis + count
                val titleText = if (activeTracks.isNotEmpty()) {
                    val emojis = activeTracks.take(4).joinToString(" ") { it.iconEmoji }
                    "$emojis · ${activeTracks.size}轨混音"
                } else if (playbackState.activeTrackCount > 0) {
                    "${playbackState.activeTrackCount} 轨混音中"
                } else {
                    "轻触音效开始混音"
                }

                Text(
                    text = titleText,
                    style = SaltTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = SaltTheme.colors.text
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle line: sleep countdown or master volume hint
                val subText = if (playbackState.isSleepTimerRunning) {
                    val remaining = playbackState.sleepTimerRemainingSeconds ?: 0L
                    val mins = remaining / 60
                    val secs = remaining % 60
                    "⏱️ 倒计时 %02d:%02d · 混音台 ↗".format(mins, secs)
                } else {
                    "总音量: ${(playbackState.masterVolume * 100).roundToInt()}% · 调音台 ↗"
                }

                Text(
                    text = subText,
                    style = SaltTheme.textStyles.sub,
                    fontSize = 11.sp,
                    color = if (playbackState.isSleepTimerRunning) SaltTheme.colors.highlight else SaltTheme.colors.subText
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Expandable Mixer Sheet Trigger Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
                    .clickable { onOpenMixer() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎛️ 混音台",
                    style = SaltTheme.textStyles.sub,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = SaltTheme.colors.highlight
                )
            }
        }
    }
}

/**
 * Backward compatibility overload for BottomPlayerBar without activeTracks.
 */
@Composable
fun BottomPlayerBar(
    playbackState: PlaybackState,
    onToggleMasterPlay: () -> Unit,
    onStopAll: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onOpenSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomPlayerBar(
        playbackState = playbackState,
        activeTracks = emptyList(),
        onToggleMasterPlay = onToggleMasterPlay,
        onOpenMixer = onOpenSleepTimer,
        modifier = modifier,
        onOpenSleepTimer = onOpenSleepTimer,
        onStopAll = onStopAll,
        onMasterVolumeChange = onMasterVolumeChange
    )
}
