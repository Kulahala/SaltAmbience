package com.whitenoise.app.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.whitenoise.app.MainActivity
import com.whitenoise.app.R
import com.whitenoise.app.core.audio.AudioMixerEngine
import com.whitenoise.app.core.audio.VolumeCalculator
import com.whitenoise.app.core.model.PlaybackState
import com.whitenoise.app.data.repository.SoundRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class WhiteNoiseMediaService : MediaSessionService() {

    companion object {
        private const val TAG = "WhiteNoiseMediaService"
        const val CHANNEL_ID = "whitenoise_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.whitenoise.app.action.PLAY"
        const val ACTION_PAUSE = "com.whitenoise.app.action.PAUSE"
        const val ACTION_STOP = "com.whitenoise.app.action.STOP"

        var instance: WhiteNoiseMediaService? = null
            private set

        enum class NotificationAction {
            START_FOREGROUND,
            UPDATE_PAUSED,
            REMOVE_NOTIFICATION
        }

        fun decideNotificationAction(
            hasStartedForeground: Boolean,
            isMasterPlaying: Boolean,
            activeTrackCount: Int
        ): NotificationAction {
            return if (isMasterPlaying) {
                NotificationAction.START_FOREGROUND
            } else {
                if (hasStartedForeground && activeTrackCount > 0) {
                    NotificationAction.UPDATE_PAUSED
                } else {
                    NotificationAction.REMOVE_NOTIFICATION
                }
            }
        }

        fun formatNotificationSubtext(
            isMasterPlaying: Boolean,
            activeTrackCount: Int,
            isSleepTimerRunning: Boolean = false,
            sleepTimerRemainingSeconds: Long? = null
        ): String {
            val timerSuffix = if (isSleepTimerRunning && sleepTimerRemainingSeconds != null && sleepTimerRemainingSeconds > 0) {
                val mins = VolumeCalculator.calculateRemainingMinutes(sleepTimerRemainingSeconds)
                " · ⏱️ ${mins}m后休眠"
            } else ""

            return if (isMasterPlaying) {
                if (activeTrackCount > 0) {
                    "正在混音播放 $activeTrackCount 种自然声$timerSuffix"
                } else {
                    "未选择音效$timerSuffix"
                }
            } else {
                if (activeTrackCount > 0) {
                    "已暂停 · $activeTrackCount 轨待续$timerSuffix"
                } else {
                    "已暂停$timerSuffix"
                }
            }
        }

        fun shouldUpdateNotification(old: PlaybackState, new: PlaybackState): Boolean {
            return old.isMasterPlaying != new.isMasterPlaying ||
                old.activeTrackCount != new.activeTrackCount ||
                old.primaryTrackId != new.primaryTrackId ||
                old.isSleepTimerRunning != new.isSleepTimerRunning ||
                (old.isSleepTimerRunning && new.isSleepTimerRunning &&
                    kotlin.math.abs((old.sleepTimerRemainingSeconds ?: 0L) - (new.sleepTimerRemainingSeconds ?: 0L)) >= 60L)
        }
    }

    inner class LocalBinder : Binder() {
        val service: WhiteNoiseMediaService
            get() = this@WhiteNoiseMediaService
        val engine: AudioMixerEngine
            get() = this@WhiteNoiseMediaService.audioEngine
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var audioEngine: AudioMixerEngine
        private set

    private var coordinatorExoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private var isNoisyReceiverRegistered = false
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Log.i(TAG, "Headphones disconnected (BECOMING_NOISY). Pausing playback.")
                audioEngine.setMasterPlaying(false)
            }
        }
    }

    private var hasStartedForeground = false

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "WhiteNoiseMediaService onCreate")
        instance = this
        audioEngine = AudioMixerEngine.getInstance(applicationContext)

        createNotificationChannel()
        setupMediaSession()
        registerNoisyReceiver()
        observeEngineState()

        audioEngine.onSleepTimerCompleted = {
            Log.i(TAG, "Sleep timer finished in service. Stopping foreground and service.")
            hasStartedForeground = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.cancel(NOTIFICATION_ID)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY -> audioEngine.setMasterPlaying(true)
            ACTION_PAUSE -> audioEngine.setMasterPlaying(false)
            ACTION_STOP -> {
                hasStartedForeground = false
                audioEngine.stopAll()
                stopForeground(STOP_FOREGROUND_REMOVE)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.cancel(NOTIFICATION_ID)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun setupMediaSession() {
        // Coordinator player proxies playback commands to AudioMixerEngine with low-latency load control
        val lowLatencyControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(500, 1000, 50, 100)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val basePlayer = ExoPlayer.Builder(applicationContext)
            .setLoadControl(lowLatencyControl)
            .build().apply {
            val dummyItem = MediaItem.fromUri("asset:///sounds/white_noise.ogg")
            setMediaItem(dummyItem)
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
        coordinatorExoPlayer = basePlayer

        val forwardingPlayer = object : ForwardingPlayer(basePlayer) {
            override fun play() {
                super.play()
                audioEngine.setMasterPlaying(true)
            }

            override fun pause() {
                super.pause()
                audioEngine.setMasterPlaying(false)
            }

            override fun stop() {
                super.stop()
                hasStartedForeground = false
                audioEngine.stopAll()
                stopForeground(STOP_FOREGROUND_REMOVE)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.cancel(NOTIFICATION_ID)
                stopSelf()
            }
        }

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(pendingIntent)
            .build()
    }

    private fun observeEngineState() {
        serviceScope.launch {
            audioEngine.playbackState
                .distinctUntilChanged { old, new ->
                    !shouldUpdateNotification(old, new)
                }
                .collectLatest { state ->
                    val player = coordinatorExoPlayer
                    if (player != null && player.playWhenReady != state.isMasterPlaying) {
                        player.playWhenReady = state.isMasterPlaying
                    }

                    val notificationManager = getSystemService(NotificationManager::class.java)
                    val action = decideNotificationAction(
                        hasStartedForeground = hasStartedForeground,
                        isMasterPlaying = state.isMasterPlaying,
                        activeTrackCount = state.activeTrackCount
                    )

                    when (action) {
                        NotificationAction.START_FOREGROUND -> {
                            hasStartedForeground = true
                            val notification = buildForegroundNotification(state)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                startForeground(
                                    NOTIFICATION_ID,
                                    notification,
                                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                                )
                            } else {
                                startForeground(NOTIFICATION_ID, notification)
                            }
                        }
                        NotificationAction.UPDATE_PAUSED -> {
                            stopForeground(STOP_FOREGROUND_DETACH)
                            notificationManager?.notify(NOTIFICATION_ID, buildForegroundNotification(state))
                        }
                        NotificationAction.REMOVE_NOTIFICATION -> {
                            hasStartedForeground = false
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            notificationManager?.cancel(NOTIFICATION_ID)
                        }
                    }
                }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "白噪音播放控制",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "SaltAmbience 自然白噪音后台混音服务通知"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(state: PlaybackState): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = state.isMasterPlaying
        val subtext = formatNotificationSubtext(
            isMasterPlaying = isPlaying,
            activeTrackCount = state.activeTrackCount,
            isSleepTimerRunning = state.isSleepTimerRunning,
            sleepTimerRemainingSeconds = state.sleepTimerRemainingSeconds
        )

        val activeTracks = audioEngine.tracksState.value.filter { it.isPlaying && !it.isMuted }
        val primaryTrack = activeTracks.find { it.id == state.primaryTrackId }
            ?: activeTracks.maxByOrNull { it.volume }
            ?: activeTracks.firstOrNull()

        val trackId = primaryTrack?.id ?: state.primaryTrackId
        val soundName = primaryTrack?.name ?: SoundRepository.ALL_TRACKS.find { it.id == trackId }?.name

        // Generate dynamic high-res Bauhaus acoustic artwork bitmap & bytes
        val (coverBitmap, artworkBytes) = BauhausArtworkGenerator.getOrCreateArtwork(
            context = this,
            trackId = trackId,
            soundName = soundName,
            activeCount = state.activeTrackCount,
            isPlaying = isPlaying
        )

        // Update MediaSession coordinator player metadata
        val title = (primaryTrack?.name ?: soundName)?.let { "$it · SaltAmbience" } ?: "SaltAmbience 自然混音"
        val updatedMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist("椒盐美学 · 多轨自然声")
            .setAlbumTitle("SaltAmbience 声学空间")
            .setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            .build()
        coordinatorExoPlayer?.let { player ->
            player.playlistMetadata = updatedMetadata
        }

        val playPauseIntent = Intent(this, WhiteNoiseMediaService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WhiteNoiseMediaService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "暂停" else "播放"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setLargeIcon(coverBitmap)
            .setContentTitle(title)
            .setContentText(subtext)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "全停", stopPendingIntent)

        val isTimerActive = state.isSleepTimerRunning &&
            state.sleepTimerRemainingSeconds != null &&
            state.sleepTimerRemainingSeconds > 0

        if (isTimerActive) {
            val targetTimestamp = System.currentTimeMillis() + (state.sleepTimerRemainingSeconds ?: 0L) * 1000L
            builder.setShowWhen(true)
                .setWhen(targetTimestamp)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
        } else {
            builder.setShowWhen(false)
                .setUsesChronometer(false)
        }

        mediaSession?.let { session ->
            builder.setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1)
            )
        }

        return builder.build()
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            ContextCompat.registerReceiver(
                this,
                noisyReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            isNoisyReceiverRegistered = true
        }
    }

    private fun unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(noisyReceiver)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering noisy receiver", e)
            }
            isNoisyReceiverRegistered = false
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "onTaskRemoved called")
        if (!audioEngine.playbackState.value.isMasterPlaying) {
            Log.i(TAG, "App swiped away while not playing, stopping service.")
            hasStartedForeground = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.cancel(NOTIFICATION_ID)
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "WhiteNoiseMediaService onDestroy")
        instance = null
        unregisterNoisyReceiver()
        serviceScope.cancel()

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        coordinatorExoPlayer?.release()
        coordinatorExoPlayer = null

        audioEngine.releasePlayers()
        super.onDestroy()
    }
}
