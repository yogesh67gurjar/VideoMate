package com.yogesh.videoplayer.view.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.model.VideoResponse
import com.yogesh.videoplayer.utils.Constants
import com.yogesh.videoplayer.utils.RecyclerViewClickListener
import java.io.File
import java.io.Serializable


class VideosAdapter(
    val context: Context,
    private val videosList: List<VideoResponse>,
    val clickListenter: RecyclerViewClickListener
) :
    RecyclerView.Adapter<VideosAdapter.VideosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.rv_video, parent, false)
        return VideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {

        val singleUnit = videosList[position]

        holder.title.text = singleUnit.displayName

        val strSize: String = singleUnit.size
        holder.size.text = Formatter.formatFileSize(context, strSize.toLong())

        val milliSeconds: Double = singleUnit.duration.toDouble()
        holder.duration.text = timeConversion(milliSeconds.toLong())

        Glide.with(context).load(File(singleUnit.path))
            .into(holder.thumbnail)

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                clickListenter.onClick(position, Constants.VIDEO)
            }
        }

//        holder.threeDots.setOnClickListener { v: View? ->
//            val bundle = Bundle()
//            bundle.putString("name", singleUnit.getDisplayName())
//            bundle.putString("thumbnail", singleUnit.getPath())
//            bundle.putSerializable("video", singleUnit)
//            bundle.putString("folderPath", folderPath)
//            bundle.putString("folderName", folderName)
//            bundle.putSerializable("videos", videos as Serializable)
//            val bottomSheet = VideoThreeDot(context)
//            bottomSheet.setArguments(bundle)
//            bottomSheet.show(fragmentManager, bottomSheet.getTag())
//        }
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

    class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbnail: ImageView
        var threeDots: ImageView
        var duration: TextView
        var title: TextView
        var size: TextView

        init {
            thumbnail = itemView.findViewById(R.id.thumbnail)
            threeDots = itemView.findViewById(R.id.threeDots)
            duration = itemView.findViewById(R.id.durationTv)
            title = itemView.findViewById(R.id.rv_videoNameTv)
            size = itemView.findViewById(R.id.rv_videoSizeTv)
        }
    }
}

