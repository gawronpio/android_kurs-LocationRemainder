package com.udacity.project4.ui.reminderslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.databinding.ListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemindersListListener(val clickListener: (id: Long) -> Unit) {
    fun onClick(reminderDTO: ReminderDTO) {
        clickListener(reminderDTO.id!!)
    }
}

sealed class DataItem {
    data class PoiRecyclerItem(val reminderDTO: ReminderDTO) : DataItem() {
        override val id: Long = reminderDTO.id!!
    }

    abstract val id: Long
}

class RemindersListDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class RemindersListRecyclerAdapter(private val clickListener: RemindersListListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(RemindersListDiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    class ViewHolder private constructor(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReminderDTO, clickListener: RemindersListListener) {
            binding.reminder = item
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
                val reminderItem = getItem(position) as DataItem.PoiRecyclerItem
                holder.bind(reminderItem.reminderDTO, clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    fun addAndSubmitList(list: List<ReminderDTO>?) {
        if(list != null) {
            adapterScope.launch {
                withContext(Dispatchers.Main) {
                    submitList(list.map { DataItem.PoiRecyclerItem(it) })
                }
            }
        }
    }
}