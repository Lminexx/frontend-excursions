package com.example.projectexcursions.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.databinding.ItemPlacesBinding

class PlacesAdapter(
    private var placesList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val binding = ItemPlacesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlacesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val placeName = placesList[position]
        holder.bind(placeName)

        holder.itemView.setOnClickListener {
            onItemClick(placeName)
        }
    }

    override fun getItemCount(): Int = placesList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlaces(newPlaces: List<String>) {
        placesList = newPlaces
        notifyDataSetChanged()
    }

    inner class PlacesViewHolder(private val binding: ItemPlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(placeName: String) {
            binding.placeName.text = placeName
        }
    }
}
