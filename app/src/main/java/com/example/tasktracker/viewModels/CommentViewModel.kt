package com.example.tasktracker.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.models.CommentModel
import com.example.tasktracker.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    private val firebaseService = FirebaseService()

    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadComments(todoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _comments.value = firebaseService.getCommentsByTask(todoId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addComment(todoId: String, userId: String, text: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val commentModel = CommentModel(
                    userId = userId,
                    todoId = todoId,
                    text = text
                )
                firebaseService.addComment(commentModel)
                loadComments(todoId)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}