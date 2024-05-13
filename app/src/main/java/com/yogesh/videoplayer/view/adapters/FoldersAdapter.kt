package com.yogesh.videoplayer.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.RvFolderBinding
import com.yogesh.videoplayer.model.FolderResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.RecyclerViewClickListener

class FoldersAdapter(
    private var folders: List<FolderResponse>,
    private val clickListener: RecyclerViewClickListener
) :
    RecyclerView.Adapter<FoldersAdapter.FoldersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersViewHolder {
        return FoldersViewHolder(RvFolderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: FoldersViewHolder, position: Int) {
        val singleUnitPath: FolderResponse = folders[position]
        val indexPathIndex = singleUnitPath.path.lastIndexOf("/")
        val folderName = singleUnitPath.path.substring(indexPathIndex + 1)
        holder.binding.name.text = folderName
        holder.binding.noOfFiles.text = "${singleUnitPath.noOfFiles} Videos"

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                clickListener.onClick(holder.adapterPosition, Constants.FOLDER)
            }
        }
    }

    fun filterList(filterlist: MutableList<FolderResponse>) {
        folders = filterlist
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    class FoldersViewHolder(val binding: RvFolderBinding) :
        RecyclerView.ViewHolder(binding.root)
}