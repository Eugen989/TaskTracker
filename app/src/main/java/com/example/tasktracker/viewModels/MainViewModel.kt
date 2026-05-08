package com.example.tasktracker.components

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.services.FirebaseService
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val firebaseService = FirebaseService()

    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val error: MutableLiveData<String?> = MutableLiveData(null)
    val isInitialized: MutableLiveData<Boolean> = MutableLiveData(false)

    fun initializeReferenceData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                firebaseService.initializeReferenceData()
                firebaseService.initializePriorities()
                isInitialized.value = true
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }
}