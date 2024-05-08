package com.yogesh.videoplayer.view

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.ActivityPlayerBinding
import com.yogesh.videoplayer.databinding.CustomExoplayerControlViewBinding
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
    private var isPipMode = false

    @Inject
    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(activityPlayerBinding.root)


        clickEvents()

    }

    private fun setPip() {
        if (!session.containsKeyOrNot(Constants.PIP_ENABLED) || !session.getBool(Constants.PIP_ENABLED)) {
            session.saveBool(Constants.PIP_ENABLED, true)
        } else {
            session.saveBool(Constants.PIP_ENABLED, false)
        }
        setPipDrawable()
    }

    private fun setPipDrawable() {
        if (session.getBool(Constants.PIP_ENABLED)) {
            activityPlayerBinding.root.findViewById<ImageView>(R.id.pipEnabled)
                .setImageResource(R.drawable.pip_on)
        } else {
            activityPlayerBinding.root.findViewById<ImageView>(R.id.pipEnabled)
                .setImageResource(R.drawable.pip_off)
        }
    }

    private fun clickEvents() {

        activityPlayerBinding.root.findViewById<LinearLayout>(R.id.pipBtn).setOnClickListener {
            setPip()
        }


    }


    override fun onResume() {
        super.onResume()
        setPip()
        additionalSetup()
        if (!isInPictureInPictureMode) {
            if (player == null) {
                initializePlayer()
            }
            hideSystemUi()
        }
    }

    private fun additionalSetup() {
        setPip()
    }

    override fun onStop() {
        super.onStop()
        if (!isInPictureInPictureMode) {
            releasePlayer()
        }
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
                val mediaItem = MediaItem.fromUri(session.getData(Constants.VIDEO_PATH).toString())
                exoPlayer.setMediaItems(
                    listOf(mediaItem),
                    mediaItemIndex,
                    playbackPosition
                )
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

    private fun enterPIPMode() {
        if (packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
        ) {
            playbackPosition = player!!.currentPosition
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            } else {
                this.enterPictureInPictureMode()
            }
        }
    }

    override fun onBackPressed() {
        if (isPipEnabled()) {
            if (packageManager
                    .hasSystemFeature(
                        PackageManager.FEATURE_PICTURE_IN_PICTURE
                    )
            ) {
                enterPIPMode()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            //user clicked on close button of PiP window
//            finishAndRemoveTask()
            releasePlayer()

        } else if (lifecycle.currentState == Lifecycle.State.STARTED) {
            if (isInPictureInPictureMode) {
                // user clicked on minimize button
            } else {
                // user clicked on maximize button of PiP window
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isPipEnabled()) {
            enterPIPMode()
        }
    }

    private fun isPipEnabled() = session.getBool(Constants.PIP_ENABLED)
}