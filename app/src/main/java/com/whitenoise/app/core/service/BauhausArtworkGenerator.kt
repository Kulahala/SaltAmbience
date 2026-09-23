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
import android.graphics.Typeface
import androidx.collection.LruCache
import java.io.ByteArrayOutputStream
import kotlin.math.cos
import kotlin.math.sin

/**
 * Bauhaus acoustic art generator for lockscreen and notification media cards.
 * Generates dynamic, high-resolution 512x512 vinyl-style Bauhaus artwork for any sound scene.
 */
object BauhausArtworkGenerator {

    private const val ARTWORK_SIZE = 512
    private val cache = LruCache<String, Pair<Bitmap, ByteArray>>(20)

    fun getOrCreateArtwork(
        context: Context,
        trackId: String?,
        soundName: String?,
        activeCount: Int,
        isPlaying: Boolean = true
    ): Pair<Bitmap, ByteArray> {
        // Acoustic cover is tied to the sound track identity and active scene, independent of play/pause state.
        // Keeping key play/pause-independent avoids costly 512x512 PNG re-compressions on main thread during pause.
        val key = "${trackId ?: "ambient"}_${activeCount}_${soundName ?: ""}"
        synchronized(cache) {
            val cached = cache.get(key)
            if (cached != null) return cached
        }

        val bitmap = generateBitmap(trackId, soundName, activeCount)
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

    private fun generateBitmap(
        trackId: String?,
        soundName: String?,
        activeCount: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(ARTWORK_SIZE, ARTWORK_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val size = ARTWORK_SIZE.toFloat()
        val cx = size * 0.5f
        val cy = size * 0.44f

        // 1. Background Gradient (Deep basalt charcoal slate)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, size, size,
                intArrayOf(Color.parseColor("#15171F"), Color.parseColor("#0E1015")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size, size, bgPaint)

        // Subtle outer border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#262933")
        }
        canvas.drawRect(1f, 1f, size - 1f, size - 1f, borderPaint)

        // 2. Radial Ambient Glow (Sea Salt Ice Cyan 20% alpha to transparent)
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, size * 0.48f,
                intArrayOf(Color.parseColor("#2E19B2A6"), Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, size * 0.48f, glowPaint)

        // 3. Central Bauhaus Geometric Acoustic Disc
        val discRadius = size * 0.28f
        val discFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#191B24")
        }
        canvas.drawCircle(cx, cy, discRadius, discFillPaint)

        // Outer Sea Salt Ice Cyan acoustic rim
        val discRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            color = Color.parseColor("#19B2A6")
        }
        canvas.drawCircle(cx, cy, discRadius, discRimPaint)

