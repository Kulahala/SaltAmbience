package com.whitenoise.app.core.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.collection.LruCache
import java.io.ByteArrayOutputStream

/**
 * Bauhaus acoustic art generator for lockscreen and notification media cards.
 * Generates ultra-clean, minimalist 512x512 Bauhaus acoustic artwork for lockscreen and notification center.
 * Features a basalt charcoal slate background with pure two-tone (Pure White + Sea Salt Ice Cyan)
 * acoustic geometry, completely free of cluttered text and unreadable circular rims.
 */
object BauhausArtworkGenerator {

    private const val ARTWORK_SIZE = 512
    private val cache = LruCache<String, Pair<Bitmap, ByteArray>>(20)

    fun getOrCreateArtwork(
        context: Context,
        trackId: String?,
        soundName: String? = null,
        activeCount: Int = 1,
        isPlaying: Boolean = true
    ): Pair<Bitmap, ByteArray> {
        val key = trackId ?: "ambient"
        synchronized(cache) {
            val cached = cache.get(key)
            if (cached != null) return cached
        }

        val bitmap = generateBitmap(trackId)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        val pair = Pair(bitmap, byteArray)
        synchronized(cache) {
            cache.put(key, pair)
        }
        return pair
    }

    fun clearCache() {
        synchronized(cache) {
            cache.evictAll()
        }
    }

    private fun generateBitmap(trackId: String?): Bitmap {
        val bitmap = Bitmap.createBitmap(ARTWORK_SIZE, ARTWORK_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val size = ARTWORK_SIZE.toFloat()
        val cx = size * 0.5f
        val cy = size * 0.5f

        val colors = getColorsForTrack(trackId)

        // 1. Basalt Cold Charcoal Slate Background Gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, size, size,
                intArrayOf(Color.parseColor("#171922"), Color.parseColor("#0E1015")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size, size, bgPaint)

        // Subtle outer boundary stroke
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
            color = Color.parseColor("#262933")
        }
        canvas.drawRect(1f, 1f, size - 1f, size - 1f, borderPaint)

        // 2. Radial Ambient Acoustic Glow with skeuomorphic natural color tint
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, size * 0.42f,
                intArrayOf(colors.glowColor, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, size * 0.42f, glowPaint)

        // 3. Central Pure Vector Bauhaus Geometric Acoustic Symbol
        // Scaled by 1.75x around (cx, cy) to fill the 512x512 canvas prominently and crisply
        canvas.save()
        canvas.scale(1.75f, 1.75f, cx, cy)
        drawSoundGeometry(canvas, trackId, cx, cy, colors)
        canvas.restore()

        return bitmap
    }

    data class ArtworkColors(
        val primaryColor: Int,
        val secondaryColor: Int,
        val glowColor: Int
    )

