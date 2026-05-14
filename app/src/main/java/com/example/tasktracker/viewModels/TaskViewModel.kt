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
import java.util.Calendar
import java.util.Date

class TaskViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val TAG = "TaskViewModel"

    private val _tasks = MutableStateFlow<List<TodoTaskModel>>(emptyList())
    val tasks: StateFlow<List<TodoTaskModel>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var originalTasks: List<TodoTaskModel> = emptyList()
    private var currentSortCriteria: String = "date"
    private var currentFilterCriteria: String = "all"

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val tasks = firebaseService.getTasksByUser(userId)
                originalTasks = tasks
                _tasks.value = tasks
                Log.d(TAG, "Loaded ${tasks.size} tasks for user: $userId")
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
                    isCompleted = false,
                    dataTimeStart = dataTimeStart,
                    dataTimeEnd = dataTimeEnd
                )
                val taskId = firebaseService.createTask(task)

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
                Log.d(TAG, "loadTasksByPlan - START, planId: $planId")
                val tasks = firebaseService.getTasksByPlan(planId)
                originalTasks = tasks
                _tasks.value = tasks
                Log.d(TAG, "loadTasksByPlan - GOT TASKS: ${tasks.size} tasks")
                tasks.forEach { task ->
                    Log.d(TAG, "  - Task: ${task.title}, ID: ${task.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadTasksByPlan - ERROR: ${e.message}")
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                Log.d(TAG, "loadTasksByPlan - FINISHED, final tasks count: ${_tasks.value.size}")
            }
        }
    }

    fun updateTaskStatus(taskId: String, isCompleted: Boolean, userId: String, planId: String = "", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentTasks = _tasks.value
                val task = currentTasks.find { it.id == taskId }
                if (task != null) {
                    val newStatus = if (isCompleted) {
                        TodoTaskModel.STATUS_COMPLETED
                    } else {
                        TodoTaskModel.STATUS_PENDING
                    }
                    val updatedTask = task.copy(
                        isCompleted = isCompleted,
                        status = newStatus
                    )
                    firebaseService.updateTask(updatedTask)
                    if (planId.isNotEmpty()) {
                        loadTasksByPlan(planId)
                    } else {
                        loadTasks(userId)
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun forceLoadTasksByPlan(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "forceLoadTasksByPlan - START, planId: $planId")
                val tasks = firebaseService.getTasksByPlan(planId)
                originalTasks = tasks
                _tasks.value = tasks
            } catch (e: Exception) {
                Log.e(TAG, "forceLoadTasksByPlan - ERROR: ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTasksByPlanAndDate(planId: String, year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "loadTasksByPlanAndDate - START, planId: $planId, date: $day.${month + 1}.$year")
                val allTasks = firebaseService.getTasksByPlan(planId)
                val filteredTasks = allTasks.filter { task ->
                    task.dataTimeStart?.let { date ->
                        val calendar = Calendar.getInstance().apply { time = date }
                        calendar.get(Calendar.YEAR) == year &&
                                calendar.get(Calendar.MONTH) == month &&
                                calendar.get(Calendar.DAY_OF_MONTH) == day
                    } ?: false
                }
                originalTasks = allTasks
                _tasks.value = filteredTasks
                Log.d(TAG, "loadTasksByPlanAndDate - GOT TASKS: ${filteredTasks.size} tasks for selected date")
            } catch (e: Exception) {
                Log.e(TAG, "loadTasksByPlanAndDate - ERROR: ${e.message}")
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTasksByUserAndDate(userId: String, year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "loadTasksByUserAndDate - START, userId: $userId, date: $day.${month + 1}.$year")
                val allTasks = firebaseService.getTasksByUser(userId)
                val filteredTasks = allTasks.filter { task ->
                    task.dataTimeStart?.let { date ->
                        val calendar = Calendar.getInstance().apply { time = date }
                        calendar.get(Calendar.YEAR) == year &&
                                calendar.get(Calendar.MONTH) == month &&
                                calendar.get(Calendar.DAY_OF_MONTH) == day
                    } ?: false
                }
                originalTasks = allTasks
                _tasks.value = filteredTasks
                Log.d(TAG, "loadTasksByUserAndDate - GOT TASKS: ${filteredTasks.size} tasks for selected date")
            } catch (e: Exception) {
                Log.e(TAG, "loadTasksByUserAndDate - ERROR: ${e.message}")
                _error.value = e.message
                e.printStackTrace()
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

    fun searchTasks(query: String) {
        if (query.isEmpty()) {
            _tasks.value = originalTasks
        } else {
            val filtered = originalTasks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
            _tasks.value = filtered
        }
    }

    fun sortTasks(criteria: String) {
        currentSortCriteria = criteria
        val sorted = _tasks.value.toMutableList()
        when (criteria) {
            "title" -> sorted.sortBy { it.title }
            "date" -> sorted.sortByDescending { it.dataTimeStart }
            "status" -> sorted.sortBy { it.isCompleted }
            "priority" -> sorted.sortBy { it.priorityId }
        }
        _tasks.value = sorted
    }

    fun filterTasks(criteria: String) {
        currentFilterCriteria = criteria
        val filtered = when (criteria) {
            "completed" -> originalTasks.filter { it.status == TodoTaskModel.STATUS_COMPLETED }
            "pending" -> originalTasks.filter { it.status == TodoTaskModel.STATUS_PENDING }
            "in_progress" -> originalTasks.filter { it.status == TodoTaskModel.STATUS_IN_PROGRESS }
            "overdue" -> originalTasks.filter { task ->
                task.dataTimeEnd != null && task.dataTimeEnd!!.before(Date()) && task.status != TodoTaskModel.STATUS_COMPLETED
            }
            else -> originalTasks
        }
        _tasks.value = filtered
    }

    fun filterByDate(year: Int, month: Int, day: Int) {
        val filtered = originalTasks.filter { task ->
            task.dataTimeStart?.let { date ->
                val calendar = Calendar.getInstance().apply { time = date }
                calendar.get(Calendar.YEAR) == year &&
                        calendar.get(Calendar.MONTH) == month &&
                        calendar.get(Calendar.DAY_OF_MONTH) == day
            } ?: false
        }
        _tasks.value = filtered
    }
}