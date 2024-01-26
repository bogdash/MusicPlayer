package com.bogdash.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class MusicPlayerService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    fun play() {
        if (!::mediaPlayer.isInitialized || mediaPlayer.currentPosition == 0) {
            mediaPlayer = MediaPlayer.create(this, R.raw.josh_levi_if_the_world)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } else {
            mediaPlayer.start()
        }
    }

    fun pause() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}