    private fun getColorsForTrack(trackId: String?): ArtworkColors {
        return when (trackId) {
            "rain" -> ArtworkColors(
                primaryColor = Color.parseColor("#E0F7FA"),
                secondaryColor = Color.parseColor("#29B6F6"),
                glowColor = Color.parseColor("#2529B6F6")
            )
            "storm" -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#FFD600"), // 闪电电光金黄
                glowColor = Color.parseColor("#25FFD600")
            )
            "wind" -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#43A047"), // 森林苍翠绿
                glowColor = Color.parseColor("#2543A047")
            )
            "stream" -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#0288D1"), // 溪流清流湛蓝
                glowColor = Color.parseColor("#250288D1")
            )
            "fireplace" -> ArtworkColors(
                primaryColor = Color.parseColor("#FF5722"), // 烈焰橙红
                secondaryColor = Color.parseColor("#FFC107"), // 火星金黄
                glowColor = Color.parseColor("#28FF5722")
            )
            "birds" -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#FFA726"), // 晨光暖橙羽
                glowColor = Color.parseColor("#25FFA726")
            )
            "summer_night" -> ArtworkColors(
                primaryColor = Color.parseColor("#FFF59D"), // 皎洁月牙金白
                secondaryColor = Color.parseColor("#7E57C2"), // 静谧夜空紫
                glowColor = Color.parseColor("#257E57C2")
            )
            "white_noise" -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#90A4AE"), // 极简频谱银灰
                glowColor = Color.parseColor("#2090A4AE")
            )
            "waves" -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#1565C0"), // 蔚蓝深海
                glowColor = Color.parseColor("#251565C0")
            )
            "coffee_shop" -> ArtworkColors(
                primaryColor = Color.parseColor("#FFF8E1"), // 奶泡暖白
                secondaryColor = Color.parseColor("#8D6E63"), // 烘焙焦糖棕
                glowColor = Color.parseColor("#258D6E63")
            )
            "train" -> ArtworkColors(
                primaryColor = Color.parseColor("#ECEFF1"), // 钢轨银白
                secondaryColor = Color.parseColor("#FF9800"), // 信号灯琥珀金
                glowColor = Color.parseColor("#25FF9800")
            )
            "boat" -> ArtworkColors(
                primaryColor = Color.parseColor("#A1887F"), // 原木暖棕
                secondaryColor = Color.parseColor("#00ACC1"), // 碧波湖水蓝
                glowColor = Color.parseColor("#2500ACC1")
            )
            "pink_noise" -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#F06292"), // 珊瑚柔粉
                glowColor = Color.parseColor("#25F06292")
            )
            "city" -> ArtworkColors(
                primaryColor = Color.parseColor("#CFD8DC"), // 建筑冷灰
                secondaryColor = Color.parseColor("#FFB300"), // 霓虹金顶
                glowColor = Color.parseColor("#25FFB300")
            )
            "brown_noise" -> ArtworkColors(
                primaryColor = Color.parseColor("#B0BEC5"), // 岩石灰
                secondaryColor = Color.parseColor("#6D4C41"), // 大地泥土暖褐
                glowColor = Color.parseColor("#286D4C41")
            )
            else -> ArtworkColors(
                primaryColor = Color.WHITE,
                secondaryColor = Color.parseColor("#19B2A6"), // 海盐冰青
                glowColor = Color.parseColor("#2419B2A6")
            )
        }
    }

    private fun drawSoundGeometry(canvas: Canvas, trackId: String?, cx: Float, cy: Float, colors: ArtworkColors) {
        val primaryStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6.0f
            color = colors.primaryColor
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val secondaryStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5.2f
            color = colors.secondaryColor
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val secondaryFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = colors.secondaryColor
        }
        val primaryFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = colors.primaryColor
        }

        when (trackId) {
            "rain" -> {
                // 3 parallel rhythmic angled rain sound vectors: Outer 2 Primary, Center Secondary
                canvas.drawLine(cx - 52f, cy - 35f, cx - 28f, cy + 45f, primaryStroke)
                canvas.drawLine(cx - 8f, cy - 50f, cx + 16f, cy + 50f, secondaryStroke)
                canvas.drawLine(cx + 36f, cy - 30f, cx + 60f, cy + 40f, primaryStroke)
                // Rain acoustic droplet node (Secondary)
                canvas.drawCircle(cx + 18f, cy + 62f, 6.5f, secondaryFill)
            }
            "storm" -> {
                // Zig-zag lightning fracture stroke (Secondary)
                val path = Path().apply {
                    moveTo(cx + 14f, cy - 62f)
                    lineTo(cx - 20f, cy - 8f)
                    lineTo(cx + 24f, cy - 8f)
                    lineTo(cx - 14f, cy + 62f)
                }
                canvas.drawPath(path, secondaryStroke)
                // Accompanying downpour line & impact dot (Primary)
                canvas.drawLine(cx - 50f, cy - 20f, cx - 36f, cy + 38f, primaryStroke)
                canvas.drawCircle(cx + 48f, cy + 32f, 7.5f, primaryFill)
            }
            "wind" -> {
                // Two aerodynamic fluid streamlines: Upper Primary, Lower Secondary
                val p1 = Path().apply {
                    moveTo(cx - 72f, cy - 18f)
                    cubicTo(cx - 25f, cy - 36f, cx + 25f, cy + 6f, cx + 72f, cy - 12f)
                }
                canvas.drawPath(p1, primaryStroke)
                val p2 = Path().apply {
                    moveTo(cx - 56f, cy + 22f)
                    cubicTo(cx - 15f, cy + 6f, cx + 25f, cy + 40f, cx + 60f, cy + 22f)
                }
                canvas.drawPath(p2, secondaryStroke)
                canvas.drawCircle(cx + 72f, cy + 20f, 5.5f, secondaryFill)
            }
            "stream" -> {
                // Twin undulating sine ripple waves: Upper Primary, Lower Secondary
                val p1 = Path().apply {
                    moveTo(cx - 72f, cy - 10f)
                    cubicTo(cx - 36f, cy - 30f, cx, cy + 10f, cx + 36f, cy - 10f)
                    cubicTo(cx + 54f, cy - 20f, cx + 64f, cy - 5f, cx + 72f, cy - 10f)
                }
                canvas.drawPath(p1, primaryStroke)
                val p2 = Path().apply {
                    moveTo(cx - 72f, cy + 22f)
                    cubicTo(cx - 36f, cy + 2f, cx, cy + 42f, cx + 36f, cy + 22f)
                    cubicTo(cx + 54f, cy + 12f, cx + 64f, cy + 27f, cx + 72f, cy + 22f)
                }
                canvas.drawPath(p2, secondaryStroke)
                // Suspended ripple droplet (Secondary)
                canvas.drawCircle(cx, cy - 42f, 6.5f, secondaryFill)
            }
            "fireplace" -> {
                // Geometric flame chevron angles: Outer chevrons Primary, Center ray Secondary
                canvas.drawLine(cx - 42f, cy + 48f, cx, cy - 38f, primaryStroke)
                canvas.drawLine(cx + 42f, cy + 48f, cx, cy - 38f, primaryStroke)
                canvas.drawLine(cx, cy + 48f, cx, cy, secondaryStroke)
                // Floating spark dots (Secondary)
                canvas.drawCircle(cx - 20f, cy - 56f, 4.5f, secondaryFill)
                canvas.drawCircle(cx + 24f, cy - 48f, 6.0f, secondaryFill)
            }
            "birds" -> {
                // Ascending gull wing arcs (Primary)
                val wingPath = Path().apply {
                    moveTo(cx - 72f, cy + 14f)
                    quadTo(cx - 36f, cy - 36f, cx, cy)
                    quadTo(cx + 36f, cy - 36f, cx + 72f, cy + 14f)
                }
                canvas.drawPath(wingPath, primaryStroke)
                // Melodic chirp vocal tone dot (Secondary)
                canvas.drawCircle(cx, cy - 45f, 6.5f, secondaryFill)
            }
            "summer_night" -> {
                // Crescent moon arc (Primary)
                val moonRect = RectF(cx + 10f, cy - 45f, cx + 65f, cy + 30f)
                canvas.drawArc(moonRect, -80f, 160f, false, primaryStroke)
                // Concentric acoustic cicada wave pulses (Secondary)
                val r1 = RectF(cx - 65f, cy - 20f, cx + 5f, cy + 50f)
                canvas.drawArc(r1, 140f, 80f, false, secondaryStroke)
                val r2 = RectF(cx - 85f, cy - 40f, cx + 25f, cy + 70f)
                canvas.drawArc(r2, 140f, 80f, false, secondaryStroke)
                canvas.drawCircle(cx - 30f, cy + 15f, 5.5f, secondaryFill)
            }
            "white_noise" -> {
                // Balanced frequency spectrum raster bars: Alternating Primary and Secondary
                canvas.drawLine(cx - 58f, cy - 45f, cx + 58f, cy - 45f, primaryStroke)
                canvas.drawLine(cx - 74f, cy - 15f, cx + 74f, cy - 15f, secondaryStroke)
                canvas.drawLine(cx - 50f, cy + 15f, cx + 50f, cy + 15f, primaryStroke)
                canvas.drawLine(cx - 68f, cy + 45f, cx + 68f, cy + 45f, secondaryStroke)
                val thinPrimary = Paint(primaryStroke).apply { strokeWidth = 2.5f }
                canvas.drawLine(cx, cy - 65f, cx, cy + 65f, thinPrimary)
            }
            "waves" -> {
                // Ocean tidal crest wave (Primary)
                val wavePath = Path().apply {
                    moveTo(cx - 70f, cy + 16f)
                    cubicTo(cx - 30f, cy + 16f, cx - 10f, cy - 46f, cx + 28f, cy - 46f)
                    cubicTo(cx + 52f, cy - 46f, cx + 62f, cy - 24f, cx + 54f, cy - 12f)
                }
                canvas.drawPath(wavePath, primaryStroke)
                // Tide baseline & dot (Secondary)
                canvas.drawLine(cx - 72f, cy + 48f, cx + 72f, cy + 48f, secondaryStroke)
                canvas.drawCircle(cx - 32f, cy + 48f, 5.5f, secondaryFill)
            }
            "coffee_shop" -> {
                // Cup basin arc and saucer (Primary)
                val cupPath = Path().apply {
                    moveTo(cx - 48f, cy + 2f)
                    lineTo(cx + 48f, cy + 2f)
                    cubicTo(cx + 48f, cy + 44f, cx - 48f, cy + 44f, cx - 48f, cy + 2f)
                }
                canvas.drawPath(cupPath, primaryStroke)
                canvas.drawLine(cx - 60f, cy + 52f, cx + 60f, cy + 52f, primaryStroke)
                // Rising steam S-curves (Secondary)
                val s1 = Path().apply {
                    moveTo(cx - 16f, cy - 8f)
                    cubicTo(cx - 24f, cy - 24f, cx - 8f, cy - 38f, cx - 16f, cy - 54f)
                }
                canvas.drawPath(s1, secondaryStroke)
                val s2 = Path().apply {
                    moveTo(cx + 16f, cy - 8f)
                    cubicTo(cx + 8f, cy - 24f, cx + 24f, cy - 38f, cx + 16f, cy - 54f)
                }
                canvas.drawPath(s2, secondaryStroke)
            }
            "train" -> {
                // Parallel rail tracks (Primary)
                canvas.drawLine(cx - 30f, cy - 60f, cx - 30f, cy + 60f, primaryStroke)
                canvas.drawLine(cx + 30f, cy - 60f, cx + 30f, cy + 60f, primaryStroke)
                // Transverse pulse ties (Secondary)
                canvas.drawLine(cx - 52f, cy - 32f, cx + 52f, cy - 32f, secondaryStroke)
                canvas.drawLine(cx - 52f, cy, cx + 52f, cy, secondaryStroke)
                canvas.drawLine(cx - 52f, cy + 32f, cx + 52f, cy + 32f, secondaryStroke)
                canvas.drawCircle(cx, cy, 6f, primaryFill)
            }
            "boat" -> {
                // Hull waterline arc (Primary)
                val hullPath = Path().apply {
                    moveTo(cx - 64f, cy + 18f)
                    cubicTo(cx - 20f, cy + 42f, cx + 20f, cy + 42f, cx + 64f, cy + 8f)
                }
                canvas.drawPath(hullPath, primaryStroke)
                // Tilted oar (Secondary)
                canvas.drawLine(cx - 28f, cy - 42f, cx + 32f, cy + 54f, secondaryStroke)
                // Water ripple (Secondary lake blue)
                canvas.drawLine(cx - 46f, cy + 54f, cx + 46f, cy + 54f, secondaryStroke)
            }
            "pink_noise" -> {
                // 1/f descending slope curve (Primary)
                val slope = Path().apply {
                    moveTo(cx - 64f, cy - 42f)
                    cubicTo(cx - 20f, cy - 10f, cx + 20f, cy + 24f, cx + 64f, cy + 42f)
                }
                canvas.drawPath(slope, primaryStroke)
                // Stepped frequency energy bars & dot (Secondary)
                canvas.drawLine(cx - 46f, cy - 12f, cx - 12f, cy - 12f, secondaryStroke)
                canvas.drawLine(cx - 16f, cy + 14f, cx + 24f, cy + 14f, secondaryStroke)
                canvas.drawLine(cx + 20f, cy + 40f, cx + 64f, cy + 40f, secondaryStroke)
                canvas.drawCircle(cx + 64f, cy + 42f, 6f, secondaryFill)
            }
            "city" -> {
                // 3 Bauhaus staggered skyline towers: Outer towers Primary, Center tower Secondary
                canvas.drawRect(cx - 54f, cy - 14f, cx - 20f, cy + 52f, primaryStroke)
                canvas.drawRect(cx - 12f, cy - 54f, cx + 22f, cy + 52f, secondaryStroke)
                canvas.drawRect(cx + 30f, cy + 6f, cx + 58f, cy + 52f, primaryStroke)
                canvas.drawLine(cx - 72f, cy + 52f, cx + 72f, cy + 52f, primaryStroke)
                canvas.drawCircle(cx + 5f, cy - 66f, 5f, secondaryFill)
            }
            "brown_noise" -> {
                // Heavyweight foundation bar (Primary)
                val heavyBar = Paint(primaryStroke).apply { strokeWidth = 11.0f }
                canvas.drawLine(cx - 68f, cy + 42f, cx + 68f, cy + 42f, heavyBar)
                // Low-frequency sine swell resting on top (Secondary)
                val deepWave = Path().apply {
                    moveTo(cx - 68f, cy - 12f)
                    cubicTo(cx - 34f, cy - 46f, cx, cy + 20f, cx + 34f, cy - 12f)
                    cubicTo(cx + 51f, cy - 28f, cx + 60f, cy - 6f, cx + 68f, cy - 12f)
                }
                canvas.drawPath(deepWave, secondaryStroke)
                canvas.drawCircle(cx, cy + 12f, 8f, secondaryFill)
            }
            else -> {
                // Headphone / Master acoustic geometry: Headband Primary, Harmonic ripple & dot Secondary
                val headphoneRect = RectF(cx - 60f, cy - 60f, cx + 60f, cy + 60f)
                canvas.drawArc(headphoneRect, 180f, 180f, false, primaryStroke)
                canvas.drawLine(cx - 60f, cy, cx - 60f, cy + 40f, primaryStroke)
                canvas.drawLine(cx + 60f, cy, cx + 60f, cy + 40f, primaryStroke)
                val rippleRect = RectF(cx - 35f, cy - 10f, cx + 35f, cy + 40f)
                canvas.drawArc(rippleRect, 30f, 120f, false, secondaryStroke)
                canvas.drawCircle(cx, cy + 15f, 6f, secondaryFill)
            }
        }
    }
}
