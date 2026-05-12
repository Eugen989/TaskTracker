package com.example.tasktracker.components

import androidx.lifecycle.ViewModel
import com.example.tasktracker.models.UserModel
import com.example.tasktracker.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegistrationViewModel : ViewModel() {
    private val firebaseService = FirebaseService()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    suspend fun registerUser(
        name: String,
        login: String,
        email: String,
        password: String
    ): Result<UserModel> {
        return try {
            _isLoading.value = true
            _error.value = null

            // Проверяем, существует ли пользователь с таким логином
            val existingUser = firebaseService.getUserByLogin(login)
            if (existingUser != null) {
                return Result.failure(Exception("Пользователь с таким логином уже существует"))
            }

            val userModel = UserModel(
                id = login,
                name = name,
                login = login,
                email = email,
                password = password
            )

            firebaseService.createUser(userModel)
            _isLoading.value = false
            Result.success(userModel)
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.message
            Result.failure(e)
        }
    }
}