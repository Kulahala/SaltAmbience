package com.whitenoise.app

import com.whitenoise.app.core.model.Preset
import com.whitenoise.app.core.model.PresetShareCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetShareCodeTest {

    private val sampleNames = mapOf(
        "rain" to "细雨",
        "storm" to "雷雨",
        "wind" to "林风",
        "fireplace" to "篝火"
    )

    @Test
    fun testGenerateAndParseRoundTrip() {
        val original = Preset(
            id = "test_custom_id",
            name = "午后小憩",
            description = "温暖篝火与轻柔细雨",
            trackVolumes = mapOf("rain" to 0.75f, "fireplace" to 0.45f),
            isDefault = false
        )

        val shareText = PresetShareCode.generateShareText(original, sampleNames)
        assertTrue(shareText.contains("午后小憩"))
        assertTrue(shareText.contains("细雨 75%"))
        assertTrue(shareText.contains("篝火 45%"))
        assertTrue(shareText.contains("\$SaltAmbience#"))

        val parsed = PresetShareCode.parseShareText(shareText)
        assertNotNull(parsed)
        assertEquals("午后小憩", parsed!!.name)
        assertEquals("温暖篝火与轻柔细雨", parsed.description)
        assertEquals(0.75f, parsed.volumes["rain"]!!, 0.001f)
        assertEquals(0.45f, parsed.volumes["fireplace"]!!, 0.001f)
    }

    @Test
    fun testParseWithChatNoisesAndPastedDecorations() {
        val original = Preset(
            id = "noisy_test",
            name = "深度专注",
            description = "纯白噪音",
            trackVolumes = mapOf("white_noise" to 0.6f)
        )
        val validText = PresetShareCode.generateShareText(original)

        // Simulate WeChat forwarding header and footer
        val chatCopiedText = """
            [微信聊天记录] 张三:
            哈哈你试试这个方案，特别好睡！
            $validText
            觉得好用点个赞哈！
        """.trimIndent()

        val parsed = PresetShareCode.parseShareText(chatCopiedText)
        assertNotNull(parsed)
        assertEquals("深度专注", parsed!!.name)
        assertEquals(0.6f, parsed.volumes["white_noise"]!!, 0.001f)
    }

    @Test
    fun testParseRawJsonFallback() {
        val rawJson = """
            {"version":1,"name":"原始JSON混音","description":"测试直接贴JSON","volumes":{"rain":0.8,"wind":0.3}}
        """.trimIndent()

        val parsed = PresetShareCode.parseShareText(rawJson)
        assertNotNull(parsed)
        assertEquals("原始JSON混音", parsed!!.name)
        assertEquals(0.8f, parsed.volumes["rain"]!!, 0.001f)
        assertEquals(0.3f, parsed.volumes["wind"]!!, 0.001f)
    }

    @Test
    fun testInvalidContentReturnsNull() {
        assertNull(PresetShareCode.parseShareText(""))
        assertNull(PresetShareCode.parseShareText("   "))
        assertNull(PresetShareCode.parseShareText("今天天气真好，出去散步吧"))
        assertNull(PresetShareCode.parseShareText("\$SaltAmbience#NOT_VALID_BASE64!@#\$"))
    }
}
