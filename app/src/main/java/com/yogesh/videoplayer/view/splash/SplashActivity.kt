package com.yogesh.videoplayer.view.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.ActivitySplashBinding
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.view.MainActivity
import com.yogesh.videoplayer.view.permission.AllowPermissionActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var activitySplashBinding: ActivitySplashBinding
    private lateinit var intent: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activitySplashBinding.root)
    }

    private fun setAppName() {
        val text = Constants.APP_NAME
        val spannableString = SpannableString(text)
        val redColorSpan = ForegroundColorSpan(Color.RED)
        spannableString.setSpan(redColorSpan, 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val whiteColorSpan = ForegroundColorSpan(Color.WHITE)
        spannableString.setSpan(whiteColorSpan, 5, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        activitySplashBinding.name.text = spannableString
    }

    override fun onResume() {
        super.onResume()

        animation()
        val timer = object : CountDownTimer(4700, 100) {
            override fun onTick(p: Long) {

            }

            override fun onFinish() {
                redirectToNextActivityAccordingToPermission()
            }
        }
        timer.start()
    }

    private fun animation() {
        setAppName()
        activitySplashBinding.bg.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.bamboo))
        activitySplashBinding.bg.start()
        activitySplashBinding.bg.setOnPreparedListener { mp -> mp.isLooping = true }

        activitySplashBinding.bg.animate().translationY(-5000F).setDuration(1000)
            .setStartDelay(3500)
        activitySplashBinding.parrot.animate().translationY(5000F).setDuration(1000)
            .setStartDelay(3500)
        activitySplashBinding.name.animate().translationY(5000F).setDuration(1000)
            .setStartDelay(3500)
        val animation =
            AnimationUtils.loadAnimation(this@SplashActivity, R.anim.splashtextanimation)
        activitySplashBinding.name.startAnimation(animation)
        activitySplashBinding.lottie.animate().translationY(5000F).setDuration(1000)
            .setStartDelay(3500)
    }

    private fun redirectToNextActivityAccordingToPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = if (Environment.isExternalStorageManager()) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, AllowPermissionActivity::class.java)
            }
        } else {
            intent = if (ContextCompat.checkSelfPermission(
                    this@SplashActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, AllowPermissionActivity::class.java)
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(intent)
        finish()
    }
}