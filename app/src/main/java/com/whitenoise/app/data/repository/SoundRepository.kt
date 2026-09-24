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
        ),
        SoundTrack(
            id = "waves",
            name = "海浪",
            subtitle = "潮汐涌动的深沉与安宁",
            assetFileName = "waves.ogg",
            volume = 0.6f
        ),
        SoundTrack(
            id = "coffee_shop",
            name = "咖啡馆",
            subtitle = "街角午后的轻语与杯碟",
            assetFileName = "coffee_shop.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "train",
            name = "列车",
            subtitle = "铁轨律动的规律催眠节奏",
            assetFileName = "train.ogg",
            volume = 0.55f
        ),
        SoundTrack(
            id = "boat",
            name = "小舟",
            subtitle = "清澈水波轻轻晃动船舷",
            assetFileName = "boat.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "pink_noise",
            name = "粉红噪",
            subtitle = "深沉厚实更助眠的慢波音",
            assetFileName = "pink_noise.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "city",
            name = "都市",
            subtitle = "遥远微茫的城市街道夜声",
            assetFileName = "city.ogg",
            volume = 0.45f
        ),
        SoundTrack(
            id = "brown_noise",
            name = "棕色噪音",
            subtitle = "深沉厚重的低频慢波瀑布声",
            assetFileName = "brown_noise.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "fan",
            name = "电风扇",
            subtitle = "恒定转动的机械微风伴眠",
            assetFileName = "fan.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "clock",
            name = "钟表",
            subtitle = "规律催眠的秒针律动",
            assetFileName = "clock.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "keyboard",
            name = "机械键盘",
            subtitle = "清脆规律的指尖敲击节奏",
            assetFileName = "keyboard.ogg",
            volume = 0.45f
        ),
        SoundTrack(
            id = "wind_chimes",
            name = "风铃",
            subtitle = "微风吹拂的空灵金属脆响",
            assetFileName = "wind_chimes.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "rain_roof",
            name = "雨打屋檐",
            subtitle = "雨水滴落窗檐与屋顶的庇护感",
            assetFileName = "rain_roof.ogg",
            volume = 0.5f
        ),
        SoundTrack(
            id = "underwater",
            name = "深海水声",
            subtitle = "深潜海底的沉浸低频与水泡",
            assetFileName = "underwater.ogg",
            volume = 0.55f
        ),
        SoundTrack(
            id = "green_noise",
            name = "绿噪",
            subtitle = "聚焦自然中频的心理学慢波",
            assetFileName = "green_noise.ogg",
            volume = 0.5f
        )
    )

    fun getTrackById(id: String): SoundTrack? = ALL_TRACKS.find { it.id == id }
}
