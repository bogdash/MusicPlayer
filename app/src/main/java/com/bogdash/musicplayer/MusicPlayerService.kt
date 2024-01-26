package com.bogdash.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class MusicPlayerService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private var songs = mutableListOf<Int>()
    private val binder: IBinder = MusicBinder()
    private var currentIndex = 0
    var isPlaying: Boolean = false

    companion object {
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_PLAY = "PLAY"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREVIOUS = "PREVIOUS"
    }
    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onCreate() {
        super.onCreate()

        songs = mutableListOf(
            R.raw.josh_levi_if_the_world,
            R.raw.muse_pressure,
            R.raw.moon_crystals,
            R.raw.the_hunna_i_wanna_know,
            R.raw.imagine_dragons_radioactive)

        mediaPlayer = MediaPlayer.create(this,songs[currentIndex])
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun previous() {
        changeTrack((currentIndex + songs.size - 1) % songs.size)
    }

    fun next() {
        changeTrack((currentIndex + 1) % songs.size)
    }

    fun play() {
        if (!isPlaying) {
            mediaPlayer.start()
            isPlaying = true
        }
    }

    fun pause() {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
    }


    private fun changeTrack(index: Int) {
        if (index in songs.indices) {
            currentIndex = index
            mediaPlayer.stop()
            mediaPlayer = MediaPlayer.create(this, songs[currentIndex])
            mediaPlayer.start()
            isPlaying = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> next()
            ACTION_PREVIOUS -> previous()
        }

        return START_STICKY
    }



}