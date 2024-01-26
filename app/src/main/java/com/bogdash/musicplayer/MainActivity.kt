package com.bogdash.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.bogdash.musicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var serviceBound = false
    private lateinit var musicPlayerService: MusicPlayerService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        binding.apply {
            btnPlayPause.setOnClickListener {
                if (serviceBound) {
                    if (musicPlayerService.isPlaying()) {
                        musicPlayerService.pause()
                        btnPlayPause.setImageResource(R.drawable.play)
                    } else {
                        musicPlayerService.play()
                        btnPlayPause.setImageResource(R.drawable.pause)
                    }
                }
            }
        }

    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            val binder = service as MusicPlayerService.LocalBinder
            musicPlayerService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }

    }
}