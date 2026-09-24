package com.whitenoise.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.data.repository.SoundRepository

/**
 * Lightweight bottom sheet drawer to confirm preset deletion and prevent accidental removal.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeletePresetConfirmBottomSheet(
    preset: Preset?,
    onConfirm: (Preset) -> Unit,
    onDismiss: () -> Unit
) {
    var lastPreset by remember { mutableStateOf<Preset?>(null) }
    LaunchedEffect(preset) {
        if (preset != null) {
            lastPreset = preset
        }
    }
    val displayPreset = preset ?: lastPreset

    SaltBottomSheet(
        isVisible = preset != null,
        onDismiss = onDismiss
    ) {
        if (displayPreset == null) return@SaltBottomSheet

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        BauhausUiIcon(
                            symbol = BauhausUiSymbol.Clear,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(10.dp))

                    Column {
                        Text(
                            text = if (displayPreset.isDefault) "删除默认预设" else "删除自定义预设",
                            style = SaltTheme.textStyles.main,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SaltTheme.colors.text
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (displayPreset.isDefault) "删除后可在场景方案末尾一键原样恢复" else "删除后将无法恢复该混音方案",
                            style = SaltTheme.textStyles.sub,
                            fontSize = 12.sp,
                            color = SaltTheme.colors.text.copy(alpha = 0.65f)
                        )
                    }
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

            // Preset detail card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SaltTheme.colors.subBackground)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = displayPreset.name,
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = SaltTheme.colors.text
                    )

                    if (displayPreset.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = displayPreset.description,
                            style = SaltTheme.textStyles.sub,
                            fontSize = 12.sp,
                            color = SaltTheme.colors.text.copy(alpha = 0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track tags preview
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        displayPreset.trackVolumes.forEach { (trackId, vol) ->
                            val track = SoundRepository.ALL_TRACKS.find { it.id == trackId }
                            val name = track?.name ?: trackId
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SaltTheme.colors.background)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    BauhausSoundIcon(
                                        trackId = trackId,
                                        isPlaying = false,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "$name ${(vol * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        style = SaltTheme.textStyles.sub,
                                        color = SaltTheme.colors.text.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
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
                        style = SaltTheme.textStyles.sub,
                        fontWeight = FontWeight.Medium,
                        color = SaltTheme.colors.text.copy(alpha = 0.70f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE53935))
                        .clickable {
                            onConfirm(displayPreset)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "确认删除",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
