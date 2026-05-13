package com.example.tasktracker.fragments.AllTasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasktracker.adapters.TaskListAdapter
import com.example.tasktracker.components.TaskViewModel
import com.example.tasktracker.components.decorations.ItemDecoration
import com.example.tasktracker.databinding.FragmentTaskListBinding
import com.example.tasktracker.models.TodoTaskModel
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {
    companion object {
        fun newInstance() = TaskListFragment()
    }

    private val TAG = "TaskListFragment"
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var adapter: TaskListAdapter

    private var currentPlanId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== onCreate ===")
        arguments?.let {
            currentPlanId = it.getString("PLAN_ID")
            currentUserId = it.getString("USER_ID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        taskViewModel = TaskViewModel()

        adapter = TaskListAdapter(
            onClick = { task ->
                updateElementFragment(task)
            },
            onStatusChanged = { task, isChecked ->
                updateTaskStatus(task, isChecked)
            },
            items = emptyList()
        )

        binding.rvTasks.addItemDecoration(ItemDecoration(0, 50, 0, 0))
        binding.rvTasks.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvTasks.adapter = adapter

        lifecycleScope.launch {
            taskViewModel.tasks.collect { tasks ->
                adapter.submitList(tasks)
            }
        }

        lifecycleScope.launch {
            taskViewModel.error.collect { error ->
                error?.let {
                    Log.e(TAG, "Error: $it")
                    Toast.makeText(requireContext(), "Ошибка: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadTasks()
    }

    private fun updateTaskStatus(task: TodoTaskModel, isCompleted: Boolean) {
        val updatedTask = task.copy(isCompleted = isCompleted)
        taskViewModel.updateTask(updatedTask) {
            Log.d(TAG, "Task status updated: ${task.title} -> isCompleted=$isCompleted")
            refreshTasks()
        }
    }

    private fun loadTasks() {
        when {
            currentPlanId != null -> {
                taskViewModel.loadTasksByPlan(currentPlanId!!)
            }
            currentUserId != null -> {
                taskViewModel.loadTasks(currentUserId!!)
            }
            else -> {
                Log.w(TAG, "No PLAN_ID or USER_ID provided")
            }
        }
    }

    fun searchTasks(query: String) {
        taskViewModel.searchTasks(query)
    }

    fun sortTasks(criteria: String) {
        taskViewModel.sortTasks(criteria)
    }

    fun filterTasks(criteria: String) {
        taskViewModel.filterTasks(criteria)
    }

    fun filterByDate(year: Int, month: Int, day: Int) {
        taskViewModel.filterByDate(year, month, day)
    }

    fun updateElementFragment(task: TodoTaskModel) {
        Log.d(TAG, "Clicked on task: ${task.title}")
        // TODO: Открыть детали задачи
    }

    fun refreshTasks() {
        loadTasks()
    }

    fun onTaskCreated() {
        loadTasks()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}