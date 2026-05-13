package com.example.tasktracker.fragments.MainMenu

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.adapters.AccountingAdapter
import com.example.tasktracker.components.AccountingViewModel
import com.example.tasktracker.components.dialogs.CreateAccountingBottomSheet
import com.example.tasktracker.components.dialogs.EditAccountingBottomSheet
import com.example.tasktracker.databinding.FragmentMainMenuAccountingBinding
import com.example.tasktracker.models.AccountingModel
import com.example.tasktracker.models.TodoFileModel
import com.example.tasktracker.utils.SPHelper
import kotlinx.coroutines.launch

class MainMenuAccountingFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuAccountingFragment = MainMenuAccountingFragment()
    }

    private val TAG = "MainMenuAccountingFragment"
    private var _binding: FragmentMainMenuAccountingBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountingViewModel: AccountingViewModel
    private lateinit var accountingAdapter: AccountingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuAccountingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountingViewModel = AccountingViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModels()
        loadData()
    }

    private fun setupRecyclerView() {
        accountingAdapter = AccountingAdapter(
            onItemClick = { accounting, file -> showAccountingDetails(accounting, file) },
            onMenuClick = { accounting, file -> showAccountingOptions(accounting, file) }
        )

        binding.rvAccounting.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = accountingAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            showCreateAccountingDialog()
        }
    }

    private fun loadData() {
        val currentUserId = getCurrentUserId()
        Log.d(TAG, "loadData called for user: $currentUserId")
        if (currentUserId.isNotEmpty()) {
            accountingViewModel.loadAccountingByUser(currentUserId)
        } else {
            Log.e(TAG, "User ID is empty!")
        }
    }

    private fun observeViewModels() {
        lifecycleScope.launch {
            accountingViewModel.accountingList.collect { accountingList ->
                Log.d(TAG, "=== accountingList COLLECTED ===")
                Log.d(TAG, "Size: ${accountingList.size}")
                Log.d(TAG, "Is empty: ${accountingList.isEmpty()}")
                accountingList.forEachIndexed { index, (accounting, file) ->
                    Log.d(TAG, "Item $index: ${accounting.title}, createdBy=${accounting.createdBy}")
                }
                accountingAdapter.submitList(accountingList)
                updateEmptyView(accountingList.isEmpty())
            }
        }

        lifecycleScope.launch {
            accountingViewModel.isLoading.collect { isLoading ->
                Log.d(TAG, "isLoading: $isLoading")
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            accountingViewModel.error.collect { error ->
                error?.let {
                    Log.e(TAG, "Error: $it")
                    Toast.makeText(requireContext(), "Ошибка: $it", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getCurrentUserId(): String {
        val user = SPHelper.getInstance(requireContext()).getUser()
        return user?.id ?: ""
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvAccounting.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvAccounting.visibility = View.VISIBLE
        }
    }

    private fun showCreateAccountingDialog() {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        val bottomSheet = CreateAccountingBottomSheet()
        bottomSheet.setOnAccountingCreatedListener { accounting ->
            accountingViewModel.createAccounting(
                title = accounting.title,
                description = accounting.description,
                fileId = accounting.fileId,
                buyerName = accounting.buyerName,
                buyerContacts = accounting.buyerContacts,
                price = accounting.price,
                currency = accounting.currency,
                createdBy = currentUserId
            ) { accountingId ->
                Toast.makeText(requireContext(), "Продажа добавлена!", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheet.show(parentFragmentManager, "CreateAccountingBottomSheet")
    }

    private fun showAccountingDetails(accounting: AccountingModel, file: TodoFileModel?) {
        val message = buildString {
            append("Название: ${accounting.title}\n\n")
            append("Описание: ${accounting.description}\n\n")
            append("Покупатель: ${accounting.buyerName}\n")
            append("Контакты: ${accounting.buyerContacts}\n\n")
            append("Цена: ${String.format("%.2f", accounting.price)} ${accounting.currency}\n")
            append("Дата: ${accounting.transactionDate}\n\n")
            if (file != null) {
                append("Файл: ${file.name}")
            } else {
                append("Файл не прикреплен")
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Информация о продаже")
            .setMessage(message)
            .setPositiveButton("Закрыть", null)
            .setNeutralButton("Редактировать") { _, _ ->
                showEditAccountingDialog(accounting, file)
            }
            .show()
    }

    private fun showEditAccountingDialog(accounting: AccountingModel, file: TodoFileModel?) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) return

        val bottomSheet = EditAccountingBottomSheet(accounting, file)
        bottomSheet.setOnAccountingUpdatedListener { updatedAccounting ->
            accountingViewModel.updateAccounting(updatedAccounting, currentUserId) {
                Toast.makeText(requireContext(), "Продажа обновлена!", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheet.show(parentFragmentManager, "EditAccountingBottomSheet")
    }

    private fun showAccountingOptions(accounting: AccountingModel, file: TodoFileModel?) {
        val options = arrayOf("Редактировать", "Удалить")

        AlertDialog.Builder(requireContext())
            .setTitle(accounting.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditAccountingDialog(accounting, file)
                    1 -> confirmDeleteAccounting(accounting)
                }
            }
            .show()
    }

    private fun confirmDeleteAccounting(accounting: AccountingModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить запись")
            .setMessage("Вы уверены, что хотите удалить запись о продаже \"${accounting.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                val currentUserId = getCurrentUserId()
                accountingViewModel.deleteAccounting(accounting.id, currentUserId) {
                    Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}