package com.example.tasktracker.components.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.tasktracker.databinding.BottomSheetEditAccountingBinding
import com.example.tasktracker.models.AccountingModel
import com.example.tasktracker.models.TodoFileModel
import com.example.tasktracker.services.FirebaseService
import com.example.tasktracker.utils.SPHelper
import kotlinx.coroutines.launch

class EditAccountingBottomSheet(
    private val accounting: AccountingModel,
    private val currentFile: TodoFileModel?
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditAccountingBinding? = null
    private val binding get() = _binding!!

    private var onAccountingUpdatedListener: ((AccountingModel) -> Unit)? = null
    private val firebaseService = FirebaseService()
    private var selectedFile: TodoFileModel? = currentFile
    private var selectedCurrency = accounting.currency

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetEditAccountingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()
        setupClickListeners()
        setupCurrencyField()
    }

    private fun setupCurrencyField() {
        binding.etCurrencySymbol.isFocusable = false
        binding.etCurrencySymbol.isClickable = true
        binding.etCurrencySymbol.setOnClickListener {
            showCurrencyPicker()
        }
    }

    private fun loadData() {
        binding.etTitle.setText(accounting.title)
        binding.etDescription.setText(accounting.description)
        binding.etBuyerName.setText(accounting.buyerName)
        binding.etBuyerContacts.setText(accounting.buyerContacts)
        binding.etPrice.setText(accounting.price.toString())
        binding.etCurrencySymbol.setText(accounting.currency)

        if (currentFile != null) {
            binding.tvSelectedFile.text = currentFile.name
            binding.tvSelectedFile.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.btnUpdate.setOnClickListener {
            updateAccounting()
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

    private fun updateAccounting() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val buyerName = binding.etBuyerName.text.toString().trim()
        val buyerContacts = binding.etBuyerContacts.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Введите название"
            return
        }

        if (buyerName.isEmpty()) {
            binding.etBuyerName.error = "Введите имя покупателя"
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0

        val updatedAccounting = accounting.copy(
            title = title,
            description = description,
            fileId = selectedFile?.id ?: "",
            buyerName = buyerName,
            buyerContacts = buyerContacts,
            price = price,
            currency = selectedCurrency
        )

        onAccountingUpdatedListener?.invoke(updatedAccounting)
        dismiss()
    }

    private fun getCurrentUserId(): String {
        val spHelper = SPHelper.getInstance(requireContext())
        return spHelper.getUserId()
    }

    fun setOnAccountingUpdatedListener(listener: (AccountingModel) -> Unit) {
        onAccountingUpdatedListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}