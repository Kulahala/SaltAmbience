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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

@Composable
fun SleepTimerDialog(
    isRunning: Boolean,
    remainingSeconds: Long?,
    onSelectMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit,
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
            Column {
                ItemOuterTitle(text = "休眠定时关闭")

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRunning && remainingSeconds != null) {
                        val m = remainingSeconds / 60
                        val s = remainingSeconds % 60
                        "当前正在倒计时：%02d:%02d（到期前将平滑对数淡出）".format(m, s)
                    } else {
                        "定时结束后自动平滑淡出并释放音频播放服务"
                    },
                    style = SaltTheme.textStyles.sub,
                    color = SaltTheme.colors.subText
                )

                Spacer(modifier = Modifier.height(16.dp))

                val options = listOf(
                    1 to "1 分钟 (快速验收)",
                    15 to "15 分钟 (小憩)",
                    30 to "30 分钟 (助眠)",
                    45 to "45 分钟 (深度入睡)",
                    60 to "60 分钟 (长时间)"
                )

                RoundedColumn {
                    options.forEach { (mins, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectMinutes(mins)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = SaltTheme.textStyles.main,
                                color = SaltTheme.colors.text
                            )
                            Text(
                                text = "${mins}m",
                                style = SaltTheme.textStyles.sub,
                                color = SaltTheme.colors.subText
                            )
                        }
                    }
                }

                if (isRunning) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SaltTheme.colors.subBackground)
                            .clickable {
                                onCancelTimer()
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "取消定时器",
                            color = SaltTheme.colors.highlight,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "关闭",
                        style = SaltTheme.textStyles.sub,
                        color = SaltTheme.colors.subText
                    )
                }
            }
        }
    }
}
