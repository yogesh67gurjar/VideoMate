package com.yogesh.videoplayer.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player.Listener
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.ActivityPlayerBinding
import com.yogesh.videoplayer.databinding.BsPlaybackSpeedBinding
import com.yogesh.videoplayer.databinding.CustomExoplayerControlViewBinding
import com.yogesh.videoplayer.model.VideoResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.Session
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    private lateinit var activityPlayerBinding: ActivityPlayerBinding
    private lateinit var bsPlaybackSpeedBinding: BsPlaybackSpeedBinding
    private var videosList: MutableList<VideoResponse> = mutableListOf()
    private val mediaItems: MutableList<MediaItem> = mutableListOf()

    private var videoIndex: Int = -1
    private lateinit var bundle: Bundle
    private var currentSpeed: Int = 0
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var playbackPosition = 0L

    @Inject
    lateinit var session: Session

    private lateinit var playBackSpeedBottomSheet: BottomSheetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(activityPlayerBinding.root)

        bundle = intent.extras!!
        videosList = bundle.getSerializable(Constants.ALL_VIDEOS) as MutableList<VideoResponse>
        videoIndex = bundle.getInt(Constants.VIDEO_INDEX, 0)

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

        activityPlayerBinding.root.findViewById<LinearLayout>(R.id.rotateBtn).setOnClickListener {
            requestedOrientation =
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
        }

        activityPlayerBinding.root.findViewById<LinearLayout>(R.id.playbackSpeedBtn)
            .setOnClickListener {
                playBackSpeedBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
                bsPlaybackSpeedBinding =
                    BsPlaybackSpeedBinding.inflate(playBackSpeedBottomSheet.layoutInflater)
                playBackSpeedBottomSheet.setContentView(bsPlaybackSpeedBinding.root)

                currentSpeed = (player!!.playbackParameters.speed * 100).toInt()


                bsPlaybackSpeedBinding.reset.setOnClickListener {
                    currentSpeed = 100
                    player!!.playbackParameters = PlaybackParameters(currentSpeed / 100.0f)
                    bsPlaybackSpeedBinding.speed.text = String.format("%.2f", currentSpeed / 100.0)
                    activityPlayerBinding.root.findViewById<TextView>(R.id.speed).text =
                        "1x"

                    setTintOfIcons()
                }
                bsPlaybackSpeedBinding.plus.setOnClickListener {
                    if (currentSpeed < 250) {
                        currentSpeed += 5
                        player!!.playbackParameters = PlaybackParameters(currentSpeed / 100.0f)
                    }
                    setTintOfIcons()
                    bsPlaybackSpeedBinding.speed.text = String.format("%.2f", currentSpeed / 100.0)
                    if (currentSpeed / 100.0f == 1.00f) {
                        activityPlayerBinding.root.findViewById<TextView>(R.id.speed).text =
                            "1x"
                    } else {
                        activityPlayerBinding.root.findViewById<TextView>(R.id.speed).text =
                            String.format("%.2f", currentSpeed / 100.0) + "x"
                    }

                }

                bsPlaybackSpeedBinding.minus.setOnClickListener {
                    if (currentSpeed > 25) {
                        currentSpeed -= 5
                        player!!.playbackParameters = PlaybackParameters(currentSpeed / 100.0f)
                    }
                    setTintOfIcons()
                    bsPlaybackSpeedBinding.speed.text = String.format("%.2f", currentSpeed / 100.0)
                    if (currentSpeed / 100.0f == 1.00f) {
                        activityPlayerBinding.root.findViewById<TextView>(R.id.speed).text =
                            "1x"
                    } else {
                        activityPlayerBinding.root.findViewById<TextView>(R.id.speed).text =
                            String.format("%.2f", currentSpeed / 100.0) + "x"
                    }
                }

                bsPlaybackSpeedBinding.speed.text = String.format("%.2f", currentSpeed / 100.0)

                val bottomSheetBehavior = playBackSpeedBottomSheet.behavior
                bottomSheetBehavior.peekHeight = resources.displayMetrics.heightPixels

                playBackSpeedBottomSheet.show()
            }
    }

    private fun setTintOfIcons() {

        if (currentSpeed in 26..249) {
            bsPlaybackSpeedBinding.minus.setColorFilter(
                ContextCompat.getColor(
                    this@PlayerActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            bsPlaybackSpeedBinding.plus.setColorFilter(
                ContextCompat.getColor(
                    this@PlayerActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
        if (currentSpeed <= 25) {

            bsPlaybackSpeedBinding.minus.setColorFilter(
                ContextCompat.getColor(
                    this@PlayerActivity,
                    R.color.red
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            bsPlaybackSpeedBinding.plus.setColorFilter(
                ContextCompat.getColor(
                    this@PlayerActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else if (currentSpeed >= 250) {
            bsPlaybackSpeedBinding.minus.setColorFilter(
                ContextCompat.getColor(
                    this@PlayerActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            bsPlaybackSpeedBinding.plus.setColorFilter(
                ContextCompat.getColor(
                    this@PlayerActivity,
                    R.color.red
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
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
            videoIndex = exoPlayer.currentMediaItemIndex
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

                for (video in videosList) {
                    mediaItems.add(MediaItem.fromUri(video.path))
                }

                player?.setMediaItems(mediaItems, videoIndex, playbackPosition)

                exoPlayer.setMediaItems(
                    mediaItems,
                    videoIndex,
                    playbackPosition
                )
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }

        player!!.addListener(object : Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                determineAndSetOrientation(videoSize.width, videoSize.height)
            }
        })
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

    private fun determineAndSetOrientation(width: Int, height: Int) {
        val aspectRatio = width.toFloat() / height
        requestedOrientation = if (aspectRatio > 1.0f) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}