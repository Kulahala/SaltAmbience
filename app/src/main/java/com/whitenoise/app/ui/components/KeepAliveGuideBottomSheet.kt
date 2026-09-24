package com.whitenoise.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

/**
 * Data model for brand specific background keep-alive steps.
 */
private data class BrandKeepAliveInfo(
    val id: String,
    val brandName: String,
    val systemName: String,
    val accentColor: Color,
    val steps: List<String>,
    val note: String? = null
)

private val BRAND_GUIDES = listOf(
    BrandKeepAliveInfo(
        id = "xiaomi",
        brandName = "小米 / Redmi",
        systemName = "澎湃 OS (HyperOS) / MIUI",
        accentColor = Color(0xFFFF6900),
        steps = listOf(
            "长按桌面的 SaltAmbience 图标，点击「应用信息」",
            "在应用信息页面中，将「省电策略」修改为【无限制】",
            "在应用信息页面中，开启「自启动」开关",
            "进入手机后台多任务卡片界面，下拉或长按 SaltAmbience 卡片，点击「小锁」图标进行锁定"
        )
    ),
    BrandKeepAliveInfo(
        id = "huawei",
        brandName = "华为 / 荣耀",
        systemName = "鸿蒙系统 (HarmonyOS) / MagicOS",
        accentColor = Color(0xFFC7000B),
        steps = listOf(
            "进入系统【设置】 -> 【应用和服务】 -> 【应用启动管理】",
            "找到 SaltAmbience，关闭右侧的【自动管理】开关",
            "在弹出的手动管理浮层中，勾选允许【允许自启动】、【允许关联启动】及【允许后台活动】",
            "进入多任务界面，将应用卡片下拉锁定"
        )
    ),
    BrandKeepAliveInfo(
        id = "oppo",
        brandName = "OPPO / 一加 / realme",
        systemName = "ColorOS",
        accentColor = Color(0xFF00875A),
        steps = listOf(
            "进入系统【设置】 -> 【电池】 -> 【应用耗电管理】",
            "找到 SaltAmbience，开启【允许完全后台行为】与【允许自启动】",
            "进入系统多任务后台，点击右上角三点或下拉卡片，选择【锁定】"
        )
    ),
    BrandKeepAliveInfo(
        id = "vivo",
        brandName = "vivo / iQOO",
        systemName = "OriginOS",
        accentColor = Color(0xFF0066FF),
        steps = listOf(
            "进入系统【设置】 -> 【电池】 -> 【后台高耗电】",
            "在列表中找到 SaltAmbience，开启允许【高耗电运行】",
            "进入【设置】 -> 【应用与权限】 -> 【权限管理】 -> 【自启动】，允许 SaltAmbience 自启动",
            "进入多任务卡片界面，下拉应用卡片锁定"
        )
    ),
    BrandKeepAliveInfo(
        id = "stock",
        brandName = "原生 Android / 三星 / Pixel",
        systemName = "OneUI / Pixel UI / AOSP",
        accentColor = Color(0xFF3DDC84),
        steps = listOf(
            "国际版与原生系统严格遵循 Android 前台媒体服务标准，已具备免杀保护",
            "如遇熄屏中断，可进入【设置】 -> 【应用程序】 -> 【SaltAmbience】 -> 【电池】，将电池使用模式设为【不受限制】"
        ),
        note = "通常无需任何额外操作即可彻夜稳定回放"
    )
)

/**
 * Minimalist Bauhaus Rotating Chevron Arrow.
 */
