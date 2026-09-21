package com.whitenoise.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.ui.MainViewModel
import com.whitenoise.app.ui.components.AboutDialog
import com.whitenoise.app.ui.components.BottomPlayerBar
import com.whitenoise.app.ui.components.SavePresetDialog
import com.whitenoise.app.ui.components.SleepTimerDialog
import com.whitenoise.app.ui.components.SoundCard

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tracks by viewModel.tracks.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val presets by viewModel.presets.collectAsState()

    val showSleepDialog by viewModel.showSleepTimerDialog.collectAsState()
    val showAboutDialog by viewModel.showAboutDialog.collectAsState()
    val showSavePresetDialog by viewModel.showSavePresetDialog.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaltTheme.colors.subBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar / Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SaltAmbience",
                        style = SaltTheme.textStyles.largeTitle,
                        color = SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "椒盐美学 · 多轨自然声混音",
                        style = SaltTheme.textStyles.sub,
                        color = SaltTheme.colors.subText
                    )
                }

                // About Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SaltTheme.colors.background)
                        .clickable { viewModel.setShowAboutDialog(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "关于",
                        style = SaltTheme.textStyles.sub,
                        color = SaltTheme.colors.subText
                    )
                }
            }

            // Scrollable Sound List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Section 1: Presets
                item {
                    ItemOuterTitle(
                        text = "场景预设",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SaltTheme.colors.background)
                                    .clickable { viewModel.applyPreset(preset) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(
                                            text = preset.name,
                                            style = SaltTheme.textStyles.main,
                                            color = SaltTheme.colors.text
                                        )
                                        if (preset.description.isNotBlank()) {
                                            Text(
                                                text = preset.description,
                                                style = SaltTheme.textStyles.sub,
                                                fontSize = 11.sp,
                                                color = SaltTheme.colors.subText
                                            )
                                        }
                                    }
                                    if (!preset.isDefault) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "✕",
                                            color = SaltTheme.colors.subText,
                                            fontSize = 12.sp,
                                            modifier = Modifier.clickable {
                                                viewModel.deletePreset(preset.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Add Custom Preset Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
                                .clickable { viewModel.setShowSavePresetDialog(true) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ 存为预设",
                                color = SaltTheme.colors.highlight,
                                style = SaltTheme.textStyles.main,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Section 2: Sound Tracks Matrix
                item {
                    ItemOuterTitle(
                        text = "音效矩阵",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                items(tracks, key = { it.id }) { track ->
                    SoundCard(
                        track = track,
                        onTogglePlay = { viewModel.toggleTrackPlay(track.id) },
                        onVolumeChange = { viewModel.setTrackVolume(track.id, it) },
                        onToggleMute = { viewModel.toggleTrackMute(track.id) }
                    )
                }
            }
        }

        // Floating Bottom Player Bar
        BottomPlayerBar(
            playbackState = playbackState,
            onToggleMasterPlay = { viewModel.toggleMasterPlay() },
            onStopAll = { viewModel.stopAll() },
            onMasterVolumeChange = { viewModel.setMasterVolume(it) },
            onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Dialogs
        if (showSleepDialog) {
            SleepTimerDialog(
                isRunning = playbackState.isSleepTimerRunning,
                remainingSeconds = playbackState.sleepTimerRemainingSeconds,
                onSelectMinutes = { viewModel.startSleepTimer(it) },
                onCancelTimer = { viewModel.cancelSleepTimer() },
                onDismiss = { viewModel.setShowSleepTimerDialog(false) }
            )
        }

        if (showAboutDialog) {
            AboutDialog(
                onDismiss = { viewModel.setShowAboutDialog(false) }
            )
        }

        if (showSavePresetDialog) {
            SavePresetDialog(
                onSave = { name -> viewModel.saveCurrentAsPreset(name) },
                onDismiss = { viewModel.setShowSavePresetDialog(false) }
            )
        }
    }
}
