package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.databinding.ItemTaskListBinding
import com.example.tasktracker.models.TodoTask

class TaskListAdapter (
    private var onClick: () -> Unit,
    private var items: List<TodoTask> = emptyList()
) : RecyclerView.Adapter<TaskListAdapter.TaskListHolder>() {
    private val TAG = "TaskListAdapter"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskListHolder {
        Log.d(TAG, TAG + " init")
        return TaskListHolder(
            ItemTaskListBinding
                .inflate(LayoutInflater.from(parent.context), parent, false),
            onClickItem = onClick
        )
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

    class TaskListHolder(private val binding: ItemTaskListBinding, private var onClickItem: () -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TodoTask) {
            binding.tvItemTitle.text = item.title
            binding.tvData.text = item.dataTimeStart
            binding.tvItemUser.text = item.description

            binding.layoutTodoInformationContainer.setOnClickListener { onClickItem() }

            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadius = 16f // радиус скругления в пикселях
            shape.setColor(importanceColorMap(item.Importance)) // устанавливаем цвет
            shape.alpha = 200

            binding.layoutTodoContainer.background = shape
        }

        fun importanceColorMap(importance: Int): Int {
            return when (importance) {
                1 -> ContextCompat.getColor(binding.root.context, R.color.red_light)
                2 -> ContextCompat.getColor(binding.root.context, R.color.orange)
                3 -> ContextCompat.getColor(binding.root.context, R.color.salad)
                else -> ContextCompat.getColor(binding.root.context, R.color.invisible)
            }
        }
    }
}