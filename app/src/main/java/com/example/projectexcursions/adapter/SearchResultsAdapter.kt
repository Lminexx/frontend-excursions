package com.example.projectexcursions.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.models.SearchResult


class SearchResultsAdapter(
    private val onItemClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private val items = mutableListOf<SearchResult>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<SearchResult>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SearchResult) {
            (itemView as TextView).text = item.name
        }
    }
}