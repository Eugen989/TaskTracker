package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.databinding.ItemAccountingBinding
import com.example.tasktracker.models.AccountingModel
import com.example.tasktracker.models.TodoFileModel
import java.text.SimpleDateFormat
import java.util.*

class AccountingAdapter(
    private val onItemClick: (AccountingModel, TodoFileModel?) -> Unit,
    private val onMenuClick: (AccountingModel, TodoFileModel?) -> Unit
) : RecyclerView.Adapter<AccountingAdapter.AccountingViewHolder>() {

    private var items: List<Pair<AccountingModel, TodoFileModel?>> = emptyList()

    fun submitList(newItems: List<Pair<AccountingModel, TodoFileModel?>>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountingViewHolder {
        val binding = ItemAccountingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountingViewHolder, position: Int) {
        holder.bind(items[position].first, items[position].second)
    }

    override fun getItemCount(): Int = items.size

    inner class AccountingViewHolder(private val binding: ItemAccountingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(accounting: AccountingModel, file: TodoFileModel?) {
            binding.tvTitle.text = accounting.title
            binding.tvBuyer.text = accounting.buyerName
            binding.tvPrice.text = "${String.format("%.2f", accounting.price)} ${accounting.currency}"

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            accounting.transactionDate?.let {
                binding.tvDate.text = dateFormat.format(it)
            } ?: run {
                binding.tvDate.text = "Дата неизвестна"
            }

            val firstLetter = if (accounting.title.isNotEmpty()) accounting.title.first().uppercase() else "П"
            binding.mbTitle.text = firstLetter

            binding.root.setOnClickListener {
                onItemClick(accounting, file)
            }

            binding.btnMenu.setOnClickListener {
                onMenuClick(accounting, file)
            }
        }
    }
}