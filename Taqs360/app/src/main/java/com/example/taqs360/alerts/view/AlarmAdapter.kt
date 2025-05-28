package com.example.taqs360.alerts.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taqs360.databinding.ItemAlarmBinding
import com.example.taqs360.alerts.model.AlarmData

class AlarmAdapter(
    private val onAlarmClick: (AlarmData) -> Unit,
    private val onDeleteClick: (AlarmData) -> Unit
) : ListAdapter<AlarmData, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: AlarmData) {
            binding.tvLocation.text = alarm.locationName
            binding.tvDateTime.text = alarm.formattedDateTime
            binding.tvWeatherStatus.text = alarm.weatherStatus
            binding.root.setOnClickListener {
                onAlarmClick(alarm)
            }
            binding.btnDelete.setOnClickListener {
                onDeleteClick(alarm)
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<AlarmData>() {
        override fun areItemsTheSame(oldItem: AlarmData, newItem: AlarmData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlarmData, newItem: AlarmData): Boolean {
            return oldItem == newItem
        }
    }
}