package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.databinding.ItemPlanBinding
import com.example.tasktracker.models.PlanModel

class PlanListAdapter(
    private val onClick: (PlanModel) -> Unit,
    private val onSettingsClick: (PlanModel) -> Unit,
    private var items: List<PlanModel> = emptyList()
) : RecyclerView.Adapter<PlanListAdapter.PlanViewHolder>() {

    private val TAG = "PlanListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ItemPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding, onClick, onSettingsClick)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<PlanModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    class PlanViewHolder(
        private val binding: ItemPlanBinding,
        private val onClick: (PlanModel) -> Unit,
        private val onSettingsClick: (PlanModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: PlanModel) {
            val firstLetter = if (plan.name.isNotEmpty()) plan.name.first().uppercase() else "П"
            binding.mbTitleProject.text = firstLetter
            binding.tvTitleProject.text = plan.name
            binding.root.setOnClickListener { onClick(plan) }
            binding.ivSettingsProject.setOnClickListener { onSettingsClick(plan) }
        }
    }
}