package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.graphics.Color
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

            binding.tvItemTitle.text = item.title
            binding.tvData.text = item.dataTimeStart?.let { dateFormat.format(it) } ?: "Дата не указана"

            binding.tvPriority.text = getPriorityText(item.priorityId)
            binding.tvPriority.setTextColor(getPriorityColor(item.priorityId))

            val isTaskCompleted = item.status == TodoTaskModel.STATUS_COMPLETED
            binding.tvStatus.text = getStatusText(item.status)
            binding.tvStatus.setTextColor(getStatusColor(item.status))

            binding.cbTaskCompleted.isChecked = isTaskCompleted
            binding.cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
                currentTask?.let { task ->
                    onStatusChanged(task, isChecked)
                }
            }

            binding.layoutTodoInformationContainer.setOnClickListener {
                onClickItem(item)
            }

            if (isTaskCompleted) {
                binding.tvItemTitle.alpha = 0.6f
                binding.tvData.alpha = 0.6f
                binding.tvPriority.alpha = 0.6f
                binding.tvStatus.alpha = 0.6f
            } else {
                binding.tvItemTitle.alpha = 1f
                binding.tvData.alpha = 1f
                binding.tvPriority.alpha = 1f
                binding.tvStatus.alpha = 1f
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

        private fun getStatusText(status: String): String {
            return when (status) {
                TodoTaskModel.STATUS_PENDING -> "К выполнению"
                TodoTaskModel.STATUS_IN_PROGRESS -> "В процессе"
                TodoTaskModel.STATUS_COMPLETED -> "Выполнена"
                else -> "Неизвестно"
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                TodoTaskModel.STATUS_PENDING -> ContextCompat.getColor(binding.root.context, R.color.orange)
                TodoTaskModel.STATUS_IN_PROGRESS -> ContextCompat.getColor(binding.root.context, R.color.blue)
                TodoTaskModel.STATUS_COMPLETED -> ContextCompat.getColor(binding.root.context, R.color.green)
                else -> ContextCompat.getColor(binding.root.context, R.color.gray)
            }
        }
    }
}