package com.whitenoise.app.ui.components

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

/**
 * Modern SaltUI bottom drawer for About info and audio assets licensing credits.
 * Features fixed header & footer with an internal scrollable Bento card
 * equipped with an elegant minimalist Bauhaus scrollbar.
 */
@Composable
fun AboutBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.9.1"
        } catch (e: Exception) {
            "1.9.1"
        }
    }

    SaltBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header Row (Fixed at top)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "关于 SaltAmbience",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "v$versionName · 椒盐美学自然声音混音器",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 12.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.65f)
                    )
                }

                // Close button (Unified 32.dp circular capsule)
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

            // Scrollable Licensing Bento Card with Bauhaus Custom Scrollbar
            val scrollState = rememberScrollState()
            val isDark = SaltTheme.configs.isDarkTheme
            val scrollbarColor = if (isDark) Color.White.copy(alpha = 0.28f) else SaltTheme.colors.text.copy(alpha = 0.20f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SaltTheme.colors.subBackground)
                    .saltScrollbar(
                        state = scrollState,
                        color = scrollbarColor,
                        width = 3.5.dp,
                        paddingEnd = 5.dp
                    )
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "开源致谢与音源许可",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        color = SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "本项目音效资产源自 Rafael Mardojai 的开源项目 Blanket、Moodist 及社区公共领域贡献，全部经过采样级无缝交叉淡化循环处理（全量 22 款音效）：\n\n" +
                                "• 细雨 (Rain) - alex36917 (CC BY 4.0)\n" +
                                "• 雷雨 (Storm) - Digifish music (CC BY 3.0)\n" +
                                "• 林风 (Wind) - felix.blume (CC0 1.0)\n" +
                                "• 溪流 (Stream) - gluckose (CC0 1.0)\n" +
                                "• 篝火 (Fireplace) - ezwa (Public Domain)\n" +
                                "• 鸟鸣 (Birds) - kvgarlic (CC0 1.0)\n" +
                                "• 夏夜 (Summer Night) - Lisa Redfern (Public Domain)\n" +
                                "• 白噪音 (White Noise) - Jorge Stolfi (CC BY-SA 3.0)\n" +
                                "• 海浪 (Waves) - Luftrum (CC BY 3.0)\n" +
                                "• 咖啡馆 (Coffee Shop) - stephan (Public Domain)\n" +
                                "• 列车 (Train) - SDLx (CC BY 3.0)\n" +
                                "• 小舟 (Boat) - Falcet (CC0 1.0)\n" +
                                "• 粉红噪 (Pink Noise) - Omegatron (CC BY-SA 3.0)\n" +
                                "• 都市 (City) - gezortenplotz (CC BY 3.0)\n" +
                                "• 棕色噪音 (Brown Noise) - Omegatron (CC BY-SA 3.0)\n" +
                                "• 电风扇 (Fan) - Moodist / MAZE (MIT / CC0)\n" +
                                "• 钟表 (Clock) - Moodist / MAZE (MIT / CC0)\n" +
                                "• 机械键盘 (Keyboard) - Moodist / MAZE (MIT / CC0)\n" +
                                "• 风铃 (Wind Chimes) - Moodist / MAZE (MIT / CC0)\n" +
                                "• 雨打屋檐 (Rain Roof) - Moodist / MAZE (MIT / CC0)\n" +
                                "• 深海水声 (Underwater) - Moodist / MAZE (MIT / CC0)\n" +
                                "• 科学绿噪 (Green Noise) - SaltAmbience (CC0 1.0 自研算法)\n\n" +
                                "完整许可条款已归档至 SOUNDS_LICENSING.md。\n\n" +
                                "技术基座：\n" +
                                "• SaltUI 3.x 设计规范\n" +
                                "• AndroidX Media3 ExoPlayer 多轨引擎\n" +
                                "• Jetpack DataStore 状态记忆",
                        style = SaltTheme.textStyles.sub,
                        color = SaltTheme.colors.text.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button (Fixed at bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaltTheme.colors.subBackground)
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "我知道了",
                    style = SaltTheme.textStyles.main,
                    fontWeight = FontWeight.Medium,
                    color = SaltTheme.colors.highlight
                )
            }
        }
    }
}

/**
 * Elegant minimalist custom scrollbar modifier adhering to SaltUI / Bauhaus aesthetics.
 */
private fun Modifier.saltScrollbar(
    state: ScrollState,
    color: Color,
    width: Dp = 3.5.dp,
    paddingEnd: Dp = 5.dp
): Modifier = this.drawWithContent {
    drawContent()
    val totalHeight = size.height
    val maxValue = state.maxValue
    if (maxValue > 0 && totalHeight > 0f) {
        val scrollRange = maxValue + totalHeight
        val thumbHeight = (totalHeight * (totalHeight / scrollRange)).coerceIn(28.dp.toPx(), totalHeight * 0.75f)
        val scrollProgress = state.value.toFloat() / maxValue.toFloat()
        val thumbOffsetY = scrollProgress * (totalHeight - thumbHeight)
        val thumbWidth = width.toPx()
        val thumbX = size.width - thumbWidth - paddingEnd.toPx()

        drawRoundRect(
            color = color,
            topLeft = Offset(thumbX, thumbOffsetY),
            size = Size(thumbWidth, thumbHeight),
            cornerRadius = CornerRadius(thumbWidth / 2f, thumbWidth / 2f)
        )
    }
}
