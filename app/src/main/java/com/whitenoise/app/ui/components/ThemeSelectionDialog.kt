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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.ThemeMode

@Composable
fun ThemeSelectionDialog(
    currentMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "主题设置",
                    style = SaltTheme.textStyles.main,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SaltTheme.colors.text
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "选择适合当前环境的外观模式",
                    style = SaltTheme.textStyles.sub,
                    color = SaltTheme.colors.text.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThemeOptionItem(
                    mode = ThemeMode.SYSTEM,
                    title = "跟随系统",
                    subtitle = "自动匹配系统深色模式设置",
                    iconEmoji = "🌓",
                    isSelected = currentMode == ThemeMode.SYSTEM,
                    onClick = {
                        onSelectMode(ThemeMode.SYSTEM)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ThemeOptionItem(
                    mode = ThemeMode.LIGHT,
                    title = "浅色模式",
                    subtitle = "清爽纯净，适合白天专注使用",
                    iconEmoji = "☀️",
                    isSelected = currentMode == ThemeMode.LIGHT,
                    onClick = {
                        onSelectMode(ThemeMode.LIGHT)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ThemeOptionItem(
                    mode = ThemeMode.DARK,
                    title = "深色模式",
                    subtitle = "纯净暗调，夜间助眠柔和不刺眼",
                    iconEmoji = "🌙",
                    isSelected = currentMode == ThemeMode.DARK,
                    onClick = {
                        onSelectMode(ThemeMode.DARK)
                        onDismiss()
                    }
                )

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
                        text = "完成",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Medium,
                        color = SaltTheme.colors.text
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    mode: ThemeMode,
    title: String,
    subtitle: String,
    iconEmoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val background = if (isSelected) {
        SaltTheme.colors.highlight.copy(alpha = 0.10f)
    } else {
        SaltTheme.colors.subBackground
    }
    val borderModifier = if (isSelected) {
        Modifier.border(1.5.dp, SaltTheme.colors.highlight, shape)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(borderModifier)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = iconEmoji,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SaltTheme.textStyles.main,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) SaltTheme.colors.highlight else SaltTheme.colors.text
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = SaltTheme.textStyles.sub,
                fontSize = 12.sp,
                color = SaltTheme.colors.text.copy(alpha = 0.65f)
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(SaltTheme.colors.highlight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
