package com.example.projectexcursions.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.databinding.ItemPlacesBinding
import com.example.projectexcursions.models.PlaceItem

class PlacesAdapter(
    private val context: Context,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private var places: List<PlaceItem>
) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val binding = ItemPlacesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlacesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val placeItem = places[position]
        holder.bind(placeItem)

        holder.itemView.setOnClickListener { onItemClick(placeItem.name) }
        holder.binding.deletePlace.setOnClickListener { onDeleteClick(placeItem.name) }
    }

    override fun getItemCount(): Int = places.size

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlaces(newPlaces: List<PlaceItem>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    inner class PlacesViewHolder(val binding: ItemPlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val photoAdapter = PhotoAdapter(context, emptyList())

        init {
            binding.placePhoto.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = photoAdapter
            }
        }

        fun bind(placeItem: PlaceItem) {
            binding.placeName.text = placeItem.name
            photoAdapter.updatePhotos(placeItem.photos)
        }
    }
}
