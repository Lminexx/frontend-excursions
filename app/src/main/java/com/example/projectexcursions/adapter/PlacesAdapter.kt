package com.example.projectexcursions.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.databinding.ItemPlacesBinding
import com.example.projectexcursions.models.PlaceItem

class PlacesAdapter(
    private val context: Context,
    private val onItemClick: (PlaceItem) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onApproveClick: (placeId: String, newName: String) -> Unit,
    private val isCreating: Boolean = false,
    private var places: List<PlaceItem>
): RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val binding = ItemPlacesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlacesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val placeItem = places[position]
        holder.bind(placeItem)

        holder.itemView.setOnClickListener { onItemClick(placeItem) }

        if (isCreating) {
            holder.binding.deletePlace.visibility = View.VISIBLE

            holder.binding.deletePlace.setOnClickListener {
                onDeleteClick(placeItem.id)
            }
            holder.binding.approveName.setOnClickListener {
                val entered = holder.binding.enterName.text.toString().trim()
                if (entered.isNotEmpty()) {
                    onApproveClick(placeItem.id, entered)

                    holder.binding.placeName.text = entered
                    holder.binding.placeName.visibility = View.VISIBLE
                    holder.binding.enterName.visibility = View.GONE
                    holder.binding.approveName.visibility = View.GONE
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Введите имя", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val name = placeItem.name
            val isUnknown = name == null || name == "Неизвестное место"

            if (isUnknown) {
                holder.binding.placeName.visibility = View.GONE
                holder.binding.enterName.visibility = View.VISIBLE
                holder.binding.approveName.visibility = View.VISIBLE
            } else {
                holder.binding.placeName.visibility = View.VISIBLE
                holder.binding.placeName.text = name
                holder.binding.enterName.visibility = View.GONE
            }

        } else {
            holder.binding.deletePlace.visibility = View.GONE
            holder.binding.enterName.visibility = View.GONE

            val displayName = placeItem.name ?: "Имя потеряно"
            holder.binding.placeName.visibility = View.VISIBLE
            holder.binding.placeName.text = displayName
        }
    }

    override fun getItemCount(): Int = places.size

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlaces(newPlaces: List<PlaceItem>) {
        Log.d("UpdatingPlaces", "")
        places = newPlaces
        for (place in places)
            Log.d("places", place.name ?: "имя потеряно")
        notifyDataSetChanged()
    }

    inner class PlacesViewHolder(val binding: ItemPlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(placeItem: PlaceItem) {
            binding.placeName.text = placeItem.name
        }
    }
}
