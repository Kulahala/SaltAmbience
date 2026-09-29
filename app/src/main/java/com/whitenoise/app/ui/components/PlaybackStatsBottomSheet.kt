package com.whitenoise.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.whitenoise.app.core.model.PlaybackStats
import com.whitenoise.app.core.model.SoundCategory
import com.whitenoise.app.data.repository.SoundRepository

/**
 * Resolves dedicated, skeuomorphic acoustic signature color for each individual sound track.
 * Ensures the ranking progress bar visually matches the essence of the sound itself
 * (e.g. rain: cyan water, storm: electric yellow, wind: emerald green, fireplace: flame red-orange,
 * waves: deep navy, keyboard: geek indigo-violet, cat_purr: warm peach).
 */
fun getTrackIndividualColor(trackId: String, isDark: Boolean = false): Color {
    return when (trackId) {
        "fireplace" -> Color(0xFFFF5722) // 烈焰橙红 (Flame Amber-Red)
        "summer_night" -> Color(0xFF8B5CF6) // 静谧夜空紫 (Night Violet)
        "boat" -> Color(0xFF00ACC1) // 碧波湖水蓝 (Lake Turquoise)
        "clock" -> Color(0xFFF59E0B) // 表盘暖金 (Clock Dial Gold)
        "keyboard" -> Color(0xFF818CF8) // 极客霓虹蓝紫 (Indigo Neon)
        "white_noise" -> Color(0xFF94A3B8) // 全频银灰 (Silver Spectrum)
        "brown_noise" -> Color(0xFF8D6E63) // 大地泥土暖褐 (Earthy Warm Brown)
        "pink_noise" -> Color(0xFFF472B6) // 珊瑚柔粉 (Coral Soft Pink)
        "green_noise" -> Color(0xFF10B981) // 森林翡翠绿 (Forest Emerald)
        else -> BauhausSoundTheme.getPalette(trackId, isDark).secondary
    }
}

/**
 * Pure Bauhaus geometric crescent moon vector icon.
 * Crafted from Euclidean circular difference with floating geometric diamond star.
 * Replaces system emojis to maintain 100% vector aesthetic purity.
 */
@Composable
fun BauhausMoonIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // 1. Boolean Euclidean Geometric Crescent Moon
        val cx1 = w * 0.40f
        val cy1 = h * 0.50f
        val r1 = w * 0.38f

        val cx2 = w * 0.62f
        val cy2 = h * 0.36f
        val r2 = w * 0.34f

        val moonPath = Path().apply {
            op(
                Path().apply { addOval(Rect(cx1 - r1, cy1 - r1, cx1 + r1, cy1 + r1)) },
                Path().apply { addOval(Rect(cx2 - r2, cy2 - r2, cx2 + r2, cy2 + r2)) },
                PathOperation.Difference
            )
        }
        drawPath(moonPath, color = color)

        // 2. Floating Bauhaus Geometric 4-point Diamond Star
        val starCenter = Offset(w * 0.74f, h * 0.28f)
        val starSize = w * 0.13f
        val starPath = Path().apply {
            moveTo(starCenter.x, starCenter.y - starSize)
            lineTo(starCenter.x + starSize * 0.35f, starCenter.y - starSize * 0.35f)
            lineTo(starCenter.x + starSize, starCenter.y)
            lineTo(starCenter.x + starSize * 0.35f, starCenter.y + starSize * 0.35f)
            lineTo(starCenter.x, starCenter.y + starSize)
            lineTo(starCenter.x - starSize * 0.35f, starCenter.y + starSize * 0.35f)
            lineTo(starCenter.x - starSize, starCenter.y)
            lineTo(starCenter.x - starSize * 0.35f, starCenter.y - starSize * 0.35f)
            close()
        }
        drawPath(starPath, color = color)
    }
}

/**
 * Rich, atmospheric Bauhaus acoustic resonance totem for empty state.
 * Combines concentric acoustic ripples, sleep crescent aura, and spectrum bars.
 */
