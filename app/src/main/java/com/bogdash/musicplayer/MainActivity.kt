package com.bogdash.musicplayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.support.v4.media.session.MediaSessionCompat
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bogdash.musicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var serviceBound = false
    private lateinit var musicPlayerService: MusicPlayerService
    private var intent: Intent? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var notificationId = 0

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicBinder
            musicPlayerService = binder.getService()
            serviceBound = true
            setupNotificationChannel()
            displayCustomNotification()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    companion object {
        private const val CHANNEL_ID = "music_channel_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaSession = MediaSessionCompat(this, "AudioPlayer")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Manifest.permission.POST_NOTIFICATIONS.checkPermissionGranted()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        if (intent == null) {
            intent = Intent(this, MusicPlayerService::class.java)
            bindService(intent!!, connection, Context.BIND_AUTO_CREATE)
        }

        binding.apply {

            btnNext.setOnClickListener {
                musicPlayerService.next()
                toggleButtons()
            }

            btnPrevious.setOnClickListener {
                musicPlayerService.previous()
                toggleButtons()
            }

            btnPlayPause.setOnClickListener {
                if (musicPlayerService.isPlaying) {
                    musicPlayerService.pause()
                } else {
                    musicPlayerService.play()
                }
                toggleButtons()
            }
        }
        setupNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private fun String.checkPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this@MainActivity, this) == PackageManager.PERMISSION_GRANTED
    }

    private fun toggleButtons() {
        val playPauseButton = binding.btnPlayPause
        if (musicPlayerService.isPlaying) {
            playPauseButton.setImageResource(R.drawable.pause)
        } else {
            playPauseButton.setImageResource(R.drawable.play)
        }
    }

    private fun setupNotificationChannel() {
        val channelName = "Music Player Controls"
        val channelDescription = "Controls for the music player"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, channelImportance).apply {
            description = channelDescription
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun displayCustomNotification() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent
            .getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)

        val musicPlayerServiceIntent = Intent(this, MusicPlayerService::class.java)
        val previousPendingIntent = PendingIntent
                .getService(this, 0, musicPlayerServiceIntent.apply { action = "PREVIOUS" }, PendingIntent.FLAG_IMMUTABLE)
        val playPendingIntent = PendingIntent
            .getService(this, 0, musicPlayerServiceIntent.apply { action = "PLAY" }, PendingIntent.FLAG_IMMUTABLE)
        val pausePendingIntent = PendingIntent
            .getService(this, 0, musicPlayerServiceIntent.apply { action = "PAUSE" }, PendingIntent.FLAG_IMMUTABLE)
        val nextPendingIntent = PendingIntent
            .getService(this, 0, musicPlayerServiceIntent.apply { action = "NEXT" }, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.music)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.imagine_dragons_evolve))
            .addAction(R.drawable.skip_back, "Previous", previousPendingIntent)
            .addAction(R.drawable.play, "Play", playPendingIntent)
            .addAction(R.drawable.pause, "Pause", pausePendingIntent)
            .addAction(R.drawable.skip_fwd, "Next", nextPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainActivityPendingIntent)
            .setOnlyAlertOnce(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId++, builder)
    }
}