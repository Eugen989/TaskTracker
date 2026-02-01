package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.databinding.ItemTaskListBinding
import com.example.tasktracker.models.TodoTask

class TaskListAdapter (
    private var items: List<TodoTask> = emptyList()
) : RecyclerView.Adapter<TaskListAdapter.TaskListHolder>() {
    private val TAG = "TaskListAdapter"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskListHolder {
        Log.d(TAG, TAG + " init")
        return TaskListHolder(ItemTaskListBinding
            .inflate(LayoutInflater.from(parent.context), parent,false))
    }

    override fun onBindViewHolder(
        holder: TaskListHolder,
        position: Int
    ) {
        Log.d(TAG, TAG + " holder")
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    public fun submitList(newItems: List<TodoTask>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TaskListHolder(private val binding: ItemTaskListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TodoTask) {
            binding.tvItemTitle.text = item.title
            binding.tvData.text = item.dataTimeStart
            binding.tvItemUser.text = item.description
        }
    }
}