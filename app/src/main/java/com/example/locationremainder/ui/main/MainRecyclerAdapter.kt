package com.example.locationremainder.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.locationremainder.data.Poi
import com.example.locationremainder.databinding.ListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainListener(val clickListener: (id: Long) -> Unit) {
    fun onClick(poi: Poi) {
        clickListener(poi.id!!)
    }
}

sealed class DataItem {
    data class PoiRecyclerItem(val poi: Poi) : DataItem() {
        override val id: Long = poi.id!!
    }

    abstract val id: Long
}

class MainDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class MainRecyclerAdapter(private val clickListener: MainListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(MainDiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    class ViewHolder private constructor(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Poi, clickListener: MainListener) {
            binding.poi = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val poiItem = getItem(position) as DataItem.PoiRecyclerItem
                holder.bind(poiItem.poi, clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    fun addAndSubmitList(list: List<Poi>?) {
        if(list != null) {
            adapterScope.launch {
                withContext(Dispatchers.Main) {
                    submitList(list.map { DataItem.PoiRecyclerItem(it) })
                }
            }
        }
    }
}