package com.yogesh.videoplayer.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.ActivityPlayerBinding
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.Session
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    private lateinit var activityPlayerBinding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L

    @Inject
    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(activityPlayerBinding.root)


    }


    override fun onResume() {
        super.onResume()

        if (player == null) {
            initializePlayer()
        }
        hideSystemUi()

    }

    override fun onPause() {
        super.onPause()

        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                activityPlayerBinding.videoView.player = exoPlayer
                val mediaItem =
//                    MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
                    MediaItem.fromUri(session.getData(Constants.VIDEO_PATH).toString())
//                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }

    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, activityPlayerBinding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}