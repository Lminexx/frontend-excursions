package com.example.projectexcursions.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.databinding.ItemExcursionBinding
import com.example.projectexcursions.models.ExcursionsList

class ExcursionAdapter(
    diffCallback: DiffUtil.ItemCallback<ExcursionsList>
) : PagingDataAdapter<ExcursionsList, ExcursionAdapter.ExcursionViewHolder>(diffCallback) {

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
