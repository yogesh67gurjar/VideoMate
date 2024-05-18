package com.yogesh.videoplayer.view.adapters

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.RvVideoBinding
import com.yogesh.videoplayer.model.FolderResponse
import com.yogesh.videoplayer.model.VideoResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.RecyclerViewClickListener
import java.io.File


class VideosAdapter(
    val context: Context,
    private var videosList: List<VideoResponse>,
    private val clickListenter: RecyclerViewClickListener
) :
    RecyclerView.Adapter<VideosAdapter.VideosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        return VideosViewHolder(
            RvVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {

        val singleUnit = videosList[position]

        holder.binding.rvVideoNameTv.text = singleUnit.displayName

        val strSize: String = singleUnit.size
        holder.binding.rvVideoSizeTv.text = Formatter.formatFileSize(context, strSize.toLong())

        val milliSeconds: Double = singleUnit.duration.toDouble()
        holder.binding.durationTv.text = timeConversion(milliSeconds.toLong())

        Glide.with(context).load(File(singleUnit.path))
            .into(holder.binding.thumbnail)

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                clickListenter.onClick(position, Constants.VIDEO)
            }
        }

        holder.binding.threeDots.setOnClickListener {

            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                clickListenter.onClick(position, Constants.THREE_DOTS)
            }

        }
    }

    private fun timeConversion(value: Long): String {
        val videoTime: String
        val duration = value.toInt()
        val hrs = duration / 3600000
        val mins = duration / 60000 % 60000
        val secs = duration % 60000 / 1000
        videoTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
        return videoTime
    }

    override fun getItemCount(): Int {
        return videosList.size
    }

    fun filterList(filterlist: MutableList<VideoResponse>) {
        videosList = filterlist
        notifyDataSetChanged()
    }


    class VideosViewHolder(val binding: RvVideoBinding) : RecyclerView.ViewHolder(binding.root)
}

