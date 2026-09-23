package com.whitenoise.app.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.util.Base64

@Serializable
data class PresetSharePayload(
    val version: Int = 1,
    val name: String,
    val description: String = "",
    val volumes: Map<String, Float>
)

object PresetShareCode {

    private const val PREFIX = "\$SaltAmbience#"
    private const val SUFFIX = "\$"
    private val REGEX = Regex("""\${'$'}SaltAmbience#([A-Za-z0-9+/=_-]+)\${'$'}""")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    /**
     * Generate an elegant, human-readable share text containing encoded payload.
     */
    fun generateShareText(preset: Preset, soundNames: Map<String, String> = emptyMap()): String {
        val sanitizedVolumes = preset.trackVolumes
            .filter { it.value > 0f }
            .mapValues { (it.value).coerceIn(0f, 1f) }

        val payload = PresetSharePayload(
            version = 1,
            name = preset.name.trim(),
            description = preset.description.trim(),
            volumes = sanitizedVolumes
        )

        val jsonString = json.encodeToString(payload)
        val base64Code = Base64.getEncoder().encodeToString(jsonString.toByteArray(StandardCharsets.UTF_8))

        // Build human-readable sound list
        val soundsSummary = sanitizedVolumes.entries.joinToString(" · ") { (id, vol) ->
            val name = soundNames[id] ?: id
            val percent = (vol * 100).toInt()
            "$name $percent%"
        }.ifEmpty { "静音混音" }

        return buildString {
            appendLine("🎧 来自 SaltAmbience 的混音方案【${preset.name.trim()}】")
            if (preset.description.isNotBlank()) {
                appendLine("「${preset.description.trim()}」")
            }
            appendLine("音效组合：$soundsSummary")
            appendLine("----------------")
            appendLine("复制整段文字，打开 SaltAmbience 即可一键载入：")
            append("$PREFIX$base64Code$SUFFIX")
        }
    }

    /**
     * Parse human-readable or direct text to extract PresetSharePayload.
     * Supports $SaltAmbience#<Base64>$, direct Base64, or direct JSON.
     */
    fun parseShareText(text: String): PresetSharePayload? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // 1. Try regex extraction from standard token format: $SaltAmbience#<Base64>$
        val matchResult = REGEX.find(trimmed)
        if (matchResult != null) {
            val base64Str = matchResult.groupValues[1]
            decodeBase64Payload(base64Str)?.let { return it }
        }

        // 2. Try direct Base64 decode (in case user pasted raw base64)
        decodeBase64Payload(trimmed)?.let { return it }

        // 3. Fallback: try direct JSON parse (if user pasted raw JSON payload or Preset JSON)
        return tryParseDirectJson(trimmed)
    }

    private fun decodeBase64Payload(base64Str: String): PresetSharePayload? {
        return try {
            val decodedBytes = Base64.getDecoder().decode(base64Str.trim())
            val jsonStr = String(decodedBytes, StandardCharsets.UTF_8)
            val payload = json.decodeFromString<PresetSharePayload>(jsonStr)
            sanitizePayload(payload)
        } catch (e: Exception) {
            null
        }
    }

    private fun tryParseDirectJson(text: String): PresetSharePayload? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start in 0 until end) {
            val jsonCandidate = text.substring(start, end + 1)
            try {
                val payload = json.decodeFromString<PresetSharePayload>(jsonCandidate)
                return sanitizePayload(payload)
            } catch (e: Exception) {
                // If it was raw Preset json
                try {
                    val preset = json.decodeFromString<Preset>(jsonCandidate)
                    return sanitizePayload(
                        PresetSharePayload(
                            version = 1,
                            name = preset.name,
                            description = preset.description,
                            volumes = preset.trackVolumes
                        )
                    )
                } catch (e2: Exception) {
                    // Ignore
                }
            }
        }
        return null
    }

    private fun sanitizePayload(payload: PresetSharePayload): PresetSharePayload {
        val sanitizedName = payload.name.trim().ifEmpty { "导入的混音方案" }
        val sanitizedVolumes = payload.volumes
            .filter { it.value > 0f }
            .mapValues { it.value.coerceIn(0f, 1f) }

        return payload.copy(
            name = sanitizedName,
            description = payload.description.trim(),
            volumes = sanitizedVolumes
        )
    }
}
