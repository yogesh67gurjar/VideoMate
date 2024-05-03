package com.yogesh.videoplayer.view.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yogesh.videoplayer.databinding.FragmentVideoBinding
import com.yogesh.videoplayer.model.VideoResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.RecyclerViewClickListener
import com.yogesh.videoplayer.utils.Session
import com.yogesh.videoplayer.view.adapters.VideosAdapter
import com.yogesh.videoplayer.view.permission.AllowPermissionActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VideoFragment : Fragment(), RecyclerViewClickListener {
    private lateinit var fragmentVideoBinding: FragmentVideoBinding
    private lateinit var videosAdapter: VideosAdapter
    private var videosList: MutableList<VideoResponse> = mutableListOf()
    private lateinit var folderName: String
    private lateinit var folderPath: String
    private lateinit var myContext: Context

    @Inject
    lateinit var session: Session
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentVideoBinding = FragmentVideoBinding.inflate(inflater, container, false)

        initSetup()

        return fragmentVideoBinding.root
    }

    private fun initSetup() {
        videosAdapter = VideosAdapter(myContext, videosList, this)
        fragmentVideoBinding.videoRecyclerView.adapter = videosAdapter
        fragmentVideoBinding.videoRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Environment.isExternalStorageManager()) {
                startActivity(Intent(context, AllowPermissionActivity::class.java))
            } else {
                showVideos()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    myContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                startActivity(Intent(context, AllowPermissionActivity::class.java))
            } else {
                showVideos()
            }
        }

    }

    private fun showVideos() {

        folderPath = session.getData(Constants.FOLDER_PATH).toString()

        val indexPathIndex: Int = folderPath.lastIndexOf("/")
        folderName = folderPath.substring(indexPathIndex + 1)
        fragmentVideoBinding.title.text = folderName
        videosList.clear()

        videosList.addAll(getAllVideos(folderPath))

        if (videosList.size > 0) {
            videosAdapter.notifyDataSetChanged()
            fragmentVideoBinding.videoRecyclerView.visibility = View.VISIBLE
            fragmentVideoBinding.noDataFound.visibility = View.GONE
        } else {
            fragmentVideoBinding.videoRecyclerView.visibility = View.GONE
            fragmentVideoBinding.noDataFound.visibility = View.VISIBLE
        }

    }

    private fun getAllVideos(folderPath: String): MutableList<VideoResponse> {
        val videos: MutableList<VideoResponse> = mutableListOf()

        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val selection =
            MediaStore.Video.Media.DATA + " LIKE ? AND " + MediaStore.Video.Media.DATA + " NOT LIKE ?"
        val selectionArgs = arrayOf(
            "%$folderPath/%",
            "%$folderPath/%/%"
        )
        val cursor: Cursor? =
            myContext.contentResolver.query(uri, null, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToNext()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                val displayName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                val size =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                val duration =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val dateAdded =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
                val video = VideoResponse(id, title, displayName, size, duration, path, dateAdded)
                videos.add(video)
            } while (cursor.moveToNext())
        }
        return videos
    }

    override fun onClick(position: Int, type: String) {
        if (type == Constants.VIDEO) {
            Toast.makeText(myContext, videosList[position].displayName, Toast.LENGTH_SHORT)
                .show()
        }
    }
}