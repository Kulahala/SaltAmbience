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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.ui.MainViewModel
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import com.whitenoise.app.ui.components.AboutDialog
import com.whitenoise.app.ui.components.BottomPlayerBar
import com.whitenoise.app.ui.components.ImportPresetDialog
import com.whitenoise.app.ui.components.MixerBottomSheet
import com.whitenoise.app.ui.components.SavePresetDialog
import com.whitenoise.app.ui.components.SleepTimerBottomSheet
import com.whitenoise.app.ui.components.SoundTileCard
import com.whitenoise.app.ui.components.ThemeSelectionDialog

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val tracks by viewModel.tracks.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val presets by viewModel.presets.collectAsState()

    val showSleepDialog by viewModel.showSleepTimerDialog.collectAsState()
    val showAboutDialog by viewModel.showAboutDialog.collectAsState()
    val showSavePresetDialog by viewModel.showSavePresetDialog.collectAsState()
    val showThemeDialog by viewModel.showThemeDialog.collectAsState()
    val showImportDialog by viewModel.showImportDialog.collectAsState()
    val detectedPayload by viewModel.clipboardDetectedPayload.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    // Observe Toast feedback events
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Inspect clipboard when Activity resumes to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                    viewModel.inspectClipboard(clipText)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Control center bottom sheet visibility
    var showMixerSheet by remember { mutableStateOf(false) }

    val activeTracks = remember(tracks) { tracks.filter { it.isPlaying } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
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
                        color = SaltTheme.colors.text.copy(alpha = 0.65f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme Switch Button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SaltTheme.colors.subBackground)
                            .clickable { viewModel.setShowThemeDialog(true) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = themeMode.iconEmoji,
                                fontSize = 13.sp
                            )
                            Text(
                                text = when (themeMode) {
                                    com.whitenoise.app.core.model.ThemeMode.SYSTEM -> "系统"
                                    com.whitenoise.app.core.model.ThemeMode.LIGHT -> "浅色"
                                    com.whitenoise.app.core.model.ThemeMode.DARK -> "深色"
                                },
                                style = SaltTheme.textStyles.sub,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SaltTheme.colors.text.copy(alpha = 0.75f)
                            )
                        }
                    }

                    // About Button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SaltTheme.colors.subBackground)
                            .clickable { viewModel.setShowAboutDialog(true) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "关于",
                            style = SaltTheme.textStyles.sub,
                            fontWeight = FontWeight.Medium,
                            color = SaltTheme.colors.text.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Lightweight detected clipboard banner
            if (detectedPayload != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
                        .clickable { viewModel.setShowImportDialog(true) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎧", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "检测到混音【${detectedPayload?.name}】，点击一键导入",
                                style = SaltTheme.textStyles.sub,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaltTheme.colors.highlight,
                                maxLines = 1
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { viewModel.dismissClipboardBanner() }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 11.sp,
                                color = SaltTheme.colors.highlight.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            // 2-Column Bento Grid for Sound Tiles
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Presets (Span 2)
                item(span = { GridItemSpan(2) }) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "场景方案",
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SaltTheme.colors.text
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // 📥 导入按钮
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.subBackground)
                                        .clickable { viewModel.setShowImportDialog(true) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = "📥", fontSize = 11.sp)
                                        Text(
                                            text = "导入",
                                            style = SaltTheme.textStyles.sub,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SaltTheme.colors.text.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // ➕ 保存当前
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
                                        .clickable { viewModel.setShowSavePresetDialog(true) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "+",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SaltTheme.colors.highlight
                                        )
                                        Text(
                                            text = "保存当前",
                                            style = SaltTheme.textStyles.sub,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SaltTheme.colors.highlight
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SaltTheme.colors.subBackground)
                                        .clickable { viewModel.applyPreset(preset) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(
                                                text = preset.name,
                                                style = SaltTheme.textStyles.main,
                                                fontWeight = FontWeight.Medium,
                                                color = SaltTheme.colors.text
                                            )
                                            if (preset.description.isNotBlank()) {
                                                Text(
                                                    text = preset.description,
                                                    style = SaltTheme.textStyles.sub,
                                                    fontSize = 11.sp,
                                                    color = SaltTheme.colors.text.copy(alpha = 0.65f)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Share Preset Button
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable { viewModel.copyPresetShareCode(preset) }
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "📤",
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (!preset.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.deletePreset(preset.id) }
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "✕",
                                                    color = SaltTheme.colors.text.copy(alpha = 0.45f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Add Custom Preset Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
                                    .clickable { viewModel.setShowSavePresetDialog(true) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ 存为预设",
                                    color = SaltTheme.colors.highlight,
                                    style = SaltTheme.textStyles.main,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Section 2: Sound Matrix Header (Span 2)
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "音效矩阵",
                            style = SaltTheme.textStyles.main,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SaltTheme.colors.text
                        )
                        Text(
                            text = if (activeTracks.isNotEmpty()) "已开启 ${activeTracks.size} 轨" else "轻触卡片开启混音",
                            style = SaltTheme.textStyles.sub,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (activeTracks.isNotEmpty()) SaltTheme.colors.highlight else SaltTheme.colors.text.copy(alpha = 0.65f)
                        )
                    }
                }

                // Bento Sound Tiles (2 columns)
                items(tracks, key = { it.id }) { track ->
                    SoundTileCard(
                        track = track,
                        onTogglePlay = { viewModel.toggleTrackPlay(track.id) }
                    )
                }
            }
        }

        // Floating Compact Mini Pill Player Bar
        BottomPlayerBar(
            playbackState = playbackState,
            activeTracks = activeTracks,
            onToggleMasterPlay = { viewModel.toggleMasterPlay() },
            onOpenMixer = {
                viewModel.setShowSleepTimerDialog(false)
                showMixerSheet = true
            },
            onOpenSleepTimer = {
                showMixerSheet = false
                viewModel.setShowSleepTimerDialog(true)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Expandable HyperOS / iOS Style Multi-Track Mixer Bottom Sheet
        MixerBottomSheet(
            isVisible = showMixerSheet,
            onDismiss = { showMixerSheet = false },
            masterVolume = playbackState.masterVolume,
            onMasterVolumeChange = { viewModel.setMasterVolume(it) },
            activeTracks = activeTracks,
            onTrackVolumeChange = { trackId, volume -> viewModel.setTrackVolume(trackId, volume) },
            onToggleTrackMute = { trackId -> viewModel.toggleTrackMute(trackId) },
            isMasterPlaying = playbackState.isMasterPlaying,
            onToggleMasterPlay = { viewModel.toggleMasterPlay() },
            onStopAll = { viewModel.stopAll() }
        )

        // Standalone Modern Music Player Style Sleep Timer Bottom Sheet
        SleepTimerBottomSheet(
            isVisible = showSleepDialog,
            onDismiss = { viewModel.setShowSleepTimerDialog(false) },
            isSleepTimerRunning = playbackState.isSleepTimerRunning,
            sleepTimerRemainingSeconds = playbackState.sleepTimerRemainingSeconds,
            onStartTimer = { mins -> viewModel.startSleepTimer(mins) },
            onCancelTimer = { viewModel.cancelSleepTimer() }
        )

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

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentMode = themeMode,
                onSelectMode = { mode -> viewModel.setThemeMode(mode) },
                onDismiss = { viewModel.setShowThemeDialog(false) }
            )
        }

        if (showImportDialog) {
            ImportPresetDialog(
                initialPayload = detectedPayload,
                onImport = { payload, applyImmediately ->
                    viewModel.importPreset(payload, applyImmediately)
                },
                onDismiss = { viewModel.setShowImportDialog(false) }
            )
        }
    }
}
