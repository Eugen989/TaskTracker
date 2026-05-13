package com.example.tasktracker.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.models.AccountingModel
import com.example.tasktracker.models.TodoFileModel
import com.example.tasktracker.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountingViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val TAG = "AccountingViewModel"

    private val _accountingList = MutableStateFlow<List<Pair<AccountingModel, TodoFileModel?>>>(emptyList())
    val accountingList: StateFlow<List<Pair<AccountingModel, TodoFileModel?>>> = _accountingList.asStateFlow()

    private val _currentAccounting = MutableStateFlow<AccountingModel?>(null)
    val currentAccounting: StateFlow<AccountingModel?> = _currentAccounting.asStateFlow()

    private val _currentFile = MutableStateFlow<TodoFileModel?>(null)
    val currentFile: StateFlow<TodoFileModel?> = _currentFile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadAccountingByUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "=== loadAccountingByUser START ===")
                Log.d(TAG, "userId: $userId")

                val accountingList = firebaseService.getAllAccountingWithFiles(userId)

                Log.d(TAG, "Received ${accountingList.size} records from Firebase")
                accountingList.forEachIndexed { index, (accounting, file) ->
                    Log.d(TAG, "Record $index: id=${accounting.id}, title=${accounting.title}, createdBy=${accounting.createdBy}")
                }

                _accountingList.value = accountingList
                Log.d(TAG, "StateFlow updated with ${_accountingList.value.size} items")
                Log.d(TAG, "=== loadAccountingByUser END ===")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error loading accounting: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAccounting(
        title: String,
        description: String,
        fileId: String,
        buyerName: String,
        buyerContacts: String,
        price: Double,
        currency: String = "₽",
        createdBy: String,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "=== createAccounting START ===")
                Log.d(TAG, "title: $title, createdBy: $createdBy")

                val accounting = AccountingModel(
                    title = title,
                    description = description,
                    fileId = fileId,
                    buyerName = buyerName,
                    buyerContacts = buyerContacts,
                    price = price,
                    currency = currency,
                    createdBy = createdBy
                )

                val accountingId = firebaseService.createAccounting(accounting)
                Log.d(TAG, "Accounting created with ID: $accountingId")

                if (fileId.isNotEmpty()) {
                    val file = firebaseService.getFileById(fileId)
                    if (file != null) {
                        val updatedFile = file.copy(accountingId = accountingId)
                        firebaseService.updateFile(updatedFile)
                        Log.d(TAG, "File updated with accountingId: $accountingId")
                    }
                }

                loadAccountingByUser(createdBy)
                onSuccess(accountingId)
                Log.d(TAG, "=== createAccounting END ===")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error creating accounting: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAccounting(accounting: AccountingModel, userId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                firebaseService.updateAccounting(accounting)
                loadAccountingByUser(userId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error updating accounting: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccounting(accountingId: String, userId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                firebaseService.deleteAccounting(accountingId)
                loadAccountingByUser(userId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error deleting accounting: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectAccounting(accounting: AccountingModel, file: TodoFileModel?) {
        _currentAccounting.value = accounting
        _currentFile.value = file
    }
}