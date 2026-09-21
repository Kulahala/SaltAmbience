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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

@Composable
fun SavePresetDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SaltTheme.colors.background)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "保存当前混音为预设",
                    style = SaltTheme.textStyles.main,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SaltTheme.colors.text
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "将当前开启的音轨及音量方案保存为自定义预设，方便随时切换。",
                    style = SaltTheme.textStyles.sub,
                    color = SaltTheme.colors.text.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaltTheme.colors.subBackground)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (presetName.isEmpty()) {
                        Text(
                            text = "请输入预设名称（如：午后读书）",
                            style = SaltTheme.textStyles.sub,
                            color = SaltTheme.colors.text.copy(alpha = 0.45f)
                        )
                    }
                    BasicTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = SaltTheme.textStyles.main.copy(color = SaltTheme.colors.text)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "取消",
                            style = SaltTheme.textStyles.sub,
                            color = SaltTheme.colors.text.copy(alpha = 0.65f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (presetName.isNotBlank()) SaltTheme.colors.highlight
                                else SaltTheme.colors.subBackground
                            )
                            .clickable(enabled = presetName.isNotBlank()) {
                                onSave(presetName.trim())
                                onDismiss()
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "保存",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (presetName.isNotBlank()) androidx.compose.ui.graphics.Color.White else SaltTheme.colors.text.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}
