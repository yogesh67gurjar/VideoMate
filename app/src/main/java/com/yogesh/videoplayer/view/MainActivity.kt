package com.yogesh.videoplayer.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.ActivityMainBinding
import com.yogesh.videoplayer.utils.Session
import com.yogesh.videoplayer.view.fragments.FolderFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var session: Session
    private lateinit var fragment: Fragment
    private lateinit var fragmentTransaction: FragmentTransaction
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
        activityMainBinding.title.text = "Folders"

        fragment = FolderFragment()
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.frame, fragment).commit()
    }


}