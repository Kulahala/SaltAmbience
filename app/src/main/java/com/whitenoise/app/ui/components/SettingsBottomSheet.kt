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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.ThemeMode

/**
 * Modern SaltUI settings drawer adhering to SaltAmbience architectural standards.
 * Features:
 * - Grouped Bento card layout for playback, appearance, and system settings
 * - Integrated Keep Screen On & Background Playback switches with tactile physics
 * - Smooth inline 3-segment theme mode capsule picker
 * - Unified 32dp circular close button and About & Licensing gateway
 */
@Composable
fun SettingsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    backgroundPlaybackEnabled: Boolean,
    onBackgroundPlaybackEnabledChange: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
    versionName: String,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = SaltTheme.configs.isDarkTheme

    SaltBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Header Row: Title & Unified 32.dp Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "设置与偏好",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "播放保活、屏幕显示与外观",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 12.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.65f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SaltTheme.colors.subBackground)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    BauhausUiIcon(
                        symbol = BauhausUiSymbol.Close,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 1: Playback & System (Bento Group)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SaltTheme.colors.subBackground)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "播放与系统",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaltTheme.colors.highlight
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Item 1.1: Background Playback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "后台继续播放",
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = SaltTheme.colors.text
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "切换到其他应用或锁屏时保持自然声混音播放",
                                style = SaltTheme.textStyles.sub,
                                fontSize = 11.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.60f)
                            )
                        }

                        SaltSwitch(
                            checked = backgroundPlaybackEnabled,
                            onCheckedChange = onBackgroundPlaybackEnabledChange
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Item 1.2: Keep Screen On Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "播放时保持屏幕常亮",
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = SaltTheme.colors.text
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "放在床头或桌面时防止系统自动息屏（仅播放中生效）",
                                style = SaltTheme.textStyles.sub,
                                fontSize = 11.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.60f)
                            )
                        }

                        SaltSwitch(
                            checked = keepScreenOn,
                            onCheckedChange = onKeepScreenOnChange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: Appearance & Theme Mode (Bento Group with Segmented Picker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SaltTheme.colors.subBackground)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "外观主题",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaltTheme.colors.highlight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "选择适合当前使用环境的视觉色彩",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 11.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.60f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3-Segment Capsule Selector: System, Light, Dark
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple(ThemeMode.SYSTEM, "跟随系统", BauhausUiSymbol.ThemeSystem),
                            Triple(ThemeMode.LIGHT, "浅色模式", BauhausUiSymbol.ThemeLight),
                            Triple(ThemeMode.DARK, "深色模式", BauhausUiSymbol.ThemeDark)
                        ).forEach { (mode, title, symbol) ->
                            val isSelected = themeMode == mode
                            val segmentBg by animateColorAsState(
                                targetValue = if (isSelected) SaltTheme.colors.highlight.copy(alpha = 0.16f) else SaltTheme.colors.background,
                                animationSpec = tween(150),
                                label = "segment_bg"
                            )
                            val segmentBorder by animateColorAsState(
                                targetValue = if (isSelected) SaltTheme.colors.highlight else Color.Transparent,
                                animationSpec = tween(150),
                                label = "segment_border"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(width = 1.dp, color = segmentBorder, shape = RoundedCornerShape(12.dp))
                                    .background(segmentBg)
                                    .clickable {
                                        if (themeMode != mode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectThemeMode(mode)
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    BauhausUiIcon(
                                        symbol = symbol,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SaltTheme.colors.highlight else SaltTheme.colors.text
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 3: App & About (Bento Group)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SaltTheme.colors.subBackground)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "关于与支持",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaltTheme.colors.highlight
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row: Version info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "版本信息",
                            style = SaltTheme.textStyles.main,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaltTheme.colors.text
                        )
                        Text(
                            text = "v$versionName (已是最新)",
                            style = SaltTheme.textStyles.sub,
                            fontSize = 12.sp,
                            color = SaltTheme.colors.text.copy(alpha = 0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Row: Open About & Sound Licensing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDismiss()
                                onOpenAbout()
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "开源许可与致谢",
                                style = SaltTheme.textStyles.main,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SaltTheme.colors.text
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "查看 22 款音源授权、第三方开源库与项目主页",
                                style = SaltTheme.textStyles.sub,
                                fontSize = 11.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.60f)
                            )
                        }

                        Text(
                            text = "查看 ›",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaltTheme.colors.highlight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