@Composable
private fun BauhausAccordionArrow(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = SaltTheme.colors.text.copy(alpha = 0.65f)
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f),
        label = "chevronRotation"
    )

    Canvas(
        modifier = modifier
            .size(12.dp)
            .graphicsLayer(rotationZ = rotation)
    ) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        val path = Path().apply {
            moveTo(w * 0.32f, h * 0.20f)
            lineTo(w * 0.72f, h * 0.50f)
            lineTo(w * 0.32f, h * 0.80f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Bauhaus Geometric Brand Vector Icon.
 * Uses pure points, lines, arcs, and planes to abstract each platform's design identity.
 * Strictly adheres to nominative fair use with zero trademark infringement risks.
 */
@Composable
private fun BauhausBrandIcon(
    brandId: String,
    accentColor: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    val isDark = SaltTheme.configs.isDarkTheme
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        when (brandId) {
            "xiaomi" -> {
                // Official Xiaomi Alive Squircle: Solid vibrant orange background + pure white geometric "mi"
                val squircleCorner = w * 0.28f
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(squircleCorner, squircleCorner)
                )

                // Crisp geometric "mi" cutouts in solid white
                val white = Color.White
                val lineW = 1.5.dp.toPx()
                val yTop = h * 0.28f
                val yBottom = h * 0.72f

                // Outer door frame of 'm' (Left vertical + top horizontal + right vertical)
                val mFrame = Path().apply {
                    moveTo(w * 0.26f, yBottom)
                    lineTo(w * 0.26f, yTop)
                    lineTo(w * 0.53f, yTop)
                    lineTo(w * 0.53f, yBottom)
                }
                drawPath(
                    path = mFrame,
                    color = white,
                    style = Stroke(width = lineW, cap = StrokeCap.Square, join = StrokeJoin.Miter)
                )

                // Middle hanging bar of 'm' (suspended vertically inside door frame)
                drawLine(
                    color = white,
                    start = Offset(w * 0.395f, yTop + lineW + 1.2.dp.toPx()),
                    end = Offset(w * 0.395f, yBottom),
                    strokeWidth = lineW,
                    cap = StrokeCap.Square
                )

                // Right vertical bar 'i' (strictly flush with 'm', no dot)
                drawLine(
                    color = white,
                    start = Offset(w * 0.74f, yTop),
                    end = Offset(w * 0.74f, yBottom),
                    strokeWidth = lineW,
                    cap = StrokeCap.Square
                )
            }
            "huawei" -> {
                // 8-petal blooming fan array with clear geometric aperture and balanced spacing
                val center = Offset(w * 0.50f, h * 0.78f)
                val petalAngles = floatArrayOf(-155f, -137f, -119f, -101f, -79f, -61f, -43f, -25f)
                val petalLengths = floatArrayOf(
                    h * 0.38f, h * 0.48f, h * 0.58f, h * 0.64f,
                    h * 0.64f, h * 0.58f, h * 0.48f, h * 0.38f
                )
                val startRadius = h * 0.16f
                for (i in petalAngles.indices) {
                    val rad = Math.toRadians(petalAngles[i].toDouble())
                    val len = petalLengths[i]
                    val startX = center.x + (startRadius * Math.cos(rad)).toFloat()
                    val startY = center.y + (startRadius * Math.sin(rad)).toFloat()
                    val endX = center.x + (len * Math.cos(rad)).toFloat()
                    val endY = center.y + (len * Math.sin(rad)).toFloat()

                    drawLine(
                        color = accentColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeWidth * 1.1f,
                        cap = StrokeCap.Round
                    )
                }
            }
            "oppo" -> {
                // Distinct dual geometric 'O O' (Separated by clear negative space, never infinity ∞)
                val circleW = w * 0.38f
                val circleH = h * 0.52f
                val topY = (h - circleH) / 2
                // Left 'O'
                drawOval(
                    color = accentColor,
                    topLeft = Offset(w * 0.08f, topY),
                    size = Size(circleW, circleH),
                    style = Stroke(width = strokeWidth)
                )
                // Right 'O' (clean negative space separation)
                drawOval(
                    color = accentColor,
                    topLeft = Offset(w * 0.54f, topY),
                    size = Size(circleW, circleH),
                    style = Stroke(width = strokeWidth)
                )
            }
            "vivo" -> {
                // Solid, authoritative geometric 'V' with OriginOS speed aesthetics
                val vLeft = Offset(w * 0.18f, h * 0.26f)
                val vBottom = Offset(w * 0.50f, h * 0.74f)
                val vRight = Offset(w * 0.82f, h * 0.26f)

                // Main bold V chevron
                val vPath = Path().apply {
                    moveTo(vLeft.x, vLeft.y)
                    lineTo(vBottom.x, vBottom.y)
                    lineTo(vRight.x, vRight.y)
                }
                drawPath(
                    path = vPath,
                    color = accentColor,
                    style = Stroke(
                        width = 2.4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Accent energetic highlight dot above
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = 1.3.dp.toPx(),
                    center = Offset(w * 0.50f, h * 0.36f)
                )
            }
            "stock" -> {
                // CC BY 3.0 Open Android Bot Head: Dome + Antennae + Eyes
                val domeW = w * 0.64f
                val domeH = h * 0.46f
                val domeLeft = (w - domeW) / 2
                val domeTop = h * 0.38f

                // Semi-circle dome
                drawArc(
                    color = accentColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(domeLeft, domeTop),
                    size = Size(domeW, domeH)
                )

                // Left antenna
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.34f, h * 0.38f),
                    end = Offset(w * 0.22f, h * 0.22f),
                    strokeWidth = strokeWidth * 0.9f,
                    cap = StrokeCap.Round
                )

                // Right antenna
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.66f, h * 0.38f),
                    end = Offset(w * 0.78f, h * 0.22f),
                    strokeWidth = strokeWidth * 0.9f,
                    cap = StrokeCap.Round
                )

                // Eyes
                val eyeColor = if (isDark) Color(0xFF1E2026) else Color.White
                val eyeRadius = strokeWidth * 0.65f
                drawCircle(
                    color = eyeColor,
                    radius = eyeRadius,
                    center = Offset(w * 0.37f, h * 0.50f)
                )
                drawCircle(
                    color = eyeColor,
                    radius = eyeRadius,
                    center = Offset(w * 0.63f, h * 0.50f)
                )
            }
        }
    }
}

