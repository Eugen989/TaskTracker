package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.databinding.ItemKanbanTaskBinding
import com.example.tasktracker.models.TodoTaskModel
import java.text.SimpleDateFormat
import java.util.Locale

class KanbanTaskAdapter(
    private val onMoveForward: ((TodoTaskModel) -> Unit)? = null,
    private val onMoveBack: ((TodoTaskModel) -> Unit)? = null
) : RecyclerView.Adapter<KanbanTaskAdapter.KanbanTaskViewHolder>() {

    private var items: List<TodoTaskModel> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

    fun submitList(newItems: List<TodoTaskModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanbanTaskViewHolder {
        val binding = ItemKanbanTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KanbanTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KanbanTaskViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class KanbanTaskViewHolder(private val binding: ItemKanbanTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TodoTaskModel) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDate.text = task.dataTimeStart?.let { dateFormat.format(it) } ?: "—"

            if (onMoveBack != null) {
                binding.btnMoveBack.visibility = View.VISIBLE
                binding.btnMoveBack.setOnClickListener {
                    onMoveBack.invoke(task)
                }
            } else {
                binding.btnMoveBack.visibility = View.GONE
            }

            if (onMoveForward != null) {
                binding.btnMoveForward.visibility = View.VISIBLE
                binding.btnMoveForward.setOnClickListener {
                    onMoveForward.invoke(task)
                }
            } else {
                binding.btnMoveForward.visibility = View.GONE
            }
        }
    }
}