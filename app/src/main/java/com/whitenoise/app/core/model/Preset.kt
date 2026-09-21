package com.whitenoise.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Preset(
    val id: String,
    val name: String,
    val description: String = "",
    val trackVolumes: Map<String, Float>,
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            Preset(
                id = "deep_night_storm",
                name = "深夜暴雨",
                description = "细雨、雷鸣与林风的沉浸洗涤",
                trackVolumes = mapOf(
                    "rain" to 0.8f,
                    "storm" to 0.6f,
                    "wind" to 0.35f
                ),
                isDefault = true
            ),
            Preset(
                id = "forest_camp",
                name = "森林露营",
                description = "微风、溪流、鸟鸣与温暖的篝火",
                trackVolumes = mapOf(
                    "birds" to 0.55f,
                    "wind" to 0.4f,
                    "stream" to 0.5f,
                    "fireplace" to 0.65f
                ),
                isDefault = true
            ),
            Preset(
                id = "summer_night_breeze",
                name = "夏夜蝉鸣",
                description = "夜幕降临时的夏虫低语与清风",
                trackVolumes = mapOf(
                    "summer_night" to 0.75f,
                    "wind" to 0.35f
                ),
                isDefault = true
            ),
            Preset(
                id = "deep_focus",
                name = "深度专注",
                description = "温和纯白噪与规律雨滴，隔绝环境噪音",
                trackVolumes = mapOf(
                    "white_noise" to 0.5f,
                    "rain" to 0.4f
                ),
                isDefault = true
            )
        )
    }
}
