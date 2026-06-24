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
    private var currentSearchQuery: String = ""
    private var isDateFilterActive: Boolean = false
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val tasks = firebaseService.getTasksByUser(userId)
                originalTasks = tasks
                applyFiltersAndSort()
                Log.d(TAG, "Loaded ${tasks.size} tasks for user: $userId")
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
                applyFiltersAndSort()
                Log.d(TAG, "loadTasksByPlan - GOT TASKS: ${tasks.size} tasks")
            } catch (e: Exception) {
                Log.e(TAG, "loadTasksByPlan - ERROR: ${e.message}")
                _error.value = e.message
                e.printStackTrace()
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
                originalTasks = allTasks
                isDateFilterActive = true
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                applyFiltersAndSort()
                Log.d(TAG, "loadTasksByPlanAndDate - GOT ${_tasks.value.size} tasks for selected date")
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
                originalTasks = allTasks
                isDateFilterActive = true
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                applyFiltersAndSort()
                Log.d(TAG, "loadTasksByUserAndDate - GOT ${_tasks.value.size} tasks for selected date")
            } catch (e: Exception) {
                Log.e(TAG, "loadTasksByUserAndDate - ERROR: ${e.message}")
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyFiltersAndSort() {
        var result = originalTasks

        if (isDateFilterActive) {
            result = result.filter { task ->
                task.dataTimeStart?.let { date ->
                    val calendar = Calendar.getInstance().apply { time = date }
                    calendar.get(Calendar.YEAR) == selectedYear &&
                            calendar.get(Calendar.MONTH) == selectedMonth &&
                            calendar.get(Calendar.DAY_OF_MONTH) == selectedDay
                } ?: false
            }
        }

        if (currentSearchQuery.isNotEmpty()) {
            result = result.filter {
                it.title.contains(currentSearchQuery, ignoreCase = true) ||
                        it.description.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        result = applyFilterByStatus(result)

        result = applySort(result)

        _tasks.value = result
    }

    private fun applyFilterByStatus(tasks: List<TodoTaskModel>): List<TodoTaskModel> {
        return when (currentFilterCriteria) {
            "completed" -> tasks.filter { it.status == TodoTaskModel.STATUS_COMPLETED }
            "pending" -> tasks.filter { it.status == TodoTaskModel.STATUS_PENDING }
            "in_progress" -> tasks.filter { it.status == TodoTaskModel.STATUS_IN_PROGRESS }
            "overdue" -> tasks.filter { task ->
                task.dataTimeEnd != null &&
                        task.dataTimeEnd!!.before(Date()) &&
                        task.status != TodoTaskModel.STATUS_COMPLETED
            }
            else -> tasks
        }
    }

    private fun applySort(tasks: List<TodoTaskModel>): List<TodoTaskModel> {
        return when (currentSortCriteria) {
            "title" -> tasks.sortedBy { it.title }
            "date" -> tasks.sortedByDescending { it.dataTimeStart }
            "status" -> tasks.sortedBy { it.isCompleted }
            "priority" -> tasks.sortedBy { it.priorityId }
            else -> tasks.sortedByDescending { it.dataTimeStart }
        }
    }

    fun searchTasks(query: String) {
        currentSearchQuery = query.trim()
        applyFiltersAndSort()
    }

    fun sortTasks(criteria: String) {
        currentSortCriteria = criteria
        applyFiltersAndSort()
    }

    fun filterTasks(criteria: String) {
        currentFilterCriteria = criteria
        isDateFilterActive = false
        applyFiltersAndSort()
    }

    fun filterByDate(year: Int, month: Int, day: Int) {
        isDateFilterActive = true
        selectedYear = year
        selectedMonth = month
        selectedDay = day
        currentFilterCriteria = "all"
        applyFiltersAndSort()
    }

    fun clearDateFilter() {
        isDateFilterActive = false
        applyFiltersAndSort()
    }

    fun clearAllFilters() {
        currentSearchQuery = ""
        currentFilterCriteria = "all"
        isDateFilterActive = false
        currentSortCriteria = "date"
        applyFiltersAndSort()
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

                if (planId.isNotEmpty()) {
                    loadTasksByPlan(planId)
                } else {
                    loadTasks(userId)
                }
                onSuccess(taskId)
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
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

                    val index = originalTasks.indexOfFirst { it.id == taskId }
                    if (index != -1) {
                        originalTasks = originalTasks.toMutableList().apply {
                            set(index, updatedTask)
                        }
                    }

                    applyFiltersAndSort()
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

    fun updateTask(task: TodoTaskModel, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firebaseService.updateTask(task)

                val index = originalTasks.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    originalTasks = originalTasks.toMutableList().apply {
                        set(index, task)
                    }
                }

                applyFiltersAndSort()
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

                originalTasks = originalTasks.filter { it.id != taskId }

                applyFiltersAndSort()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshTasks() {
        applyFiltersAndSort()
    }
}