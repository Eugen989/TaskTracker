package com.example.tasktracker.adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.databinding.ItemPlanBinding
import com.example.tasktracker.models.TodoTaskModel

class PlansAdapter (
    private var onClick: (TodoTaskModel) -> Unit,
    private var items: List<TodoTaskModel> = emptyList()
) {
    class PlansHolder(
        private val binding: ItemPlanBinding,
        private var onClickItem: (TodoTaskModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvTitleProject
        }
    }
}