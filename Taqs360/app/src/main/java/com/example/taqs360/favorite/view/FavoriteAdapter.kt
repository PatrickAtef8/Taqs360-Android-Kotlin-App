package com.example.taqs360.favorite.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taqs360.databinding.ItemFavoriteLocationBinding
import com.example.taqs360.favorite.model.data.FavoriteLocation

class FavoriteAdapter(
    private val onItemClick: (FavoriteLocation) -> Unit,
    private val onRemoveClick: (FavoriteLocation) -> Unit
) : ListAdapter<FavoriteLocation, FavoriteAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(private val binding: ItemFavoriteLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(location: FavoriteLocation) {
            binding.tvLocation.text = location.locationName
            binding.root.setOnClickListener { onItemClick(location) }
            binding.btnDelete.setOnClickListener { onRemoveClick(location) }
        }
    }

    class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteLocation>() {
        override fun areItemsTheSame(oldItem: FavoriteLocation, newItem: FavoriteLocation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavoriteLocation, newItem: FavoriteLocation): Boolean {
            return oldItem == newItem
        }
    }
}