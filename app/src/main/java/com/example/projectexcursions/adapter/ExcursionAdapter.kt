package com.example.projectexcursions.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ItemExcursionBinding
import com.example.projectexcursions.models.ExcursionsList

class ExcursionAdapter(
    diffCallback: DiffUtil.ItemCallback<ExcursionsList>,
    private val isMineList: Boolean = false,
    private val shouldRate: Boolean = true
): PagingDataAdapter<ExcursionsList, ExcursionAdapter.ExcursionViewHolder>(diffCallback) {

    var onExcursionClickListener: OnExcursionClickListener? = null

    interface OnExcursionClickListener {
        fun onExcursionClick(excursionsList: ExcursionsList)
    }

    inner class ExcursionViewHolder(private val binding: ItemExcursionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(excursionsList: ExcursionsList?) {
            if (excursionsList != null) {
                binding.tvExcursionTitle.text = excursionsList.title
                binding.tvExcursionDescription.text = excursionsList.description
                binding.excursionAuthor.text = excursionsList.userName

                if (shouldRate)
                    binding.rating.text = excursionsList.rating.toString()
                else
                    binding.ratingContainer.visibility = View.GONE

                if (isMineList)
                    if (excursionsList.moderationStatus == "APPROVED")
                        binding.status.setImageResource(R.drawable.approved)
                    else if (excursionsList.moderationStatus == "PENDING") {
                        binding.status.setImageResource(R.drawable.pending)
                        binding.ratingContainer.visibility = View.GONE
                    }

                Glide.with(binding.userAvatar.context)
                    .load(excursionsList.userUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
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
                    .into(binding.userAvatar)

                Glide.with(binding.photo.context)
                    .load(excursionsList.url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
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
                    .into(binding.photo)

                binding.root.setOnClickListener {
                    onExcursionClickListener?.onExcursionClick(excursionsList)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExcursionViewHolder {
        val binding = ItemExcursionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExcursionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExcursionViewHolder, position: Int) {
        val excursion = getItem(position)
        Log.d("ExcursionAdapter", "Binding excursionsList: $excursion")
        holder.bind(excursion)
    }
}
