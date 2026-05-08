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
import com.example.tasktracker.models.TodoTaskModel
import java.text.SimpleDateFormat
import java.util.Locale

class TaskListAdapter(
    private var onClick: (TodoTaskModel) -> Unit,
    private var items: List<TodoTaskModel> = emptyList()
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
    fun submitList(newItems: List<TodoTaskModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TaskListHolder(
        private val binding: ItemTaskListBinding,
        private var onClickItem: (TodoTaskModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        fun bind(item: TodoTaskModel) {
            binding.tvItemTitle.text = item.title
            binding.tvData.text = item.dataTimeStart?.let { dateFormat.format(it) } ?: "Дата не указана"
            binding.tvItemUser.text = item.description

            binding.layoutTodoInformationContainer.setOnClickListener {
                onClickItem(item)
            }

            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadius = 16f
            shape.setColor(ContextCompat.getColor(binding.root.context, R.color.orange))
            shape.alpha = 200

            binding.layoutTodoContainer.background = shape
        }
    }
}