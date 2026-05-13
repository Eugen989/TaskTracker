package com.example.tasktracker.components.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.tasktracker.databinding.BottomSheetCreateAccountingBinding
import com.example.tasktracker.models.AccountingModel
import com.example.tasktracker.models.TodoFileModel
import com.example.tasktracker.services.FirebaseService
import com.example.tasktracker.utils.SPHelper
import kotlinx.coroutines.launch

class CreateAccountingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreateAccountingBinding? = null
    private val binding get() = _binding!!

    private var onAccountingCreatedListener: ((AccountingModel) -> Unit)? = null
    private val firebaseService = FirebaseService()
    private var selectedFile: TodoFileModel? = null
    private var selectedCurrency = "₽"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetCreateAccountingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setDefaultCurrency()
    }

    private fun setDefaultCurrency() {
        binding.etCurrencySymbol.setText("₽")
        binding.etCurrencySymbol.isFocusable = false
        binding.etCurrencySymbol.isClickable = true
        binding.etCurrencySymbol.setOnClickListener {
            showCurrencyPicker()
        }
    }

    private fun setupClickListeners() {
        binding.btnCreate.setOnClickListener {
            createAccounting()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSelectFile.setOnClickListener {
            showFilePicker()
        }
    }

    private fun showCurrencyPicker() {
        val currencies = listOf(
            "₽" to "Рубль",
            "$" to "Доллар",
            "€" to "Евро",
            "Br" to "Белорусский рубль"
        )

        val currencyNames = currencies.map { "${it.second} (${it.first})" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите валюту")
            .setItems(currencyNames) { _, which ->
                selectedCurrency = currencies[which].first
                binding.etCurrencySymbol.setText(selectedCurrency)
            }
            .show()
    }

    private fun showFilePicker() {
        lifecycleScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                if (currentUserId.isEmpty()) return@launch

                val tasks = firebaseService.getTasksByUser(currentUserId)
                val files = mutableListOf<TodoFileModel>()

                for (task in tasks) {
                    val taskFiles = firebaseService.getFilesByTask(task.id)
                    files.addAll(taskFiles)
                }

                if (files.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет доступных файлов", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val fileNames = files.map { it.name }.toTypedArray()

                AlertDialog.Builder(requireContext())
                    .setTitle("Выберите файл")
                    .setItems(fileNames) { _, which ->
                        selectedFile = files[which]
                        binding.tvSelectedFile.text = selectedFile?.name
                        binding.tvSelectedFile.visibility = View.VISIBLE
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки файлов: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAccounting() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val buyerName = binding.etBuyerName.text.toString().trim()
        val buyerContacts = binding.etBuyerContacts.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val currentUserId = getCurrentUserId()

        if (title.isEmpty()) {
            binding.etTitle.error = "Введите название"
            return
        }

        if (buyerName.isEmpty()) {
            binding.etBuyerName.error = "Введите имя покупателя"
            return
        }

        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0

        val accounting = AccountingModel(
            title = title,
            description = description,
            fileId = selectedFile?.id ?: "",
            buyerName = buyerName,
            buyerContacts = buyerContacts,
            price = price,
            currency = selectedCurrency,
            createdBy = currentUserId
        )

        onAccountingCreatedListener?.invoke(accounting)
        dismiss()
    }

    private fun getCurrentUserId(): String {
        val spHelper = SPHelper.getInstance(requireContext())
        return spHelper.getUserId()
    }

    fun setOnAccountingCreatedListener(listener: (AccountingModel) -> Unit) {
        onAccountingCreatedListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}