package com.whitenoise.app.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.PresetShareCode
import com.whitenoise.app.core.model.PresetSharePayload
import com.whitenoise.app.data.repository.SoundRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportPresetDialog(
    initialPayload: PresetSharePayload? = null,
    onImport: (PresetSharePayload, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var parsedPayload by remember { mutableStateOf<PresetSharePayload?>(initialPayload) }
    var applyImmediately by remember { mutableStateOf(true) }

    // If opened with initial payload, or try auto-reading clipboard if empty
    LaunchedEffect(Unit) {
        if (initialPayload != null) {
            parsedPayload = initialPayload
        } else {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                if (!clipText.isNullOrBlank()) {
                    val detected = PresetShareCode.parseShareText(clipText)
                    if (detected != null) {
                        inputText = clipText
                        parsedPayload = detected
                    }
                }
            } catch (e: Exception) {
                // Clipboard read exception safely ignored
            }
        }
    }

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
                // Header
                Text(
                    text = "导入混音方案",
                    style = SaltTheme.textStyles.main,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SaltTheme.colors.text
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "粘贴朋友分享的混音口令即可一键载入",
                    style = SaltTheme.textStyles.sub,
                    color = SaltTheme.colors.text.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input Box with "Paste" button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SaltTheme.colors.subBackground)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "口令或 JSON 代码",
                                style = SaltTheme.textStyles.sub,
                                fontSize = 12.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.5f)
                            )

                            // Quick paste button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
                                    .clickable {
                                        try {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                            if (!clip.isNullOrBlank()) {
                                                inputText = clip
                                                parsedPayload = PresetShareCode.parseShareText(clip)
                                            }
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "从剪贴板粘贴",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SaltTheme.colors.highlight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        BasicTextField(
                            value = inputText,
                            onValueChange = {
                                inputText = it
                                parsedPayload = PresetShareCode.parseShareText(it)
                            },
                            textStyle = SaltTheme.textStyles.sub.copy(
                                color = SaltTheme.colors.text,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(SaltTheme.colors.highlight),
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "支持粘贴整段微信分享文案，系统会自动提取 \$SaltAmbience#...$",
                                        style = SaltTheme.textStyles.sub,
                                        fontSize = 12.sp,
                                        color = SaltTheme.colors.text.copy(alpha = 0.35f)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Parsed preview card
                val payload = parsedPayload
                if (payload != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, SaltTheme.colors.highlight, RoundedCornerShape(14.dp))
                            .background(SaltTheme.colors.highlight.copy(alpha = 0.08f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎧",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = payload.name,
                                        style = SaltTheme.textStyles.main,
                                        fontWeight = FontWeight.Bold,
                                        color = SaltTheme.colors.highlight
                                    )
                                    if (payload.description.isNotBlank()) {
                                        Text(
                                            text = payload.description,
                                            style = SaltTheme.textStyles.sub,
                                            fontSize = 12.sp,
                                            color = SaltTheme.colors.text.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sound Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val trackNameMap = SoundRepository.ALL_TRACKS.associate { it.id to (it.iconEmoji to it.name) }
                                payload.volumes.forEach { (trackId, volume) ->
                                    val (emoji, name) = trackNameMap[trackId] ?: ("🎵" to trackId)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SaltTheme.colors.subBackground)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$emoji $name ${(volume * 100).toInt()}%",
                                            style = SaltTheme.textStyles.sub,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SaltTheme.colors.text
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Apply immediately toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { applyImmediately = !applyImmediately }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = if (applyImmediately) SaltTheme.colors.highlight else SaltTheme.colors.text.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .background(if (applyImmediately) SaltTheme.colors.highlight else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (applyImmediately) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "导入后立即播放此方案",
                            style = SaltTheme.textStyles.sub,
                            fontSize = 13.sp,
                            color = SaltTheme.colors.text.copy(alpha = 0.85f)
                        )
                    }
                } else if (inputText.isNotBlank()) {
                    Text(
                        text = "⚠️ 未能识别出合法的混音口令，请检查内容是否完整",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 12.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SaltTheme.colors.subBackground)
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "取消",
                            style = SaltTheme.textStyles.main,
                            fontWeight = FontWeight.Medium,
                            color = SaltTheme.colors.text.copy(alpha = 0.7f)
                        )
                    }

                    val canConfirm = parsedPayload != null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (canConfirm) SaltTheme.colors.highlight else SaltTheme.colors.highlight.copy(alpha = 0.35f))
                            .clickable(enabled = canConfirm) {
                                parsedPayload?.let { onImport(it, applyImmediately) }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "确认导入",
                            style = SaltTheme.textStyles.main,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
