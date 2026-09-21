package com.whitenoise.app.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.whitenoise.app.MainActivity
import com.whitenoise.app.core.audio.AudioMixerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WhiteNoiseMediaService : MediaSessionService() {

    companion object {
        private const val TAG = "WhiteNoiseMediaService"
        const val CHANNEL_ID = "whitenoise_playback_channel"
        const val NOTIFICATION_ID = 1001

        // Singleton reference for in-process binding / communication
        var instance: WhiteNoiseMediaService? = null
            private set
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

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "WhiteNoiseMediaService onCreate")
        instance = this
        audioEngine = AudioMixerEngine(applicationContext)

        createNotificationChannel()
        setupMediaSession()
        registerNoisyReceiver()
        observeEngineState()

        audioEngine.onSleepTimerCompleted = {
            Log.i(TAG, "Sleep timer finished in service. Stopping foreground and service.")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun setupMediaSession() {
        // Coordinator player proxies playback commands to AudioMixerEngine
        val basePlayer = ExoPlayer.Builder(applicationContext).build().apply {
            // Keep a virtual dummy item with metadata so system notification displays title and controls
            val metadata = MediaMetadata.Builder()
                .setTitle("SaltAmbience 自然混音")
                .setArtist("SaltAmbience")
                .build()
            val dummyItem = MediaItem.Builder()
                .setMediaId("master_stream")
                .setMediaMetadata(metadata)
                .build()
            setMediaItem(dummyItem)
            repeatMode = Player.REPEAT_MODE_ONE
        }
        coordinatorExoPlayer = basePlayer

        val forwardingPlayer = object : ForwardingPlayer(basePlayer) {
            override fun play() {
                audioEngine.setMasterPlaying(true)
            }

            override fun pause() {
                audioEngine.setMasterPlaying(false)
            }

            override fun stop() {
                audioEngine.stopAll()
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
            audioEngine.playbackState.collectLatest { state ->
                coordinatorExoPlayer?.playWhenReady = state.isMasterPlaying
                if (state.isMasterPlaying) {
                    startForeground(NOTIFICATION_ID, buildForegroundNotification(state.activeTrackCount))
                } else {
                    // Update notification or allow dismiss if stopped
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.notify(NOTIFICATION_ID, buildForegroundNotification(state.activeTrackCount))
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

    private fun buildForegroundNotification(activeCount: Int): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtext = if (activeCount > 0) {
            "正在混音播放 $activeCount 种自然声"
        } else {
            "已暂停"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("SaltAmbience")
            .setContentText(subtext)
            .setContentIntent(contentIntent)
            .setOngoing(audioEngine.playbackState.value.isMasterPlaying)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(noisyReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(noisyReceiver, filter)
            }
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

        audioEngine.release()
        super.onDestroy()
    }
}
