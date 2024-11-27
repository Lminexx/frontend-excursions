package com.example.projectexcursions.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.databinding.ItemExcursionBinding
import com.example.projectexcursions.models.Excursion
import javax.inject.Inject

class ExcursionAdapter @Inject constructor(
    diffCallback: DiffUtil.ItemCallback<Excursion>
) : PagingDataAdapter<Excursion, ExcursionAdapter.ExcursionViewHolder>(diffCallback) {

    var onExcursionClickListener: OnExcursionClickListener? = null
    interface OnExcursionClickListener {
        fun onExcursionClick(excursion: Excursion)
    }
    inner class ExcursionViewHolder(private val binding: ItemExcursionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(excursion: Excursion?) {
            if (excursion != null) {
                binding.tvExcursionTitle.text = excursion.title
                binding.tvExcursionDescription.text = excursion.description

                binding.root.setOnClickListener {
                    onExcursionClickListener?.onExcursionClick(excursion)
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
        holder.bind(getItem(position))
    }
}
