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
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.PresetShareCode
import com.whitenoise.app.core.model.PresetSharePayload
import com.whitenoise.app.data.repository.SoundRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportPresetBottomSheet(
    isVisible: Boolean,
    initialPayload: PresetSharePayload? = null,
    onImport: (PresetSharePayload, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var parsedPayload by remember { mutableStateOf<PresetSharePayload?>(initialPayload) }
    var applyImmediately by remember { mutableStateOf(true) }

    LaunchedEffect(isVisible, initialPayload) {
        if (isVisible) {
            if (initialPayload != null) {
                inputText = ""
                parsedPayload = initialPayload
            } else {
                inputText = ""
                parsedPayload = null
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
    }

    SaltBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "导入混音方案",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "粘贴朋友分享的混音口令即可一键载入",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 12.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.65f)
                    )
                }

                // Close button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SaltTheme.colors.subBackground)
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        style = SaltTheme.textStyles.sub,
                        color = SaltTheme.colors.text.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

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
                                text = "📋 粘贴",
                                style = SaltTheme.textStyles.sub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SaltTheme.colors.highlight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    BasicTextField(
                        value = inputText,
                        onValueChange = { newText ->
                            inputText = newText
                            parsedPayload = PresetShareCode.parseShareText(newText)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        textStyle = SaltTheme.textStyles.sub.copy(
                            color = SaltTheme.colors.text,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(SaltTheme.colors.highlight),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "例如：SaltAmbience://preset/eyJuYW1lIjoi...",
                                    style = SaltTheme.textStyles.sub,
                                    fontSize = 13.sp,
                                    color = SaltTheme.colors.text.copy(alpha = 0.35f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Parsed Result Preview
            val currentPayload = parsedPayload
            if (currentPayload != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SaltTheme.colors.highlight.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .background(SaltTheme.colors.highlight.copy(alpha = 0.05f))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentPayload.name,
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SaltTheme.colors.text
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SaltTheme.colors.highlight.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "有效方案",
                                    fontSize = 11.sp,
                                    color = SaltTheme.colors.highlight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (currentPayload.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentPayload.description,
                                style = SaltTheme.textStyles.sub,
                                fontSize = 12.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.65f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audio track tags
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currentPayload.volumes.forEach { (trackId, volume) ->
                                val track = SoundRepository.ALL_TRACKS.find { it.id == trackId }
                                val name = track?.name ?: trackId
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.subBackground)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        BauhausSoundIcon(
                                            trackId = trackId,
                                            isPlaying = true,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "$name ${(volume * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            style = SaltTheme.textStyles.sub,
                                            color = SaltTheme.colors.text.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option: Apply immediately switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { applyImmediately = !applyImmediately }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "导入后立即播放该方案",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 13.sp,
                        color = SaltTheme.colors.text
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                if (applyImmediately) SaltTheme.colors.highlight else SaltTheme.colors.text.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .background(
                                if (applyImmediately) SaltTheme.colors.highlight else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (applyImmediately) {
                            Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SaltTheme.colors.highlight)
                        .clickable {
                            onImport(currentPayload, applyImmediately)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "立即导入方案",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            } else if (inputText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaltTheme.colors.subBackground)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ 未能识别有效口令，请检查格式后重试",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 12.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

@Deprecated(
    "Use ImportPresetBottomSheet instead",
    ReplaceWith("ImportPresetBottomSheet(isVisible = true, initialPayload = initialPayload, onImport = onImport, onDismiss = onDismiss)")
)
@Composable
fun ImportPresetDialog(
    initialPayload: PresetSharePayload? = null,
    onImport: (PresetSharePayload, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ImportPresetBottomSheet(
        isVisible = true,
        initialPayload = initialPayload,
        onImport = onImport,
        onDismiss = onDismiss
    )
}
