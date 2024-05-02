package com.yogesh.videoplayer.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.ActivityMainBinding
import com.yogesh.videoplayer.utils.FragmentMethods
import com.yogesh.videoplayer.utils.Session
import com.yogesh.videoplayer.view.fragments.FolderFragment
import com.yogesh.videoplayer.view.fragments.VideoFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var session: Session
    private lateinit var fragment: Fragment
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        initSetup()
    }

    private fun clickEvents() {

    }

    private fun initSetup() {
        clickEvents()
        fragment = FolderFragment()
        FragmentMethods.addFragment(R.id.frame, fragment, supportFragmentManager)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.frame)
        if (currentFragment is VideoFragment) {
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            finish()
        }
    }
}