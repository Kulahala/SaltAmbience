package com.whitenoise.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.PlaybackState
import kotlin.math.roundToInt

@Composable
fun BottomPlayerBar(
    playbackState: PlaybackState,
    onToggleMasterPlay: () -> Unit,
    onStopAll: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onOpenSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(SaltTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            // Master Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Info & Sleep Timer badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Play/Pause Master Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SaltTheme.colors.highlight)
                            .clickable { onToggleMasterPlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (playbackState.isMasterPlaying) "暂停" else "播放",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (playbackState.activeTrackCount > 0) {
                                "${playbackState.activeTrackCount} 轨声音播放中"
                            } else {
                                "无播放音轨"
                            },
                            style = SaltTheme.textStyles.main,
                            color = SaltTheme.colors.text
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (playbackState.isSleepTimerRunning) {
                                val remaining = playbackState.sleepTimerRemainingSeconds ?: 0L
                                val mins = remaining / 60
                                val secs = remaining % 60
                                "倒计时 %02d:%02d".format(mins, secs)
                            } else {
                                "总音量: ${(playbackState.masterVolume * 100).roundToInt()}%"
                            },
                            style = SaltTheme.textStyles.sub,
                            color = if (playbackState.isSleepTimerRunning) SaltTheme.colors.highlight else SaltTheme.colors.subText
                        )
                    }
                }

                // Right action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sleep Timer button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (playbackState.isSleepTimerRunning) SaltTheme.colors.highlight.copy(alpha = 0.15f)
                                else SaltTheme.colors.subBackground
                            )
                            .clickable { onOpenSleepTimer() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (playbackState.isSleepTimerRunning) "定时中" else "定时",
                            fontSize = 12.sp,
                            color = if (playbackState.isSleepTimerRunning) SaltTheme.colors.highlight else SaltTheme.colors.subText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // All Stop button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SaltTheme.colors.subBackground)
                            .clickable { onStopAll() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "全停",
                            fontSize = 12.sp,
                            color = SaltTheme.colors.subText
                        )
                    }
                }
            }

            // Master Volume Slider Row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "主音量",
                    style = SaltTheme.textStyles.sub,
                    color = SaltTheme.colors.subText,
                    modifier = Modifier.width(42.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = playbackState.masterVolume,
                    onValueChange = onMasterVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SaltTheme.colors.highlight,
                        activeTrackColor = SaltTheme.colors.highlight,
                        inactiveTrackColor = SaltTheme.colors.subBackground
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(playbackState.masterVolume * 100).roundToInt()}%",
                    style = SaltTheme.textStyles.sub,
                    color = SaltTheme.colors.subText,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}
