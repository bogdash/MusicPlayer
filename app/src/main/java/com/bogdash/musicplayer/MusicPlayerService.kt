package com.bogdash.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class MusicPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayers = mutableListOf<MediaPlayer>()
    private val binder = LocalBinder()
    private var currentIndex = 0

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayers.add(MediaPlayer.create(this, R.raw.josh_levi_if_the_world))
        mediaPlayers.add(MediaPlayer.create(this, R.raw.moon_crystals))
        mediaPlayers.add(MediaPlayer.create(this, R.raw.imagine_dragons_radioactive))
        mediaPlayers.add(MediaPlayer.create(this, R.raw.muse_pressure))
        mediaPlayers.add(MediaPlayer.create(this, R.raw.the_hunna_i_wanna_know))
    }

    fun play() {
        if (mediaPlayers.isEmpty()) {
            return
        }
        if (currentIndex >= mediaPlayers.size) {
            currentIndex = 0
        }
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.reset()
        }
        mediaPlayer = mediaPlayers[currentIndex]
        if ((mediaPlayer?.duration ?: 0) > 0) {
            mediaPlayer?.start()
        } else {
            mediaPlayer?.setOnPreparedListener {
                it.start()
            }
            mediaPlayer?.prepareAsync()
        }
    }

    fun pause() {
        if (currentIndex < mediaPlayers.size) {
            val mediaPlayer = mediaPlayers[currentIndex]
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }

    fun isPlaying(): Boolean {
        return currentIndex < mediaPlayers.size && mediaPlayers[currentIndex].isPlaying
    }

    fun next() {
        currentIndex++
        if (currentIndex >= mediaPlayers.size) {
            currentIndex = 0
        }
        play()
    }

    fun previous() {
        currentIndex--
        if (currentIndex < 0) {
            currentIndex = mediaPlayers.size - 1
        }
        play()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayers.forEach { it.stop() }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}