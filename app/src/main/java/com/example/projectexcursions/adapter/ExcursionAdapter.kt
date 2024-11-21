package com.example.projectexcursions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.R
import com.example.projectexcursions.models.Excursion

class ExcursionAdapter(
    private val listener: (Excursion) -> Unit
) : PagingDataAdapter<Excursion, ExcursionAdapter.ExcursionViewHolder>(ExcursionDiffCallback) {

    inner class ExcursionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_excursion_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tv_excursion_description)

        fun bind(excursion: Excursion?) {
            if (excursion != null) {
                titleTextView.text = excursion.title
                descriptionTextView.text = excursion.description

                itemView.setOnClickListener { listener(excursion) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExcursionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_excursion, parent, false)
        return ExcursionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExcursionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object ExcursionDiffCallback : DiffUtil.ItemCallback<Excursion>() {
        override fun areItemsTheSame(oldItem: Excursion, newItem: Excursion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Excursion, newItem: Excursion): Boolean {
            return oldItem == newItem
        }
    }
}
