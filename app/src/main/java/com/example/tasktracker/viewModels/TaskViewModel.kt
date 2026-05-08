package com.example.tasktracker.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.models.TodoTaskModel
import com.example.tasktracker.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel : ViewModel() {
    private val firebaseService = FirebaseService()

    private val _tasks = MutableStateFlow<List<TodoTaskModel>>(emptyList())
    val tasks: StateFlow<List<TodoTaskModel>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _tasks.value = firebaseService.getTasksByUser(userId)
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        todoTypeId: String,
        priorityId: String,
        userId: String,
        planId: String = "",
        dataTimeStart: Date?,
        dataTimeEnd: Date?,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val task = TodoTaskModel(
                    title = title,
                    description = description,
                    todoTypeId = todoTypeId,
                    priorityId = priorityId,
                    userId = userId,
                    planId = planId,
                    dataTimeStart = dataTimeStart,
                    dataTimeEnd = dataTimeEnd
                )
                val taskId = firebaseService.createTask(task)

                // Если задача создана в плане, добавляем её в список задач плана
                if (planId.isNotEmpty()) {
                    firebaseService.addTaskToPlan(planId, taskId)
                }

                loadTasks(userId)
                onSuccess(taskId)
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTasksByPlan(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("TaskViewModel", "loadTasksByPlan - START, planId: $planId")
                val tasks = firebaseService.getTasksByPlan(planId)
                Log.d("TaskViewModel", "loadTasksByPlan - GOT TASKS: ${tasks.size} tasks")
                tasks.forEach { task ->
                    Log.d("TaskViewModel", "  - Task: ${task.title}, ID: ${task.id}")
                }
                _tasks.value = tasks
                Log.d("TaskViewModel", "loadTasksByPlan - _tasks updated: ${_tasks.value.size} tasks")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "loadTasksByPlan - ERROR: ${e.message}")
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                Log.d("TaskViewModel", "loadTasksByPlan - FINISHED, final tasks count: ${_tasks.value.size}")
            }
        }
    }

    fun forceLoadTasksByPlan(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("TaskViewModel", "forceLoadTasksByPlan - START, planId: $planId")
                val tasks = firebaseService.getTasksByPlan(planId)
                Log.d("TaskViewModel", "forceLoadTasksByPlan - GOT TASKS: ${tasks.size} tasks")
                _tasks.value = tasks
            } catch (e: Exception) {
                Log.e("TaskViewModel", "forceLoadTasksByPlan - ERROR: ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTask(task: TodoTaskModel, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firebaseService.updateTask(task)
                if (task.planId.isNotEmpty()) {
                    loadTasksByPlan(task.planId)
                } else {
                    loadTasks(task.userId)
                }
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(taskId: String, userId: String, planId: String = "", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firebaseService.deleteTask(taskId)
                if (planId.isNotEmpty()) {
                    loadTasksByPlan(planId)
                } else {
                    loadTasks(userId)
                }
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}