/**
 * Bottom Sheet for Background Playback Keep-Alive Guide.
 * Displays universal rules + collapsible brand-specific accordion guides with zero copyright risks.
 */
@Composable
fun KeepAliveGuideBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val isDark = SaltTheme.configs.isDarkTheme
    val scrollState = rememberScrollState()

    // Track which brand cards are expanded (multiple can be toggled)
    var expandedBrands by remember { mutableStateOf(setOf<String>()) }

    SaltBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BauhausUiIcon(
                            symbol = BauhausUiSymbol.Warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "后台防杀与保活指南",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaltTheme.colors.text
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "国内主流系统长时回放防休眠冻结设置",
                        fontSize = 12.sp,
                        color = SaltTheme.colors.text.copy(alpha = 0.55f)
                    )
                }

                // 32dp Circle Close Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f)
                            else Color.Black.copy(alpha = 0.05f)
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BauhausUiIcon(
                        symbol = BauhausUiSymbol.Close,
                        modifier = Modifier.size(13.dp),
                        tint = SaltTheme.colors.text.copy(alpha = 0.65f)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(scrollState)
            ) {
                // Bento 1: Core Principles (通用三板斧)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SaltTheme.colors.subBackground)
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "通用核心三步 (推荐设置)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaltTheme.colors.highlight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "国内系统为极端省电，常在深夜锁屏时强行冻结后台任务。开启以下三项可彻底免疫误杀：",
                            fontSize = 12.sp,
                            color = SaltTheme.colors.text.copy(alpha = 0.70f),
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val coreRules = listOf(
                            "① 多任务加锁" to "进入手机多任务界面，长按或下拉应用卡片，点击「小锁」图标锁定",
                            "② 电池无限制" to "进入系统设置【应用管理 -> 省电策略/电池】，修改为「无限制」",
                            "③ 允许自启动" to "在系统设置中开启「自启动」与「允许后台活动」权限"
                        )

                        coreRules.forEachIndexed { index, (title, desc) ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SaltTheme.colors.text,
                                    modifier = Modifier.width(96.dp)
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = SaltTheme.colors.text.copy(alpha = 0.60f),
                                    modifier = Modifier.weight(1f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title for Specific Brands
                Text(
                    text = "各大厂商具体路径速查 (点击展开)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaltTheme.colors.text.copy(alpha = 0.50f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                // Bento 2: Brand Accordions
                BRAND_GUIDES.forEach { guide ->
                    val isExpanded = expandedBrands.contains(guide.id)

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SaltTheme.colors.subBackground)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header Clickable Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                                        expandedBrands = if (isExpanded) {
                                            expandedBrands - guide.id
                                        } else {
                                            expandedBrands + guide.id
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Animated Rotating Bauhaus Chevron
                                    BauhausAccordionArrow(
                                        expanded = isExpanded,
                                        tint = if (isExpanded) SaltTheme.colors.highlight else SaltTheme.colors.text.copy(alpha = 0.50f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Bauhaus Geometric Brand Vector Icon
                                    BauhausBrandIcon(
                                        brandId = guide.id,
                                        accentColor = guide.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = guide.brandName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SaltTheme.colors.text
                                        )
                                        Text(
                                            text = guide.systemName,
                                            fontSize = 11.sp,
                                            color = SaltTheme.colors.text.copy(alpha = 0.45f)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isExpanded) "收起" else "查看",
                                    fontSize = 11.sp,
                                    color = if (isExpanded) SaltTheme.colors.highlight else SaltTheme.colors.text.copy(alpha = 0.40f)
                                )
                            }

                            // Collapsible Body
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) + fadeIn(),
                                exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                        .padding(bottom = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(
                                                if (isDark) Color.White.copy(alpha = 0.05f)
                                                else Color.Black.copy(alpha = 0.04f)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    guide.steps.forEachIndexed { idx, step ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${idx + 1}.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = guide.accentColor,
                                                modifier = Modifier.width(18.dp)
                                            )
                                            Text(
                                                text = step,
                                                fontSize = 12.sp,
                                                color = SaltTheme.colors.text.copy(alpha = 0.75f),
                                                lineHeight = 17.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    guide.note?.let { noteText ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "注：$noteText",
                                            fontSize = 11.sp,
                                            color = SaltTheme.colors.text.copy(alpha = 0.45f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
