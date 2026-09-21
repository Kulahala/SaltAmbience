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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Switcher
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.SoundTrack
import kotlin.math.roundToInt

@Composable
fun SoundCard(
    track: SoundTrack,
    onTogglePlay: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoundedColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Track title, subtitle, and Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.name,
                        style = SaltTheme.textStyles.main,
                        color = if (track.isPlaying) SaltTheme.colors.highlight else SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.subtitle,
                        style = SaltTheme.textStyles.sub,
                        color = SaltTheme.colors.subText
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Track Toggle Switcher
                Box(
                    modifier = Modifier.clickable { onTogglePlay() }
                ) {
                    Switcher(
                        state = track.isPlaying
                    )
                }
            }

            // If track is playing, show the fine-grained volume slider and mute controls
            if (track.isPlaying) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute pill button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (track.isMuted) SaltTheme.colors.highlight.copy(alpha = 0.15f)
                                else SaltTheme.colors.subBackground
                            )
                            .clickable { onToggleMute() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (track.isMuted) "已静音" else "静音",
                            fontSize = 11.sp,
                            color = if (track.isMuted) SaltTheme.colors.highlight else SaltTheme.colors.subText
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Material3 Slider styled with SaltTheme colors
                    Slider(
                        value = if (track.isMuted) 0f else track.volume,
                        onValueChange = { onVolumeChange(it) },
                        enabled = !track.isMuted,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = SaltTheme.colors.highlight,
                            activeTrackColor = SaltTheme.colors.highlight,
                            inactiveTrackColor = SaltTheme.colors.subBackground
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Volume percentage text
                    Text(
                        text = if (track.isMuted) "0%" else "${(track.volume * 100).roundToInt()}%",
                        style = SaltTheme.textStyles.sub,
                        color = SaltTheme.colors.subText,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}
