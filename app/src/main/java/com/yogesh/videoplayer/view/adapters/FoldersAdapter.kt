package com.yogesh.videoplayer.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.model.FolderResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.RecyclerViewClickListener

class FoldersAdapter(
    private var folders: List<FolderResponse>,
    private val clickListener: RecyclerViewClickListener
) :
    RecyclerView.Adapter<FoldersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_folder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val singleUnitPath: FolderResponse = folders[position]
        val indexPathIndex = singleUnitPath.path.lastIndexOf("/")
        val folderName = singleUnitPath.path.substring(indexPathIndex + 1)
        holder.name.text = folderName
        holder.noOfFiles.text = "${singleUnitPath.noOfFiles} Videos"

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

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val noOfFiles: TextView = itemView.findViewById(R.id.noOfFiles)

    }
}