package com.whitenoise.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.audio.VolumeCalculator
import kotlin.math.roundToInt

/**
 * Modern music-player-style standalone sleep timer bottom sheet.
 * Features:
 * - Direct duration display with manual number input support
 * - Smooth horizontal slider (5m ~ 120m)
 * - Quick preset chips (15, 30, 45, 60, 90 mins)
 * - Prominent start / cancel capsule buttons
 * - Live countdown status when timer is running
 */
@Composable
fun SleepTimerBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isSleepTimerRunning: Boolean,
    sleepTimerRemainingSeconds: Long?,
    onStartTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        BackHandler(onBack = onDismiss)
    }

    var selectedMinutes by remember { mutableIntStateOf(30) }
    var isManualInputMode by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("30") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Synchronize selectedMinutes with running timer when opened, and reset manual mode
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isManualInputMode = false
            if (isSleepTimerRunning && sleepTimerRemainingSeconds != null && sleepTimerRemainingSeconds > 0) {
                val remainingMins = VolumeCalculator.calculateRemainingMinutes(sleepTimerRemainingSeconds).coerceIn(1, 240)
                selectedMinutes = remainingMins
                manualInputText = remainingMins.toString()
            }
        }
    }

    // Auto-focus and trigger soft keyboard when entering manual input mode
    LaunchedEffect(isManualInputMode) {
        if (isManualInputMode) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val shortcutMinutes = listOf(15, 30, 45, 60, 90)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Semi-transparent scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        // Sliding Bottom Sheet
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(SaltTheme.colors.background)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Prevent dismissing */ }
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Top Drag Pill Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDismiss() }
                            .padding(top = 10.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(SaltTheme.colors.subText.copy(alpha = 0.25f))
                        )
                    }

                    // Header Row: Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "睡眠定时",
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SaltTheme.colors.text
                            )

                            if (isSleepTimerRunning) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.highlight.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    val remaining = sleepTimerRemainingSeconds ?: 0L
                                    val m = remaining / 60
                                    val s = remaining % 60
                                    Text(
                                        text = "⏱️ %02d:%02d 倒计时中".format(m, s),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SaltTheme.colors.highlight
                                    )
                                }
                            }
                        }

                        // Close "✕" Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SaltTheme.colors.subBackground)
                                .clickable { onDismiss() }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                color = SaltTheme.colors.text.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Duration Display & Manual Input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SaltTheme.colors.subBackground.copy(alpha = 0.65f))
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isManualInputMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SaltTheme.colors.background)
                                        .border(
                                            width = 1.dp,
                                            color = SaltTheme.colors.highlight,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BasicTextField(
                                        value = manualInputText,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() } && input.length <= 3) {
                                                manualInputText = input
                                            }
                                        },
                                        modifier = Modifier.focusRequester(focusRequester),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                val parsed = manualInputText.toIntOrNull()
                                                if (parsed != null && parsed in 1..240) {
                                                    selectedMinutes = parsed
                                                } else {
                                                    manualInputText = selectedMinutes.toString()
                                                }
                                                isManualInputMode = false
                                                keyboardController?.hide()
                                            }
                                        ),
                                        singleLine = true,
                                        textStyle = SaltTheme.textStyles.main.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SaltTheme.colors.highlight,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "分钟",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SaltTheme.colors.text
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.highlight)
                                        .clickable {
                                            val parsed = manualInputText.toIntOrNull()
                                            if (parsed != null && parsed in 1..240) {
                                                selectedMinutes = parsed
                                            } else {
                                                manualInputText = selectedMinutes.toString()
                                            }
                                            isManualInputMode = false
                                            keyboardController?.hide()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "确定",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.subBackground)
                                        .clickable {
                                            manualInputText = selectedMinutes.toString()
                                            isManualInputMode = false
                                            keyboardController?.hide()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "取消",
                                        color = SaltTheme.colors.text.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        manualInputText = selectedMinutes.toString()
                                        isManualInputMode = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "时长：",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SaltTheme.colors.text.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Text(
                                    text = "$selectedMinutes",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaltTheme.colors.highlight,
                                    lineHeight = 44.sp
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "分钟",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SaltTheme.colors.text,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SaltTheme.colors.background)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        text = "✏️ 输入",
                                        fontSize = 11.sp,
                                        color = SaltTheme.colors.text.copy(alpha = 0.65f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Horizontal Slider (5 ~ 120 mins)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = selectedMinutes.toFloat().coerceIn(5f, 120f),
                            onValueChange = {
                                selectedMinutes = it.roundToInt()
                                manualInputText = selectedMinutes.toString()
                            },
                            valueRange = 5f..120f,
                            colors = SliderDefaults.colors(
                                thumbColor = SaltTheme.colors.highlight,
                                activeTrackColor = SaltTheme.colors.highlight,
                                inactiveTrackColor = SaltTheme.colors.subBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "5 分钟",
                                fontSize = 11.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.55f)
                            )
                            Text(
                                text = "60 分钟",
                                fontSize = 11.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.55f)
                            )
                            Text(
                                text = "120 分钟",
                                fontSize = 11.sp,
                                color = SaltTheme.colors.text.copy(alpha = 0.55f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shortcut Pills Row (15, 30, 45, 60, 90)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        shortcutMinutes.forEach { mins ->
                            val isSelected = selectedMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) SaltTheme.colors.highlight
                                        else SaltTheme.colors.subBackground
                                    )
                                    .clickable {
                                        selectedMinutes = mins
                                        manualInputText = mins.toString()
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else SaltTheme.colors.text
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button(s)
                    if (isSleepTimerRunning) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Cancel Timer Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SaltTheme.colors.subBackground)
                                    .clickable {
                                        onCancelTimer()
                                        onDismiss()
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "关闭定时器",
                                    style = SaltTheme.textStyles.main,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = SaltTheme.colors.text.copy(alpha = 0.8f)
                                )
                            }

                            // Update Timer Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SaltTheme.colors.highlight)
                                    .clickable {
                                        onStartTimer(selectedMinutes)
                                        onDismiss()
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "重设为 ${selectedMinutes} 分钟",
                                    style = SaltTheme.textStyles.main,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Start Timer Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SaltTheme.colors.highlight)
                                .clickable {
                                    onStartTimer(selectedMinutes)
                                    onDismiss()
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "开始 ${selectedMinutes} 分钟定时",
                                style = SaltTheme.textStyles.main,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Explanatory Footer
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💡 到期前将呈对数平滑淡出至静音，结束后自动释放播放服务",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 11.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
