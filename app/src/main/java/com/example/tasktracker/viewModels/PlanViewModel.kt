package com.example.tasktracker.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.models.PlanModel
import com.example.tasktracker.models.PriorityModel
import com.example.tasktracker.models.TodoTaskModel
import com.example.tasktracker.models.TodoTypeModel
import com.example.tasktracker.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class PlanViewModel : ViewModel() {
    private val TAG = "PlanViewModel"

    private val firebaseService = FirebaseService()

    private val _plans = MutableStateFlow<List<PlanModel>>(emptyList())
    val plans: StateFlow<List<PlanModel>> = _plans.asStateFlow()

    private val _tasksOfPlan = MutableStateFlow<List<TodoTaskModel>>(emptyList())
    val tasksOfPlan: StateFlow<List<TodoTaskModel>> = _tasksOfPlan.asStateFlow()

    private val _currentPlan = MutableStateFlow<PlanModel?>(null)
    val currentPlan: StateFlow<PlanModel?> = _currentPlan.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPlansByUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("PlanViewModel", "loadPlansByUser called for userId: $userId")
                val plans = firebaseService.getPlansByUser(userId)
                Log.d("PlanViewModel", "loadPlansByUser received ${plans.size} plans")
                _plans.value = plans
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("PlanViewModel", "Error loading plans: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlansCreatedByUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _plans.value = firebaseService.getPlansCreatedByUser(userId)
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
                _tasksOfPlan.value = firebaseService.getTasksByPlan(planId)
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPlan(
        name: String,
        description: String,
        createdBy: String,
        userIdList: List<String> = listOf(),
        todoTypeIdList: List<String> = emptyList(),
        priorityIdList: List<String> = emptyList(),
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("PlanViewModel", "Creating plan for user: $createdBy")

                var finalTodoTypeIds = todoTypeIdList
                var finalPriorityIds = priorityIdList

                if (finalTodoTypeIds.isEmpty()) {
                    finalTodoTypeIds = getOrCreateDefaultTodoTypes()
                }

                if (finalPriorityIds.isEmpty()) {
                    finalPriorityIds = getOrCreateDefaultPriorities()
                }

                val plan = PlanModel(
                    name = name,
                    description = description,
                    userIdList = userIdList.plus(createdBy),
                    todoTypeIdList = finalTodoTypeIds,
                    priorityIdList = finalPriorityIds,
                    createdBy = createdBy
                )

                Log.d("PlanViewModel", "Creating plan with createdBy: ${plan.createdBy}")
                Log.d("PlanViewModel", "Creating plan with userIdList: ${plan.userIdList}")

                val planId = firebaseService.createPlan(plan)
                Log.d("PlanViewModel", "Plan created with ID: $planId")

                loadPlansByUser(createdBy)
                onSuccess(planId)
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("PlanViewModel", "Error creating plan: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getOrCreateDefaultTodoTypes(): List<String> {
        return try {
            var todoTypes = firebaseService.getTodoTypes()

            if (todoTypes.isEmpty()) {
                Log.d("PlanViewModel", "No todoTypes found in Firebase, creating defaults...")

                val defaultTodoTypes = TodoTypeModel.getDefaults()
                val createdIds = mutableListOf<String>()

                for (todoType in defaultTodoTypes) {
                    val id = firebaseService.createTodoType(todoType)
                    createdIds.add(id)
                    Log.d("PlanViewModel", "Created todoType: ${todoType.name} with ID: $id")
                }

                todoTypes = firebaseService.getTodoTypes()
                todoTypes.map { it.id }
            } else {
                Log.d("PlanViewModel", "Found ${todoTypes.size} todoTypes in Firebase")
                todoTypes.map { it.id }
            }
        } catch (e: Exception) {
            Log.e("PlanViewModel", "Error getting/creating todoTypes: ${e.message}")
            listOf("temp_type_1", "temp_type_2", "temp_type_3", "temp_type_4")
        }
    }

    private suspend fun getOrCreateDefaultPriorities(): List<String> {
        return try {
            var priorities = firebaseService.getPriorities()

            if (priorities.isEmpty()) {
                Log.d("PlanViewModel", "No priorities found in Firebase, creating defaults...")

                val defaultPriorities = PriorityModel.getDefaults()
                val createdIds = mutableListOf<String>()

                for (priority in defaultPriorities) {
                    val id = firebaseService.createPriority(priority)
                    createdIds.add(id)
                    Log.d("PlanViewModel", "Created priority: ${priority.name} with ID: $id")
                }

                priorities = firebaseService.getPriorities()
                priorities.map { it.id }
            } else {
                Log.d("PlanViewModel", "Found ${priorities.size} priorities in Firebase")
                priorities.map { it.id }
            }
        } catch (e: Exception) {
            Log.e("PlanViewModel", "Error getting/creating priorities: ${e.message}")
            listOf("temp_priority_1", "temp_priority_2", "temp_priority_3")
        }
    }

    fun loadTodoTypes(onSuccess: (List<TodoTypeModel>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val todoTypes = firebaseService.getTodoTypes()
                onSuccess(todoTypes)
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            }
        }
    }

    fun loadPriorities(onSuccess: (List<PriorityModel>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val priorities = firebaseService.getPriorities()
                onSuccess(priorities)
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            }
        }
    }

    fun updatePlan(plan: PlanModel, userId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "Updating plan: ${plan.id}, new name: ${plan.name}")
                firebaseService.updatePlan(plan)
                loadPlansByUser(userId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error updating plan: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePlan(planId: String, userId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "Deleting plan: $planId with all tasks")
                firebaseService.deletePlan(planId)
                loadPlansByUser(userId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error deleting plan: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPlan(plan: PlanModel) {
        _currentPlan.value = plan
        loadTasksByPlan(plan.id)
    }

    fun addUserToPlan(planId: String, userId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                firebaseService.addUserToPlan(planId, userId)
                _currentPlan.value?.let { plan ->
                    val updatedUserList = plan.userIdList.toMutableList()
                    if (!updatedUserList.contains(userId)) {
                        updatedUserList.add(userId)
                        _currentPlan.value = plan.copy(userIdList = updatedUserList)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            }
        }
    }

    fun addTaskToPlan(planId: String, taskId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                firebaseService.addTaskToPlan(planId, taskId)
                loadTasksByPlan(planId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            }
        }
    }
}