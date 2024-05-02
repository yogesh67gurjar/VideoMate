package com.yogesh.videoplayer.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.FragmentVideoBinding

class VideoFragment : Fragment() {
    private lateinit var fragmentVideoBinding: FragmentVideoBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentVideoBinding = FragmentVideoBinding.inflate(inflater, container, false)


        return fragmentVideoBinding.root
    }
}