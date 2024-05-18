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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yogesh.videoplayer.databinding.FragmentVideoBinding
import com.yogesh.videoplayer.model.FolderResponse
import com.yogesh.videoplayer.model.VideoResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.Constants.Companion.ALL_VIDEOS
import com.yogesh.videoplayer.utils.Constants.Companion.VIDEO_INDEX
import com.yogesh.videoplayer.utils.RecyclerViewClickListener
import com.yogesh.videoplayer.utils.Session
import com.yogesh.videoplayer.view.PlayerActivity
import com.yogesh.videoplayer.view.adapters.VideosAdapter
import com.yogesh.videoplayer.view.permission.AllowPermissionActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.newSingleThreadContext
import java.io.Serializable
import java.util.Locale
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
        clickEvents()
        videosAdapter = VideosAdapter(myContext, videosList, this)
        fragmentVideoBinding.videoRecyclerView.adapter = videosAdapter
        fragmentVideoBinding.videoRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun clickEvents() {
        fragmentVideoBinding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(text: Editable?) {
                filterVideos(text.toString())
            }

        })
    }

    private fun filterVideos(text: String) {

        if (text == "") {
            if (videosList.isEmpty()) {
                fragmentVideoBinding.videoRecyclerView.visibility = View.GONE
                fragmentVideoBinding.noDataFound.visibility = View.VISIBLE
            } else {
                videosAdapter.filterList(videosList)
                fragmentVideoBinding.videoRecyclerView.visibility = View.VISIBLE
                fragmentVideoBinding.noDataFound.visibility = View.GONE
            }
        } else {
            val filteredlist: MutableList<VideoResponse> = mutableListOf()

            for (item in videosList) {
                if (item.displayName.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                    filteredlist.add(item)
                }
            }
            if (filteredlist.isEmpty()) {
                fragmentVideoBinding.videoRecyclerView.visibility = View.GONE
                fragmentVideoBinding.noDataFound.visibility = View.VISIBLE
            } else {
                videosAdapter.filterList(filteredlist)

                fragmentVideoBinding.videoRecyclerView.visibility = View.VISIBLE
                fragmentVideoBinding.noDataFound.visibility = View.GONE
            }
        }
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
            val bundle = Bundle()
            bundle.putSerializable(ALL_VIDEOS, videosList as Serializable)
            bundle.putInt(VIDEO_INDEX, position)
            startActivity(Intent(myContext, PlayerActivity::class.java).putExtras(bundle))
        } else if (type == Constants.THREE_DOTS) {
//            val bundle = Bundle()
//            bundle.putString("name", videosList[position].displayName)
//            bundle.putString("thumbnail", videosList[position].path)
//            bundle.putSerializable("video", videosList[position])
//            bundle.putString("folderPath", folderPath)
//            bundle.putString("folderName", folderName)
//            bundle.putSerializable("videos", videos as Serializable)
//            val bottomSheet = VideoThreeDot(context)
//            bottomSheet.setArguments(bundle)
//            bottomSheet.show(fragmentManager, bottomSheet.getTag())
        }
    }
}