        // Inner subtle concentric sound track ring
        val innerRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            color = Color.parseColor("#26FFFFFF")
        }
        canvas.drawCircle(cx, cy, discRadius * 0.62f, innerRimPaint)

        // Acoustic coordinate tick marks (N, S, W, E)
        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.parseColor("#8019B2A6")
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy - discRadius - 6f, cx, cy - discRadius + 8f, tickPaint)
        canvas.drawLine(cx, cy + discRadius - 8f, cx, cy + discRadius + 6f, tickPaint)
        canvas.drawLine(cx - discRadius - 6f, cy, cx - discRadius + 8f, cy, tickPaint)
        canvas.drawLine(cx + discRadius - 8f, cy, cx + discRadius + 6f, cy, tickPaint)

        // 4. Bauhaus Acoustic Vector Geometry (Sound Symbol)
        drawSoundGeometry(canvas, trackId, cx, cy)

        // 5. Typography
        // Top banner: SALT AMBIENCE · NATURAL SOUND
        val topTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8E95A5")
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.25f
        }
        canvas.drawText("SALT AMBIENCE · ACOUSTIC SPACE", cx, size * 0.11f, topTextPaint)

        // Main Title (e.g. 细雨 · 空间声学 or 3轨自然混音)
        val titleText = when {
            soundName != null && soundName.isNotBlank() -> "$soundName · 空间声学"
            activeCount > 0 -> "$activeCount 轨自然混音"
            else -> "椒盐美学 · 多轨混音"
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 25f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        canvas.drawText(titleText, cx, size * 0.84f, titlePaint)

        // Subtitle: BAUHAUS ACOUSTICS · 48kHz HI-RES
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#19B2A6")
            textSize = 12.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.16f
        }
        canvas.drawText("BAUHAUS ACOUSTICS · 48kHz HI-RES", cx, size * 0.90f, subTextPaint)

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
            strokeWidth = 5.0f
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
                // 3 parallel rhythmic angled rain sound vectors
                canvas.drawLine(cx - 52f, cy - 35f, cx - 28f, cy + 45f, whiteStroke)
                canvas.drawLine(cx - 8f, cy - 50f, cx + 16f, cy + 50f, cyanStroke)
                canvas.drawLine(cx + 36f, cy - 30f, cx + 60f, cy + 40f, whiteStroke)
                // Rain acoustic droplet node
                canvas.drawCircle(cx + 18f, cy + 62f, 6.5f, cyanFill)
            }
            "storm" -> {
                // Zig-zag lightning fracture stroke
                val path = Path().apply {
                    moveTo(cx + 14f, cy - 62f)
                    lineTo(cx - 20f, cy - 8f)
                    lineTo(cx + 24f, cy - 8f)
                    lineTo(cx - 14f, cy + 62f)
                }
                canvas.drawPath(path, cyanStroke)
                // Accompanying downpour line & impact dot
                canvas.drawLine(cx - 50f, cy - 20f, cx - 36f, cy + 38f, whiteStroke)
                canvas.drawCircle(cx + 48f, cy + 32f, 7.5f, whiteFill)
            }
            "wind" -> {
                // Two aerodynamic fluid streamlines
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
                // Twin undulating sine ripple waves
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
                canvas.drawCircle(cx, cy - 42f, 6.5f, cyanFill)
            }
            "fireplace" -> {
                // Geometric flame chevron angles
                canvas.drawLine(cx - 42f, cy + 48f, cx, cy - 38f, whiteStroke)
                canvas.drawLine(cx + 42f, cy + 48f, cx, cy - 38f, whiteStroke)
                canvas.drawLine(cx, cy + 48f, cx, cy, cyanStroke)
                // Spark constellation
                canvas.drawCircle(cx - 20f, cy - 56f, 4.5f, cyanFill)
                canvas.drawCircle(cx + 24f, cy - 48f, 6.0f, whiteFill)
            }
            "birds" -> {
                // Ascending gull wing arcs
                val wingPath = Path().apply {
                    moveTo(cx - 72f, cy + 14f)
                    quadTo(cx - 36f, cy - 36f, cx, cy)
                    quadTo(cx + 36f, cy - 36f, cx + 72f, cy + 14f)
                }
                canvas.drawPath(wingPath, whiteStroke)
                canvas.drawCircle(cx, cy - 45f, 6.5f, cyanFill)
            }
            "summer_night" -> {
                // Crescent moon arc
                val moonRect = RectF(cx + 10f, cy - 45f, cx + 65f, cy + 30f)
                canvas.drawArc(moonRect, -80f, 160f, false, whiteStroke)
                // Concentric acoustic cicada wave pulses
                val r1 = RectF(cx - 65f, cy - 20f, cx + 5f, cy + 50f)
                canvas.drawArc(r1, 140f, 80f, false, cyanStroke)
                val r2 = RectF(cx - 85f, cy - 40f, cx + 25f, cy + 70f)
                canvas.drawArc(r2, 140f, 80f, false, whiteStroke)
                canvas.drawCircle(cx - 30f, cy + 15f, 5.5f, cyanFill)
            }
            "white_noise" -> {
                // Balanced frequency spectrum raster bars
                canvas.drawLine(cx - 58f, cy - 45f, cx + 58f, cy - 45f, whiteStroke)
                canvas.drawLine(cx - 74f, cy - 15f, cx + 74f, cy - 15f, cyanStroke)
                canvas.drawLine(cx - 50f, cy + 15f, cx + 50f, cy + 15f, whiteStroke)
                canvas.drawLine(cx - 68f, cy + 45f, cx + 68f, cy + 45f, cyanStroke)
                val thinWhite = Paint(whiteStroke).apply { strokeWidth = 2.5f }
                canvas.drawLine(cx, cy - 65f, cx, cy + 65f, thinWhite)
            }
            "waves" -> {
                // Ocean tidal crest wave
                val wavePath = Path().apply {
                    moveTo(cx - 70f, cy + 16f)
                    cubicTo(cx - 30f, cy + 16f, cx - 10f, cy - 46f, cx + 28f, cy - 46f)
                    cubicTo(cx + 52f, cy - 46f, cx + 62f, cy - 24f, cx + 54f, cy - 12f)
                }
                canvas.drawPath(wavePath, whiteStroke)
                // Tide baseline
                canvas.drawLine(cx - 72f, cy + 48f, cx + 72f, cy + 48f, cyanStroke)
                canvas.drawCircle(cx - 32f, cy + 48f, 5.5f, cyanFill)
            }
            "coffee_shop" -> {
                // Cup basin arc and saucer
                val cupPath = Path().apply {
                    moveTo(cx - 48f, cy + 2f)
                    lineTo(cx + 48f, cy + 2f)
                    cubicTo(cx + 48f, cy + 44f, cx - 48f, cy + 44f, cx - 48f, cy + 2f)
                }
                canvas.drawPath(cupPath, whiteStroke)
                canvas.drawLine(cx - 60f, cy + 52f, cx + 60f, cy + 52f, cyanStroke)
                // Steam S-curves
                val s1 = Path().apply {
                    moveTo(cx - 16f, cy - 8f)
                    cubicTo(cx - 24f, cy - 24f, cx - 8f, cy - 38f, cx - 16f, cy - 54f)
                }
                canvas.drawPath(s1, cyanStroke)
                val s2 = Path().apply {
                    moveTo(cx + 16f, cy - 8f)
                    cubicTo(cx + 8f, cy - 24f, cx + 24f, cy - 38f, cx + 16f, cy - 54f)
                }
                canvas.drawPath(s2, whiteStroke)
            }
            "train" -> {
                // Parallel rail tracks
                canvas.drawLine(cx - 30f, cy - 60f, cx - 30f, cy + 60f, whiteStroke)
                canvas.drawLine(cx + 30f, cy - 60f, cx + 30f, cy + 60f, whiteStroke)
                // Transverse pulse ties
                canvas.drawLine(cx - 52f, cy - 32f, cx + 52f, cy - 32f, cyanStroke)
                canvas.drawLine(cx - 52f, cy, cx + 52f, cy, cyanStroke)
                canvas.drawLine(cx - 52f, cy + 32f, cx + 52f, cy + 32f, cyanStroke)
                canvas.drawCircle(cx, cy, 6f, whiteFill)
            }
            "boat" -> {
                // Hull waterline arc
                val hullPath = Path().apply {
                    moveTo(cx - 64f, cy + 18f)
                    cubicTo(cx - 20f, cy + 42f, cx + 20f, cy + 42f, cx + 64f, cy + 8f)
                }
                canvas.drawPath(hullPath, whiteStroke)
                canvas.drawLine(cx - 28f, cy - 42f, cx + 32f, cy + 54f, cyanStroke)
                canvas.drawLine(cx - 46f, cy + 54f, cx + 46f, cy + 54f, whiteStroke)
            }
            "pink_noise" -> {
                // 1/f descending slope curve
                val slope = Path().apply {
                    moveTo(cx - 64f, cy - 42f)
                    cubicTo(cx - 20f, cy - 10f, cx + 20f, cy + 24f, cx + 64f, cy + 42f)
                }
                canvas.drawPath(slope, whiteStroke)
                // Stepped frequency energy bars
                canvas.drawLine(cx - 46f, cy - 12f, cx - 12f, cy - 12f, cyanStroke)
                canvas.drawLine(cx - 16f, cy + 14f, cx + 24f, cy + 14f, cyanStroke)
                canvas.drawLine(cx + 20f, cy + 40f, cx + 64f, cy + 40f, cyanStroke)
                canvas.drawCircle(cx + 64f, cy + 42f, 6f, whiteFill)
            }
            "city" -> {
                // 3 Bauhaus staggered skyline towers
                canvas.drawRect(cx - 54f, cy - 14f, cx - 20f, cy + 52f, whiteStroke)
                canvas.drawRect(cx - 12f, cy - 54f, cx + 22f, cy + 52f, cyanStroke)
                canvas.drawRect(cx + 30f, cy + 6f, cx + 58f, cy + 52f, whiteStroke)
                canvas.drawLine(cx - 72f, cy + 52f, cx + 72f, cy + 52f, whiteStroke)
                canvas.drawCircle(cx + 5f, cy - 66f, 5f, cyanFill)
            }
            "brown_noise" -> {
                // Heavyweight foundation bar
                val heavyBar = Paint(whiteStroke).apply { strokeWidth = 11.0f }
                canvas.drawLine(cx - 68f, cy + 42f, cx + 68f, cy + 42f, heavyBar)
                // Low-frequency sine swell resting on top
                val deepWave = Path().apply {
                    moveTo(cx - 68f, cy - 12f)
                    cubicTo(cx - 34f, cy - 46f, cx, cy + 20f, cx + 34f, cy - 12f)
                    cubicTo(cx + 51f, cy - 28f, cx + 60f, cy - 6f, cx + 68f, cy - 12f)
                }
                canvas.drawPath(deepWave, cyanStroke)
                canvas.drawCircle(cx, cy + 12f, 8f, cyanFill)
            }
            else -> {
                // Signature Bauhaus 3-layer strings
                val s1 = Path().apply {
                    moveTo(cx - 64f, cy - 24f)
                    cubicTo(cx - 32f, cy - 44f, cx + 32f, cy - 4f, cx + 64f, cy - 24f)
                }
                canvas.drawPath(s1, whiteStroke)
                val s2 = Path().apply {
                    moveTo(cx - 64f, cy)
                    cubicTo(cx - 32f, cy + 20f, cx + 32f, cy - 20f, cx + 64f, cy)
                }
                canvas.drawPath(s2, cyanStroke)
                val s3 = Path().apply {
                    moveTo(cx - 64f, cy + 24f)
                    cubicTo(cx - 32f, cy + 4f, cx + 32f, cy + 44f, cx + 64f, cy + 24f)
                }
                canvas.drawPath(s3, whiteStroke)
                canvas.drawCircle(cx - 28f, cy - 48f, 5.5f, whiteFill)
                canvas.drawCircle(cx + 34f, cy + 48f, 4.5f, cyanFill)
            }
        }
    }
}
