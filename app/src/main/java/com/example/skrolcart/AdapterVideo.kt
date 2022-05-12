package com.example.skrolcart

import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skrolcart.databinding.ItemVideoViewBinding


class AdapterVideo() : ListAdapter<VideoModel, AdapterVideo.ViewHolder>(BannerDiffUtil) {


    inner class ViewHolder(val binding: ItemVideoViewBinding) : RecyclerView.ViewHolder(binding.root)


    object BannerDiffUtil : DiffUtil.ItemCallback<VideoModel>() {
        override fun areItemsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VideoModel, newItem:VideoModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemVideoViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = getItem(position)
        val id:String?= video.id
        val title:String?=video.title
        val timestamp:String?=video.timeStamp
        val videoUri:String?=video.videoUri

        holder.binding.apply {
            tvTitle.text=title
            setVideoUrl(video, holder)
        }
    }

    private fun setVideoUrl(video: VideoModel, holder: ViewHolder) {
        holder.binding.progressBar.visibility=View.GONE
        val videoUrl:String?=video.videoUri
        val videoUri = Uri.parse(videoUrl)
        holder.binding.videoView.setVideoURI(videoUri)
        holder.binding.videoView.requestFocus()

        holder.binding.videoView.setOnPreparedListener {
            it.start()
        }
        holder.binding.videoView.setOnInfoListener(MediaPlayer.OnInfoListener{mp, what, extra->
            when(what){
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START->{
                    holder.binding.progressBar.visibility=View.GONE
                    return@OnInfoListener true
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_START ->{
                    holder.binding.progressBar.visibility=View.GONE
                    return@OnInfoListener true
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_END ->{
                    holder.binding.progressBar.visibility=View.GONE
                    return@OnInfoListener true
                }
            }
            false

        })

        holder.binding.videoView.setOnCompletionListener { mediaPlayer->
            mediaPlayer.start()
            holder.binding.progressBar.visibility=View.GONE
        }
    }

}