package com.whitenoise.app.data.repository

import com.whitenoise.app.core.model.SoundTrack

object SoundRepository {
    val ALL_TRACKS = listOf(
        SoundTrack(
            id = "rain",
            name = "细雨",
            subtitle = "连绵舒缓的窗边细雨",
            assetFileName = "rain.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "storm",
            name = "雷雨",
            subtitle = "远雷隆隆伴随骤雨",
            assetFileName = "storm.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "wind",
            name = "林风",
            subtitle = "穿越松林的自然微风",
            assetFileName = "wind.ogg",
            volume = 0.4f
        ),
        SoundTrack(
            id = "stream",
            name = "溪流",
            subtitle = "山涧清冽奔流的泉水",
            assetFileName = "stream.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "fireplace",
            name = "篝火",
            subtitle = "壁炉中柴火燃烧的噼啪声",
            assetFileName = "fireplace.ogg",
            volume = 0.6f
        ),
        SoundTrack(
            id = "birds",
            name = "鸟鸣",
            subtitle = "清晨林野间的婉转欢啼",
            assetFileName = "birds.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "summer_night",
            name = "夏夜",
            subtitle = "田园夜幕与静谧蝉鸣",
            assetFileName = "summer_night.ogg",
            volume = 0.6f
        ),
        SoundTrack(
            id = "white_noise",
            name = "白噪音",
            subtitle = "平稳柔和的全频遮噪纯音",
            assetFileName = "white_noise.ogg",
            volume = 0.5f
        )
    )

    fun getTrackById(id: String): SoundTrack? = ALL_TRACKS.find { it.id == id }
}
