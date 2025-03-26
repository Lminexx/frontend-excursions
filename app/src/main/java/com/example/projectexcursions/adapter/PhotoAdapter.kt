package com.example.projectexcursions.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ItemPhotoBinding
import com.example.projectexcursions.ui.fullscreen.FullScreenPhotoActivity

class PhotoAdapter(private val context: Context, private var photoList: List<Uri>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val imageUri = photoList[position]
        val photoHolder = holder as PhotoViewHolder
        photoHolder.bind(imageUri)

        holder.itemView.setOnClickListener {
            val intent = FullScreenPhotoActivity.createIntent(context, photoList, position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePhotos(newPhotos: List<Uri>) {
        photoList = newPhotos
        notifyDataSetChanged()
    }

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUri: Uri) {
            Glide.with(binding.imageView.context)
                .load(imageUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .placeholder(R.color.lighter_blue)
                .error(R.drawable.ic_app_v3)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        e?.printStackTrace()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(binding.imageView)
        }
    }
}

