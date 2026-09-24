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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

/**
 * Bottom drawer for saving the current multi-track mix as a custom preset.
 * Full .imePadding() & .navigationBarsPadding() integration guarantees ample viewport space
 * above the soft keyboard when typing preset names.
 */
@Composable
fun SavePresetBottomSheet(
    isVisible: Boolean,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isVisible) {
        if (isVisible) {
            presetName = ""
            // Slight delay to request focus after bottom sheet animation kicks in
            kotlinx.coroutines.delay(150)
            focusRequester.requestFocus()
            keyboardController?.show()
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
                        text = "保存当前混音为预设",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "将当前开启的音轨及音量方案保存为自定义预设，方便随时切换。",
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
                    BauhausUiIcon(
                        symbol = BauhausUiSymbol.Close,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SaltTheme.colors.subBackground)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                if (presetName.isEmpty()) {
                    Text(
                        text = "请输入预设名称（如：午后读书）",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 14.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.40f)
                    )
                }
                BasicTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = SaltTheme.textStyles.main.copy(
                        color = SaltTheme.colors.text,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(SaltTheme.colors.highlight),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (presetName.isNotBlank()) {
                                onSave(presetName.trim())
                                onDismiss()
                            }
                        }
                    )
                )
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
                        .background(
                            if (presetName.isNotBlank()) SaltTheme.colors.highlight
                            else SaltTheme.colors.subBackground
                        )
                        .clickable(enabled = presetName.isNotBlank()) {
                            onSave(presetName.trim())
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "保存预设",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (presetName.isNotBlank()) Color.White else SaltTheme.colors.text.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}
