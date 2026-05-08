package com.example.tasktracker.components

import androidx.lifecycle.ViewModel
import com.example.tasktracker.models.UserModel
import com.example.tasktracker.services.FirebaseService

class LoginViewModel : ViewModel() {
    private val firebaseService = FirebaseService()

    suspend fun loginUser(login: String, password: String): Result<UserModel> {
        return try {
            val user = firebaseService.getUserByLogin(login)
            if (user != null && user.password == password) {
                Result.success(user)
            } else {
                Result.failure(Exception("Неверный логин или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}