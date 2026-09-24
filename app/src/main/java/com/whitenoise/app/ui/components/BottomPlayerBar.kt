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
import com.whitenoise.app.core.audio.VolumeCalculator
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
    val isDark = SaltTheme.configs.isDarkTheme
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else SaltTheme.colors.text.copy(alpha = 0.06f)
    val baseSurfaceColor = if (isDark) Color(0xFF1E2026) else Color(0xFFF2F4F7)
    val surfaceColor = baseSurfaceColor
    val buttonSurfaceColor = if (isDark) Color(0xFF282B33) else Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(elevation = 14.dp, shape = shape)
            .clip(shape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .background(surfaceColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Master Play / Pause Circular Button
            val isMasterPlaying = playbackState.isMasterPlaying
            val playBgColor = if (isMasterPlaying) SaltTheme.colors.highlight else buttonSurfaceColor
            val playIconColor = if (isMasterPlaying) Color.White else SaltTheme.colors.highlight
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(playBgColor)
                    .clickable { onToggleMasterPlay() },
                contentAlignment = Alignment.Center
            ) {
                BauhausPlayPauseMorphIcon(
                    isPlaying = isMasterPlaying,
                    tint = playIconColor,
                    modifier = Modifier.size(16.dp)
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
                // Top line: active Bauhaus acoustic badges + count
                if (activeTracks.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            activeTracks.take(4).forEach { track ->
                                BauhausSoundIcon(
                                    trackId = track.id,
                                    isPlaying = playbackState.isMasterPlaying,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (playbackState.isMasterPlaying) "${activeTracks.size}轨混音中" else "${activeTracks.size}轨已暂停",
                            style = SaltTheme.textStyles.main,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = SaltTheme.colors.text
                        )
                    }
                } else {
                    val titleText = if (playbackState.activeTrackCount > 0) {
                        if (playbackState.isMasterPlaying) {
                            "${playbackState.activeTrackCount} 轨混音中"
                        } else {
                            "${playbackState.activeTrackCount} 轨已暂停"
                        }
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
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle line: master volume hint with tap-to-open mixer affordance
                val subText = "总音量: ${(playbackState.masterVolume * 100).roundToInt()}% · 混音台 ↗"

                Text(
                    text = subText,
                    style = SaltTheme.textStyles.sub,
                    fontSize = 11.sp,
                    color = SaltTheme.colors.text.copy(alpha = 0.65f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sleep Timer / Dynamic Countdown Pill Button
            val isTimerRunning = playbackState.isSleepTimerRunning
            val timerBg = if (isTimerRunning) SaltTheme.colors.highlight.copy(alpha = 0.16f) else buttonSurfaceColor
            val timerBorder = if (isTimerRunning) SaltTheme.colors.highlight.copy(alpha = 0.35f) else Color.Transparent
            val timerContentColor = if (isTimerRunning) SaltTheme.colors.highlight else SaltTheme.colors.text

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(timerBg)
                    .border(width = 1.dp, color = timerBorder, shape = CircleShape)
                    .clickable { onOpenSleepTimer() }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BauhausUiIcon(
                        symbol = BauhausUiSymbol.Timer,
                        tint = if (isTimerRunning) timerContentColor else null,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isTimerRunning) VolumeCalculator.formatCountdown(playbackState.sleepTimerRemainingSeconds) else "定时",
                        style = SaltTheme.textStyles.sub,
                        fontWeight = if (isTimerRunning) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = timerContentColor
                    )
                }
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
