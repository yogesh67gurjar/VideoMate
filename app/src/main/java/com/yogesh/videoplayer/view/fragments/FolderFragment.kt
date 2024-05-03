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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.FragmentFolderBinding
import com.yogesh.videoplayer.model.FolderResponse
import com.yogesh.videoplayer.model.VideoResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.FragmentMethods
import com.yogesh.videoplayer.utils.RecyclerViewClickListener
import com.yogesh.videoplayer.utils.Session
import com.yogesh.videoplayer.view.adapters.FoldersAdapter
import com.yogesh.videoplayer.view.permission.AllowPermissionActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FolderFragment : Fragment(), RecyclerViewClickListener {
    private lateinit var fragmentFolderBinding: FragmentFolderBinding
    private lateinit var foldersAdapter: FoldersAdapter
    private lateinit var myContext: Context
    private lateinit var fragment: Fragment

    private var videosList: MutableList<VideoResponse> = mutableListOf()
    private var foldersList: MutableList<FolderResponse> = mutableListOf()

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
        fragmentFolderBinding = FragmentFolderBinding.inflate(inflater, container, false)

        initSetup()

        return fragmentFolderBinding.root
    }

    private fun initSetup() {
        clickEvents()
        foldersAdapter = FoldersAdapter(foldersList, this)
        fragmentFolderBinding.folderRecyclerView.adapter = foldersAdapter
        fragmentFolderBinding.folderRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun clickEvents() {
        fragmentFolderBinding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(text: Editable?) {
                filterFolders(text.toString())
            }

        })
    }

    private fun filterFolders(text: String) {

        if (text == "") {
            if (foldersList.isEmpty()) {
                fragmentFolderBinding.folderRecyclerView.visibility = View.GONE
                fragmentFolderBinding.noDataFound.visibility = View.VISIBLE
            } else {
                foldersAdapter.filterList(foldersList)
                fragmentFolderBinding.folderRecyclerView.visibility = View.VISIBLE
                fragmentFolderBinding.noDataFound.visibility = View.GONE
            }
        } else {
            val filteredlist: MutableList<FolderResponse> = mutableListOf()

            for (item in foldersList) {
                val indexPathIndex: Int = item.path.lastIndexOf("/")
                if (item.path.substring(indexPathIndex + 1).lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))
                ) {
                    filteredlist.add(item)
                }
            }
            if (filteredlist.isEmpty()) {
                fragmentFolderBinding.folderRecyclerView.visibility = View.GONE
                fragmentFolderBinding.noDataFound.visibility = View.VISIBLE
            } else {
                foldersAdapter.filterList(filteredlist)
                fragmentFolderBinding.folderRecyclerView.visibility = View.VISIBLE
                fragmentFolderBinding.noDataFound.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentFolderBinding.rootLayout.visibility = View.VISIBLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Environment.isExternalStorageManager()) {
                startActivity(Intent(context, AllowPermissionActivity::class.java))
            } else {
                showFolders()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    myContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                startActivity(Intent(context, AllowPermissionActivity::class.java))
            } else {
                showFolders()
            }
        }
    }

    private fun showFolders() {
        foldersList.clear()
        videosList.clear()
        videosList.addAll(getFoldersFunc())

        if (foldersList.size > 0) {
            foldersAdapter.notifyDataSetChanged()
            fragmentFolderBinding.folderRecyclerView.visibility = View.VISIBLE
            fragmentFolderBinding.noDataFound.visibility = View.GONE
        } else {
            fragmentFolderBinding.folderRecyclerView.visibility = View.GONE
            fragmentFolderBinding.noDataFound.visibility = View.VISIBLE
        }
    }

    private fun getFoldersFunc(): MutableList<VideoResponse> {
        val videoList: MutableList<VideoResponse> = mutableListOf()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = myContext.contentResolver.query(uri, null, null, null, null)
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

                val currentVideoIndex = path.lastIndexOf("/")
                val subString = path.substring(0, currentVideoIndex)


                if (!foldersList.any { it.path.contains(subString) }) {
                    foldersList.add(FolderResponse(subString, getVideosCount(subString)))
                }
                videoList.add(video)
            } while (cursor.moveToNext())
        }
        return videoList
    }

    private fun getVideosCount(folderPath: String): Int {
        val videos: MutableList<VideoResponse> = mutableListOf()

        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val selection =
            MediaStore.Video.Media.DATA + " LIKE ? AND " + MediaStore.Video.Media.DATA + " NOT LIKE ?"
        val selectionArgs = arrayOf(
            "%$folderPath/%",
            "%$folderPath/%/%"
        )
        val cursor = myContext.contentResolver.query(uri, null, selection, selectionArgs, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
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
            }
            cursor.close()
        }
        return videos.size
    }

    override fun onClick(position: Int, type: String) {
        if (type == Constants.FOLDER) {
            fragment = VideoFragment()
            session.saveData(Constants.FOLDER_PATH, foldersList[position].path)
            FragmentMethods.addFragment(R.id.frame, fragment, parentFragmentManager)
        }
    }

}