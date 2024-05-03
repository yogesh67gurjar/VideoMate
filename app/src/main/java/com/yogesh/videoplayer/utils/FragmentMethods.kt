package com.yogesh.videoplayer.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

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