package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.databinding.ItemTaskListBinding
import com.example.tasktracker.models.TodoTaskModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskListAdapter(
    private var onClick: (TodoTaskModel) -> Unit,
    private var onStatusChanged: (TodoTaskModel, Boolean) -> Unit,
    private var items: List<TodoTaskModel> = emptyList()
) : RecyclerView.Adapter<TaskListAdapter.TaskListHolder>() {
    private val TAG = "TaskListAdapter"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskListHolder {
        return TaskListHolder(
            ItemTaskListBinding
                .inflate(LayoutInflater.from(parent.context), parent, false),
            onClickItem = onClick,
            onStatusChanged = onStatusChanged
        )
    }

    override fun onBindViewHolder(
        holder: TaskListHolder,
        position: Int
    ) {
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
        private var onClickItem: (TodoTaskModel) -> Unit,
        private var onStatusChanged: (TodoTaskModel, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private var currentTask: TodoTaskModel? = null

        fun bind(item: TodoTaskModel) {
            currentTask = item

            // Заголовок задачи
            binding.tvItemTitle.text = item.title

            // Дата
            binding.tvData.text = item.dataTimeStart?.let { dateFormat.format(it) } ?: "Дата не указана"

            // Приоритет
            binding.tvPriority.text = getPriorityText(item.priorityId)
            binding.tvPriority.setTextColor(getPriorityColor(item.priorityId))

            // Статус
            binding.tvStatus.text = if (item.isCompleted) "Выполнена" else "В процессе"
            binding.tvStatus.setTextColor(if (item.isCompleted) {
                ContextCompat.getColor(binding.root.context, R.color.green)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.orange)
            })

            // Checkbox
            binding.cbTaskCompleted.isChecked = item.isCompleted
            binding.cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
                currentTask?.let { task ->
                    onStatusChanged(task, isChecked)
                }
            }

            // Обработка нажатия на контейнер
            binding.layoutTodoInformationContainer.setOnClickListener {
                onClickItem(item)
            }

            // Стилизация для выполненной задачи
            if (item.isCompleted) {
                binding.tvItemTitle.setAlpha(0.6f)
                binding.tvData.setAlpha(0.6f)
                binding.tvPriority.setAlpha(0.6f)
                binding.tvStatus.setAlpha(0.6f)
            } else {
                binding.tvItemTitle.setAlpha(1f)
                binding.tvData.setAlpha(1f)
                binding.tvPriority.setAlpha(1f)
                binding.tvStatus.setAlpha(1f)
            }
        }

        private fun getPriorityText(priorityId: String): String {
            return when (priorityId) {
                "1", "high" -> "Высокий"
                "2", "medium" -> "Средний"
                "3", "low" -> "Низкий"
                else -> "Обычный"
            }
        }

        private fun getPriorityColor(priorityId: String): Int {
            return when (priorityId) {
                "1", "high" -> ContextCompat.getColor(binding.root.context, R.color.red)
                "2", "medium" -> ContextCompat.getColor(binding.root.context, R.color.orange)
                "3", "low" -> ContextCompat.getColor(binding.root.context, R.color.green)
                else -> ContextCompat.getColor(binding.root.context, R.color.gray)
            }
        }
    }
}