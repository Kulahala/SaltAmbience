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

        // 2. Radial Ambient Acoustic Glow (Sea Salt Ice Cyan 15% alpha to transparent)
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, size * 0.42f,
                intArrayOf(Color.parseColor("#2419B2A6"), Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, size * 0.42f, glowPaint)

        // 3. Central Pure Vector Bauhaus Geometric Acoustic Symbol
        // Scaled by 1.75x around (cx, cy) to fill the 512x512 canvas prominently and crisply
        canvas.save()
        canvas.scale(1.75f, 1.75f, cx, cy)
        drawSoundGeometry(canvas, trackId, cx, cy)
        canvas.restore()

        return bitmap
    }

    private fun drawSoundGeometry(canvas: Canvas, trackId: String?, cx: Float, cy: Float) {
        val whiteStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6.0f
            color = Color.WHITE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val cyanStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5.2f
            color = Color.parseColor("#19B2A6")
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val cyanFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#19B2A6")
        }
        val whiteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        when (trackId) {
            "rain" -> {
                // 3 parallel rhythmic angled rain sound vectors: Outer 2 White, Center Cyan
                canvas.drawLine(cx - 52f, cy - 35f, cx - 28f, cy + 45f, whiteStroke)
                canvas.drawLine(cx - 8f, cy - 50f, cx + 16f, cy + 50f, cyanStroke)
                canvas.drawLine(cx + 36f, cy - 30f, cx + 60f, cy + 40f, whiteStroke)
                // Rain acoustic droplet node (Cyan)
                canvas.drawCircle(cx + 18f, cy + 62f, 6.5f, cyanFill)
            }
            "storm" -> {
                // Zig-zag lightning fracture stroke (Cyan)
                val path = Path().apply {
                    moveTo(cx + 14f, cy - 62f)
                    lineTo(cx - 20f, cy - 8f)
                    lineTo(cx + 24f, cy - 8f)
                    lineTo(cx - 14f, cy + 62f)
                }
                canvas.drawPath(path, cyanStroke)
                // Accompanying downpour line & impact dot (White)
                canvas.drawLine(cx - 50f, cy - 20f, cx - 36f, cy + 38f, whiteStroke)
                canvas.drawCircle(cx + 48f, cy + 32f, 7.5f, whiteFill)
            }
            "wind" -> {
                // Two aerodynamic fluid streamlines: Upper White, Lower Cyan
                val p1 = Path().apply {
                    moveTo(cx - 72f, cy - 18f)
                    cubicTo(cx - 25f, cy - 36f, cx + 25f, cy + 6f, cx + 72f, cy - 12f)
                }
                canvas.drawPath(p1, whiteStroke)
                val p2 = Path().apply {
                    moveTo(cx - 56f, cy + 22f)
                    cubicTo(cx - 15f, cy + 6f, cx + 25f, cy + 40f, cx + 60f, cy + 22f)
                }
                canvas.drawPath(p2, cyanStroke)
                canvas.drawCircle(cx + 72f, cy + 20f, 5.5f, cyanFill)
            }
            "stream" -> {
                // Twin undulating sine ripple waves: Upper White (上白), Lower Cyan (下蓝)
                val p1 = Path().apply {
                    moveTo(cx - 72f, cy - 10f)
                    cubicTo(cx - 36f, cy - 30f, cx, cy + 10f, cx + 36f, cy - 10f)
                    cubicTo(cx + 54f, cy - 20f, cx + 64f, cy - 5f, cx + 72f, cy - 10f)
                }
                canvas.drawPath(p1, whiteStroke)
                val p2 = Path().apply {
                    moveTo(cx - 72f, cy + 22f)
                    cubicTo(cx - 36f, cy + 2f, cx, cy + 42f, cx + 36f, cy + 22f)
                    cubicTo(cx + 54f, cy + 12f, cx + 64f, cy + 27f, cx + 72f, cy + 22f)
                }
                canvas.drawPath(p2, cyanStroke)
                // Suspended ripple droplet (Cyan)
                canvas.drawCircle(cx, cy - 42f, 6.5f, cyanFill)
            }
            "fireplace" -> {
                // Geometric flame chevron angles: Outer chevrons White, Center ray Cyan
                canvas.drawLine(cx - 42f, cy + 48f, cx, cy - 38f, whiteStroke)
                canvas.drawLine(cx + 42f, cy + 48f, cx, cy - 38f, whiteStroke)
                canvas.drawLine(cx, cy + 48f, cx, cy, cyanStroke)
                // Floating spark dots (Cyan)
                canvas.drawCircle(cx - 20f, cy - 56f, 4.5f, cyanFill)
                canvas.drawCircle(cx + 24f, cy - 48f, 6.0f, cyanFill)
            }
            "birds" -> {
                // Ascending gull wing arcs (White)
                val wingPath = Path().apply {
                    moveTo(cx - 72f, cy + 14f)
                    quadTo(cx - 36f, cy - 36f, cx, cy)
                    quadTo(cx + 36f, cy - 36f, cx + 72f, cy + 14f)
                }
                canvas.drawPath(wingPath, whiteStroke)
                // Melodic chirp vocal tone dot (Cyan)
                canvas.drawCircle(cx, cy - 45f, 6.5f, cyanFill)
            }
            "summer_night" -> {
                // Crescent moon arc (White)
                val moonRect = RectF(cx + 10f, cy - 45f, cx + 65f, cy + 30f)
                canvas.drawArc(moonRect, -80f, 160f, false, whiteStroke)
                // Concentric acoustic cicada wave pulses (Cyan)
                val r1 = RectF(cx - 65f, cy - 20f, cx + 5f, cy + 50f)
                canvas.drawArc(r1, 140f, 80f, false, cyanStroke)
                val r2 = RectF(cx - 85f, cy - 40f, cx + 25f, cy + 70f)
                canvas.drawArc(r2, 140f, 80f, false, cyanStroke)
                canvas.drawCircle(cx - 30f, cy + 15f, 5.5f, cyanFill)
            }
            "white_noise" -> {
                // Balanced frequency spectrum raster bars: Alternating White and Cyan
                canvas.drawLine(cx - 58f, cy - 45f, cx + 58f, cy - 45f, whiteStroke)
                canvas.drawLine(cx - 74f, cy - 15f, cx + 74f, cy - 15f, cyanStroke)
                canvas.drawLine(cx - 50f, cy + 15f, cx + 50f, cy + 15f, whiteStroke)
                canvas.drawLine(cx - 68f, cy + 45f, cx + 68f, cy + 45f, cyanStroke)
                val thinWhite = Paint(whiteStroke).apply { strokeWidth = 2.5f }
                canvas.drawLine(cx, cy - 65f, cx, cy + 65f, thinWhite)
            }
            "waves" -> {
                // Ocean tidal crest wave (White)
                val wavePath = Path().apply {
                    moveTo(cx - 70f, cy + 16f)
                    cubicTo(cx - 30f, cy + 16f, cx - 10f, cy - 46f, cx + 28f, cy - 46f)
                    cubicTo(cx + 52f, cy - 46f, cx + 62f, cy - 24f, cx + 54f, cy - 12f)
                }
                canvas.drawPath(wavePath, whiteStroke)
                // Tide baseline & dot (Cyan)
                canvas.drawLine(cx - 72f, cy + 48f, cx + 72f, cy + 48f, cyanStroke)
                canvas.drawCircle(cx - 32f, cy + 48f, 5.5f, cyanFill)
            }
            "coffee_shop" -> {
                // Cup basin arc and saucer (White)
                val cupPath = Path().apply {
                    moveTo(cx - 48f, cy + 2f)
                    lineTo(cx + 48f, cy + 2f)
                    cubicTo(cx + 48f, cy + 44f, cx - 48f, cy + 44f, cx - 48f, cy + 2f)
                }
                canvas.drawPath(cupPath, whiteStroke)
                canvas.drawLine(cx - 60f, cy + 52f, cx + 60f, cy + 52f, whiteStroke)
                // Rising steam S-curves (Cyan)
                val s1 = Path().apply {
                    moveTo(cx - 16f, cy - 8f)
                    cubicTo(cx - 24f, cy - 24f, cx - 8f, cy - 38f, cx - 16f, cy - 54f)
                }
                canvas.drawPath(s1, cyanStroke)
                val s2 = Path().apply {
                    moveTo(cx + 16f, cy - 8f)
                    cubicTo(cx + 8f, cy - 24f, cx + 24f, cy - 38f, cx + 16f, cy - 54f)
                }
                canvas.drawPath(s2, cyanStroke)
            }
            "train" -> {
                // Parallel rail tracks (White)
                canvas.drawLine(cx - 30f, cy - 60f, cx - 30f, cy + 60f, whiteStroke)
                canvas.drawLine(cx + 30f, cy - 60f, cx + 30f, cy + 60f, whiteStroke)
                // Transverse pulse ties (Cyan)
                canvas.drawLine(cx - 52f, cy - 32f, cx + 52f, cy - 32f, cyanStroke)
                canvas.drawLine(cx - 52f, cy, cx + 52f, cy, cyanStroke)
                canvas.drawLine(cx - 52f, cy + 32f, cx + 52f, cy + 32f, cyanStroke)
                canvas.drawCircle(cx, cy, 6f, whiteFill)
            }
            "boat" -> {
                // Hull waterline arc (White)
                val hullPath = Path().apply {
                    moveTo(cx - 64f, cy + 18f)
                    cubicTo(cx - 20f, cy + 42f, cx + 20f, cy + 42f, cx + 64f, cy + 8f)
                }
                canvas.drawPath(hullPath, whiteStroke)
                // Tilted oar (Cyan)
                canvas.drawLine(cx - 28f, cy - 42f, cx + 32f, cy + 54f, cyanStroke)
                // Water ripple (White)
                canvas.drawLine(cx - 46f, cy + 54f, cx + 46f, cy + 54f, whiteStroke)
            }
            "pink_noise" -> {
                // 1/f descending slope curve (White)
                val slope = Path().apply {
                    moveTo(cx - 64f, cy - 42f)
                    cubicTo(cx - 20f, cy - 10f, cx + 20f, cy + 24f, cx + 64f, cy + 42f)
                }
                canvas.drawPath(slope, whiteStroke)
                // Stepped frequency energy bars & dot (Cyan)
                canvas.drawLine(cx - 46f, cy - 12f, cx - 12f, cy - 12f, cyanStroke)
                canvas.drawLine(cx - 16f, cy + 14f, cx + 24f, cy + 14f, cyanStroke)
                canvas.drawLine(cx + 20f, cy + 40f, cx + 64f, cy + 40f, cyanStroke)
                canvas.drawCircle(cx + 64f, cy + 42f, 6f, cyanFill)
            }
            "city" -> {
                // 3 Bauhaus staggered skyline towers: Outer towers White, Center tower Cyan
                canvas.drawRect(cx - 54f, cy - 14f, cx - 20f, cy + 52f, whiteStroke)
                canvas.drawRect(cx - 12f, cy - 54f, cx + 22f, cy + 52f, cyanStroke)
                canvas.drawRect(cx + 30f, cy + 6f, cx + 58f, cy + 52f, whiteStroke)
                canvas.drawLine(cx - 72f, cy + 52f, cx + 72f, cy + 52f, whiteStroke)
                canvas.drawCircle(cx + 5f, cy - 66f, 5f, cyanFill)
            }
            "brown_noise" -> {
                // Heavyweight foundation bar (White)
                val heavyBar = Paint(whiteStroke).apply { strokeWidth = 11.0f }
                canvas.drawLine(cx - 68f, cy + 42f, cx + 68f, cy + 42f, heavyBar)
                // Low-frequency sine swell resting on top (Cyan)
                val deepWave = Path().apply {
                    moveTo(cx - 68f, cy - 12f)
                    cubicTo(cx - 34f, cy - 46f, cx, cy + 20f, cx + 34f, cy - 12f)
                    cubicTo(cx + 51f, cy - 28f, cx + 60f, cy - 6f, cx + 68f, cy - 12f)
                }
                canvas.drawPath(deepWave, cyanStroke)
                canvas.drawCircle(cx, cy + 12f, 8f, cyanFill)
            }
            else -> {
                // Headphone / Master acoustic geometry: Headband White, Harmonic ripple & dot Cyan
                val headphoneRect = RectF(cx - 60f, cy - 60f, cx + 60f, cy + 60f)
                canvas.drawArc(headphoneRect, 180f, 180f, false, whiteStroke)
                canvas.drawLine(cx - 60f, cy, cx - 60f, cy + 40f, whiteStroke)
                canvas.drawLine(cx + 60f, cy, cx + 60f, cy + 40f, whiteStroke)
                val rippleRect = RectF(cx - 35f, cy - 10f, cx + 35f, cy + 40f)
                canvas.drawArc(rippleRect, 30f, 120f, false, cyanStroke)
                canvas.drawCircle(cx, cy + 15f, 6f, cyanFill)
            }
        }
    }
}
