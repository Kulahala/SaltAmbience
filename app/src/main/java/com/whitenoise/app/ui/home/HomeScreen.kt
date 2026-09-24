package com.whitenoise.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import android.os.Build
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.ui.MainViewModel
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.SoundCategory
import com.whitenoise.app.ui.components.AboutBottomSheet
import com.whitenoise.app.ui.components.BauhausSoundIcon
import com.whitenoise.app.ui.components.BauhausUiIcon
import com.whitenoise.app.ui.components.BauhausUiSymbol
import com.whitenoise.app.ui.components.getContentColor
import com.whitenoise.app.ui.components.getThemeColor
import com.whitenoise.app.ui.components.toBauhausSymbol
import com.whitenoise.app.ui.components.BottomPlayerBar
import com.whitenoise.app.ui.components.DeletePresetConfirmBottomSheet
import com.whitenoise.app.ui.components.ImportPresetBottomSheet
import com.whitenoise.app.ui.components.MixerBottomSheet
import com.whitenoise.app.ui.components.SavePresetBottomSheet
import com.whitenoise.app.ui.components.SettingsBottomSheet
import com.whitenoise.app.ui.components.SleepTimerBottomSheet
import com.whitenoise.app.ui.components.SoundTileCard
import com.whitenoise.app.ui.components.ThemeSelectionBottomSheet

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.7.3"
        } catch (_: Exception) {
            "1.7.3"
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    val tracks by viewModel.tracks.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val presets by viewModel.presets.collectAsState()

    val showSleepDialog by viewModel.showSleepTimerDialog.collectAsState()
    val showAboutDialog by viewModel.showAboutDialog.collectAsState()
    val showSavePresetDialog by viewModel.showSavePresetDialog.collectAsState()
    val showThemeDialog by viewModel.showThemeDialog.collectAsState()
    val showImportDialog by viewModel.showImportDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val backgroundPlaybackEnabled by viewModel.backgroundPlaybackEnabled.collectAsState()
    val detectedPayload by viewModel.clipboardDetectedPayload.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isPresetHintDismissed by viewModel.isPresetHintDismissed.collectAsState()

    var presetPendingDelete by remember { mutableStateOf<Preset?>(null) }
    var selectedCategory by remember { mutableStateOf(SoundCategory.ALL) }

    // Map of currently active tracks (playing, unmuted, positive volume) for preset match detection
    val activeTracksMap = remember(tracks) {
        tracks
            .filter { it.isPlaying && !it.isMuted && it.volume > 0.001f }
            .associate { it.id to it.volume }
    }

    // Keep screen on when enabled in settings AND audio is actively playing
    val shouldKeepScreenOn = keepScreenOn && playbackState.isMasterPlaying && activeTracksMap.isNotEmpty()
    DisposableEffect(shouldKeepScreenOn) {
        val activity = context as? Activity
        if (shouldKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Observe Toast feedback events
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Inspect clipboard when Activity resumes; pause playback on stop if background playback is disabled
    DisposableEffect(lifecycleOwner, backgroundPlaybackEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                    viewModel.inspectClipboard(clipText)
                } catch (e: Exception) {
                    // Ignore
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                if (!backgroundPlaybackEnabled && viewModel.playbackState.value.isMasterPlaying) {
                    viewModel.pauseMasterPlay()
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

    val isAnySheetOpen = showMixerSheet || showSleepDialog || showAboutDialog ||
        showSavePresetDialog || showThemeDialog || showImportDialog || showSettingsDialog || (presetPendingDelete != null)

    // Smooth backdrop blur (14.dp provides elegant legibility reduction without excessive GPU convolution overhead)
    val animatedBlurRadius by animateDpAsState(
        targetValue = if (isAnySheetOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 14.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "HomeScreenBackdropBlur"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
    ) {
        // Main Screen Content (Pure Backdrop Blur on modal open, no artificial scaling/corner warping)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && animatedBlurRadius > 0.dp) {
                        Modifier.blur(animatedBlurRadius)
                    } else {
                        Modifier
                    }
                )
        ) {
            val gridState = rememberLazyGridState()
            val density = LocalDensity.current
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val presetsRowHeight = 54.dp
            val collapsibleHeight = 90.dp // TitleBar (54dp) + SceneTitleBar (36dp)
            val collapsibleHeightPx = with(density) { collapsibleHeight.toPx() }

            val collapseFraction by remember {
                derivedStateOf {
                    if (gridState.firstVisibleItemIndex > 0) {
                        1f
                    } else {
                        (gridState.firstVisibleItemScrollOffset.toFloat() / collapsibleHeightPx).coerceIn(0f, 1f)
                    }
                }
            }

            val isDark = SaltTheme.configs.isDarkTheme
            val topBarBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else SaltTheme.colors.text.copy(alpha = 0.06f)
            val headerBgColor = SaltTheme.colors.background

            // Adaptive Bento Grid as Main Scroll Container (Multi-device responsive: 2 columns on phone, 3-4 on tablet/landscape)
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = statusBarTop + collapsibleHeight + presetsRowHeight,
                    bottom = 130.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

            // Lightweight detected clipboard banner (Full width span)
            if (detectedPayload != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                                BauhausUiIcon(
                                    symbol = BauhausUiSymbol.Import,
                                    modifier = Modifier.size(13.dp)
                                )
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
                                BauhausUiIcon(
                                    symbol = BauhausUiSymbol.Close,
                                    modifier = Modifier.size(10.dp),
                                    tint = SaltTheme.colors.highlight.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
                // 预设长按提示框（支持关闭后不再显示）
                if (!isPresetHintDismissed) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SaltTheme.colors.subBackground)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "长按预设卡片可快速复制并导出分享口令",
                                        style = SaltTheme.textStyles.sub,
                                        fontSize = 11.sp,
                                        color = SaltTheme.colors.text.copy(alpha = 0.65f)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { viewModel.dismissPresetHint() }
                                        .padding(4.dp)
                                ) {
                                    BauhausUiIcon(
                                        symbol = BauhausUiSymbol.Close,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Sound Matrix Header & Category Filter (Full width span)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
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

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SoundCategory.entries.forEach { category ->
                                val isSelected = category == selectedCategory
                                val categoryColor = category.getThemeColor()
                                val isDark = SaltTheme.configs.isDarkTheme
                                val contentColor = category.getContentColor(isDark)

                                val chipBgColor = if (isSelected) {
                                    categoryColor.copy(alpha = 0.16f)
                                } else {
                                    SaltTheme.colors.subBackground
                                }
                                val chipBorderColor = if (isSelected) {
                                    categoryColor.copy(alpha = 0.55f)
                                } else {
                                    SaltTheme.colors.text.copy(alpha = 0.08f)
                                }
                                val chipTextColor = if (isSelected) {
                                    contentColor
                                } else {
                                    SaltTheme.colors.text.copy(alpha = 0.65f)
                                }
                                val chipIconColor = if (isSelected) {
                                    contentColor
                                } else {
                                    SaltTheme.colors.text.copy(alpha = 0.45f)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(chipBgColor)
                                        .border(
                                            width = 1.dp,
                                            color = chipBorderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            if (selectedCategory != category) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedCategory = category
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val categoryIconId = when (category) {
                                            SoundCategory.ALL -> "master"
                                            SoundCategory.RAIN -> "rain"
                                            SoundCategory.NATURE -> "wind"
                                            SoundCategory.LIFE -> "coffee_shop"
                                            SoundCategory.NOISE -> "white_noise"
                                        }
                                        BauhausSoundIcon(
                                            trackId = categoryIconId,
                                            isPlaying = isSelected,
                                            modifier = Modifier.size(13.dp),
                                            tint = chipIconColor
                                        )
                                        Text(
                                            text = category.title,
                                            style = SaltTheme.textStyles.sub,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = chipTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val displayedTracks = if (selectedCategory == SoundCategory.ALL) {
                    tracks
                } else {
                    tracks.filter { selectedCategory.matches(it.id) }
                }

                // Bento Sound Tiles (2 columns)
                items(displayedTracks, key = { it.id }) { track ->
                    SoundTileCard(
                        track = track,
                        onTogglePlay = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleTrackPlay(track.id)
                        }
                    )
                }
            }

            // Collapsing Sticky Top Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(headerBgColor)
                    .drawBehind {
                        if (collapseFraction > 0.05f) {
                            drawLine(
                                color = topBarBorderColor.copy(alpha = topBarBorderColor.alpha * collapseFraction),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                    .statusBarsPadding()
            ) {
                // Collapsible Title Section (Brand + Scene bar)
                val currentCollapsibleHeight = collapsibleHeight * (1f - collapseFraction)
                if (currentCollapsibleHeight > 1.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentCollapsibleHeight)
                            .clipToBounds()
                            .alpha((1f - collapseFraction * 1.5f).coerceIn(0f, 1f))
                    ) {
                        // Part 1: Brand Header with subtle version badge & Theme Switcher (Height: 54.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setShowAboutDialog(true)
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "SaltAmbience",
                                        style = SaltTheme.textStyles.largeTitle,
                                        color = SaltTheme.colors.text
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SaltTheme.colors.subBackground)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "v$versionName",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SaltTheme.colors.text.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "椒盐美学 · 多轨自然声混音",
                                    style = SaltTheme.textStyles.sub,
                                    color = SaltTheme.colors.text.copy(alpha = 0.65f)
                                )
                            }

                            // Top Right: Compact Settings Button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SaltTheme.colors.subBackground)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setShowSettingsDialog(true)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BauhausUiIcon(
                                        symbol = BauhausUiSymbol.Settings,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "设置",
                                        style = SaltTheme.textStyles.sub,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SaltTheme.colors.text.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }

                        // Part 2: Scene Title Bar (Height: 36.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "场景方案",
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SaltTheme.colors.text
                            )
                        }
                    }
                }

                // Part 3: Sticky Presets Horizontal Scroll Row (Height: 54.dp, always visible)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(presetsRowHeight)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val isPresetActive = remember(preset, activeTracksMap) { preset.matchesTracks(activeTracksMap) }
                        val animatedBorderColor by animateColorAsState(
                            targetValue = if (isPresetActive) SaltTheme.colors.highlight else Color.Transparent,
                            animationSpec = tween(150),
                            label = "preset_border"
                        )
                        val animatedBgColor by animateColorAsState(
                            targetValue = if (isPresetActive) SaltTheme.colors.highlight.copy(alpha = 0.10f) else SaltTheme.colors.subBackground,
                            animationSpec = tween(150),
                            label = "preset_bg"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = animatedBorderColor,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(animatedBgColor)
                                .combinedClickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.applyPreset(preset)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.copyPresetShareCode(preset)
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(
                                        text = preset.name,
                                        style = SaltTheme.textStyles.main,
                                        fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPresetActive) SaltTheme.colors.highlight else SaltTheme.colors.text
                                    )
                                    if (preset.description.isNotBlank()) {
                                        Text(
                                            text = preset.description,
                                            style = SaltTheme.textStyles.sub,
                                            fontSize = 11.sp,
                                            color = if (isPresetActive) SaltTheme.colors.highlight.copy(alpha = 0.85f) else SaltTheme.colors.text.copy(alpha = 0.65f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.text.copy(alpha = 0.08f))
                                        .clickable { presetPendingDelete = preset },
                                    contentAlignment = Alignment.Center
                                ) {
                                    BauhausUiIcon(
                                        symbol = BauhausUiSymbol.Close,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // "+ 存为预设" 卡片
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
                            .clickable { viewModel.setShowSavePresetDialog(true) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BauhausUiIcon(
                                symbol = BauhausUiSymbol.Add,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "存为预设",
                                color = SaltTheme.colors.highlight,
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // 导入按钮（吸顶常驻胶囊）
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SaltTheme.colors.subBackground)
                            .border(
                                width = 1.dp,
                                color = SaltTheme.colors.text.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setShowImportDialog(true) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BauhausUiIcon(
                                symbol = BauhausUiSymbol.Import,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "导入",
                                color = SaltTheme.colors.text.copy(alpha = 0.75f),
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Floating Compact Mini Pill Player Bar
            BottomPlayerBar(
                playbackState = playbackState,
                activeTracks = activeTracks,
                onToggleMasterPlay = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.toggleMasterPlay()
                },
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
        }

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
            onToggleMasterPlay = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.toggleMasterPlay()
            },
            onStopAll = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.stopAll()
            }
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

        AboutBottomSheet(
            isVisible = showAboutDialog,
            onDismiss = { viewModel.setShowAboutDialog(false) }
        )

        SavePresetBottomSheet(
            isVisible = showSavePresetDialog,
            onSave = { name -> viewModel.saveCurrentAsPreset(name) },
            onDismiss = { viewModel.setShowSavePresetDialog(false) }
        )

        ThemeSelectionBottomSheet(
            isVisible = showThemeDialog,
            currentMode = themeMode,
            onSelectMode = { mode -> viewModel.setThemeMode(mode) },
            onDismiss = { viewModel.setShowThemeDialog(false) }
        )

        SettingsBottomSheet(
            isVisible = showSettingsDialog,
            onDismiss = { viewModel.setShowSettingsDialog(false) },
            keepScreenOn = keepScreenOn,
            onKeepScreenOnChange = { viewModel.setKeepScreenOn(it) },
            backgroundPlaybackEnabled = backgroundPlaybackEnabled,
            onBackgroundPlaybackEnabledChange = { viewModel.setBackgroundPlaybackEnabled(it) },
            themeMode = themeMode,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            versionName = versionName,
            onOpenAbout = {
                viewModel.setShowSettingsDialog(false)
                viewModel.setShowAboutDialog(true)
            },
            onRestorePresets = {
                viewModel.restoreDefaultPresets()
            }
        )

        ImportPresetBottomSheet(
            isVisible = showImportDialog,
            initialPayload = detectedPayload,
            currentPresets = presets,
            onImport = { payload, applyImmediately ->
                viewModel.importPreset(payload, applyImmediately)
            },
            onRestoreDefaultPreset = { presetId ->
                viewModel.restoreSingleDefaultPreset(presetId)
            },
            onRestoreAllDefaults = {
                viewModel.restoreDefaultPresets()
            },
            onPresetAlreadyExists = { preset ->
                viewModel.notifyPresetAlreadyExists(preset.name)
            },
            onDismiss = { viewModel.setShowImportDialog(false) }
        )

        DeletePresetConfirmBottomSheet(
            preset = presetPendingDelete,
            onConfirm = { preset -> viewModel.deletePreset(preset.id) },
            onDismiss = { presetPendingDelete = null }
        )
    }
}
