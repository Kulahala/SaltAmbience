package com.whitenoise.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SaltTheme.colors.background)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "关于 SaltAmbience",
                    style = SaltTheme.textStyles.main,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SaltTheme.colors.text
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "v1.3.0 · 椒盐美学自然声音混音器",
                    style = SaltTheme.textStyles.sub,
                    color = SaltTheme.colors.text.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                RoundedColumn {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "开源致谢与音源许可",
                            style = SaltTheme.textStyles.main,
                            fontWeight = FontWeight.Bold,
                            color = SaltTheme.colors.text
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "本项目音效资产源自 Rafael Mardojai 的开源项目 Blanket，经过无缝交叉淡化循环处理：\n\n" +
                                    "• 细雨 (Rain) - alex36917 (CC BY 4.0)\n" +
                                    "• 雷雨 (Storm) - Digifish music (CC BY 3.0)\n" +
                                    "• 林风 (Wind) - felix.blume (CC0 1.0)\n" +
                                    "• 溪流 (Stream) - gluckose (CC0 1.0)\n" +
                                    "• 篝火 (Fireplace) - ezwa (Public Domain)\n" +
                                    "• 鸟鸣 (Birds) - kvgarlic (CC0 1.0)\n" +
                                    "• 夏夜 (Summer Night) - Lisa Redfern (Public Domain)\n" +
                                    "• 白噪音 (White Noise) - Jorge Stolfi (CC BY-SA 3.0)\n\n" +
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

                Spacer(modifier = Modifier.height(16.dp))
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
                        color = SaltTheme.colors.highlight
                    )
                }
            }
        }
    }
}
