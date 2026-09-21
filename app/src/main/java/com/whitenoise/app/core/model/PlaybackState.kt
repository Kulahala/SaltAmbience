package com.whitenoise.app.core.model

data class PlaybackState(
    val isMasterPlaying: Boolean = false,
    val masterVolume: Float = 1.0f,
    val activeTrackCount: Int = 0,
    val sleepTimerRemainingSeconds: Long? = null,
    val isSleepTimerRunning: Boolean = false,
    val sleepFadeFraction: Float = 1.0f
)