@Composable
fun BauhausEmptyStatsTotem(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF818CF8),
    secondaryColor: Color = Color(0xFF38BDF8),
    moonColor: Color = Color(0xFFFBBF24)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        // 1. Outer concentric acoustic ripple arcs
        drawArc(
            color = primaryColor.copy(alpha = 0.22f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.84f, h * 0.84f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = secondaryColor.copy(alpha = 0.35f),
            startAngle = -30f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.20f, h * 0.20f),
            size = Size(w * 0.60f, h * 0.60f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 2. Center-right sleep crescent moon arc (Euclidean circular difference)
        val mcx1 = w * 0.64f
        val mcy1 = h * 0.32f
        val mr1 = w * 0.16f

        val mcx2 = w * 0.73f
        val mcy2 = h * 0.26f
        val mr2 = w * 0.14f

        val totemMoonPath = Path().apply {
            op(
                Path().apply { addOval(Rect(mcx1 - mr1, mcy1 - mr1, mcx1 + mr1, mcy1 + mr1)) },
                Path().apply { addOval(Rect(mcx2 - mr2, mcy2 - mr2, mcx2 + mr2, mcy2 + mr2)) },
                PathOperation.Difference
            )
        }
        drawPath(totemMoonPath, color = moonColor)

        // 3. Central harmonic sound spectrum bars
        val barWidth = 3.dp.toPx()
        // Bar 1 (Left low)
        drawLine(
            color = primaryColor,
            start = Offset(w * 0.36f, h * 0.68f),
            end = Offset(w * 0.36f, h * 0.52f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )
        // Bar 2 (Center high)
        drawLine(
            color = secondaryColor,
            start = Offset(w * 0.48f, h * 0.68f),
            end = Offset(w * 0.48f, h * 0.40f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )
        // Bar 3 (Right medium)
        drawLine(
            color = primaryColor,
            start = Offset(w * 0.60f, h * 0.68f),
            end = Offset(w * 0.60f, h * 0.46f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )

        // 4. Anchor resonance base point
        drawCircle(
            color = secondaryColor,
            radius = barWidth * 0.8f,
            center = Offset(w * 0.48f, h * 0.78f)
        )
    }
}

/**
 * Modern SaltUI bottom sheet for Playback Statistics & Sleep Night Insights.
 * Adheres to SaltAmbience Bauhaus minimalist aesthetics and provides an emotional,
 * companion-driven reflection of user's sound journey.
 */
@Composable
fun PlaybackStatsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    stats: PlaybackStats,
    onRequestReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = SaltTheme.configs.isDarkTheme

    SaltBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Header Row: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "播放统计与伴眠印记",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SaltTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "时光声息 · 记录每一个宁静时刻",
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

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Hero Bento Card: Total Companion Hours & Nights
            val cardBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
            val cardBgColor = if (isDark) Color(0xFF1B1D24) else Color(0xFFF3F4F6)
            val cardShape = RoundedCornerShape(20.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(cardBgColor)
                    .border(1.dp, cardBorderColor, cardShape)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "累计陪伴聆听",
                            style = SaltTheme.textStyles.sub,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaltTheme.colors.text.copy(alpha = 0.65f)
                        )

                        // Lunar Nights Badge (8h standard per night)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SaltTheme.colors.highlight.copy(alpha = if (isDark) 0.16f else 0.10f))
                                .border(
                                    width = 1.dp,
                                    color = SaltTheme.colors.highlight.copy(alpha = if (isDark) 0.45f else 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BauhausMoonIcon(
                                    modifier = Modifier.size(13.dp),
                                    color = SaltTheme.colors.highlight
                                )
                                Text(
                                    text = "约 ${stats.sleepNights} 个安睡之夜",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SaltTheme.colors.highlight,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Big Number Display
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (stats.totalHours >= 1.0f) "${stats.totalHours}" else stats.getFormattedTotalDuration(),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = SaltTheme.colors.text
                        )
                        if (stats.totalHours >= 1.0f) {
                            Text(
                                text = "小时",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaltTheme.colors.text.copy(alpha = 0.65f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Poetic Subtitle
                    Text(
                        text = stats.getCompanionPoeticText(),
                        style = SaltTheme.textStyles.sub,
                        fontSize = 12.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.60f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Section: Sound Footprint Spectrum (Ranked tracks by duration)
            val sortedTracks = stats.getSortedTrackStats()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "声音偏好排行",
                    style = SaltTheme.textStyles.main,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SaltTheme.colors.text
                )

                if (sortedTracks.isNotEmpty()) {
                    Text(
                        text = "共点亮 ${sortedTracks.size} 款音效",
                        style = SaltTheme.textStyles.sub,
                        fontSize = 11.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.50f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sortedTracks.isEmpty()) {
                // Empty state card (Consistent Bento styling + rich acoustic totem)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(cardBgColor)
                        .border(1.dp, cardBorderColor, cardShape)
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BauhausEmptyStatsTotem(
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "尚未产生播放记录",
                            style = SaltTheme.textStyles.main,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SaltTheme.colors.text.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "在主页点亮自然声音，让声息在此沉淀",
                            style = SaltTheme.textStyles.sub,
                            fontSize = 12.sp,
                            color = SaltTheme.colors.text.copy(alpha = 0.50f)
                        )
                    }
                }
            } else {
                // List of ranked tracks (Consistent Bento styling)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(cardBgColor)
                        .border(1.dp, cardBorderColor, cardShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    sortedTracks.forEachIndexed { index, (trackId, _) ->
                        val soundTrack = SoundRepository.getTrackById(trackId)
                        val trackName = soundTrack?.name ?: trackId
                        val category = SoundCategory.fromTrackId(trackId)
                        val themeColor = category.getThemeColor()
                        val trackColor = remember(trackId, isDark) { getTrackIndividualColor(trackId, isDark) }
                        val ratio = stats.getTrackRatio(trackId)

                        val animatedRatio by animateFloatAsState(
                            targetValue = ratio,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                            label = "track_ratio_$trackId"
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Rank index number
                                    Text(
                                        text = "#${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) trackColor else SaltTheme.colors.text.copy(alpha = 0.40f)
                                    )

                                    // Sound Icon (Showcase asset: always display vibrant skeuomorphic dual-tone colors)
                                    BauhausSoundIcon(
                                        trackId = trackId,
                                        isPlaying = true,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    // Track Name
                                    Text(
                                        text = trackName,
                                        style = SaltTheme.textStyles.main,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = SaltTheme.colors.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Category Pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(themeColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = category.title,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = category.getContentColor(isDark),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Formatted Duration
                                Text(
                                    text = stats.getFormattedTrackDuration(trackId),
                                    style = SaltTheme.textStyles.sub,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SaltTheme.colors.text.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Proportional Progress Bar (Dedicated individual acoustic signature color)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedRatio)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(trackColor)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Reset Action Button
            if (stats.totalSeconds > 0L) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRequestReset()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "↺ 重置播放统计",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaltTheme.colors.text.copy(alpha = 0.45f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Secondary confirmation bottom sheet for resetting playback statistics safely.
 */
@Composable
fun ResetStatsConfirmBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirmReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    SaltBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BauhausUiIcon(
                        symbol = BauhausUiSymbol.Warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "重置播放统计数据",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SaltTheme.colors.text
                    )
                }

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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "重置后，累计播放总时长、伴眠安睡夜晚折算以及各音效的声音偏好排行将被全部清空并归零，该操作不可恢复。",
                style = SaltTheme.textStyles.sub,
                fontSize = 13.sp,
                color = SaltTheme.colors.text.copy(alpha = 0.70f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SaltTheme.colors.text
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirmReset()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "确认清空",
                        style = SaltTheme.textStyles.main,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
