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

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicBinder
            musicPlayerService = binder.getService()
            serviceBound = true
            createNotificationChannel()
            showNotification()
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
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(CHANNEL_ID, "Music Controller", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val previousIntent = Intent(this, MusicPlayerService::class.java).setAction("PREVIOUS")
        val previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(this, MusicPlayerService::class.java).setAction("PLAY")
        val playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, MusicPlayerService::class.java).setAction("PAUSE")
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, MusicPlayerService::class.java).setAction("NEXT")
        val nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.imagine_dragons_evolve)
            .addAction(R.drawable.skip_back, "Previous", previousPendingIntent)
            .addAction(R.drawable.play, "Play", playPendingIntent)
            .addAction(R.drawable.pause, "Pause", pausePendingIntent)
            .addAction(R.drawable.skip_fwd, "Next", nextPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .build()
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }

    private fun String.checkPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this@MainActivity, this) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private fun toggleButtons() {
        val playPauseButton = binding.btnPlayPause
        if (musicPlayerService.isPlaying) {
            playPauseButton.setImageResource(R.drawable.pause)
        } else {
            playPauseButton.setImageResource(R.drawable.play)
        }
    }
}