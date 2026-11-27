package com.resumeplayer.app.playback

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Foreground media service that owns the single ExoPlayer instance.
 *
 * Because playback lives here (not in the Activity), it keeps running when the
 * screen turns off or the app is backgrounded. WAKE_MODE_LOCAL holds a partial
 * wake lock while playing so the CPU doesn't sleep mid-track, and the service's
 * media notification satisfies Android's foreground-service requirement and
 * provides lock-screen controls.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            // Route audio focus correctly and pause when headphones are unplugged.
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                // Keep the CPU awake while playing so screen-off audio never stalls.
                setWakeMode(C.WAKE_MODE_LOCAL)
            }
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    // If the user swipes the app away and nothing is playing, stop the service.
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || (!player.playWhenReady || player.mediaItemCount == 0)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
