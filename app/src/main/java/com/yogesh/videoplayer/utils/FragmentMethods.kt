package com.yogesh.videoplayer.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.yogesh.videoplayer.R
import javax.inject.Inject

object FragmentMethods {

    fun addFragment(
        frame: Int,
        fragment: Fragment,
        fragmentManager: FragmentManager
    ) {
        fragmentManager.beginTransaction().add(frame, fragment)
            .addToBackStack(null).commit()
    }


}