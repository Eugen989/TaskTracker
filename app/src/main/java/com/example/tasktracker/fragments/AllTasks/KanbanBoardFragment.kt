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
import com.example.tasktracker.adapters.KanbanTaskAdapter
import com.example.tasktracker.components.TaskViewModel
import com.example.tasktracker.databinding.FragmentKanbanBoardBinding
import com.example.tasktracker.models.TodoTaskModel
import kotlinx.coroutines.launch

class KanbanBoardFragment : Fragment() {
    companion object {
        fun newInstance(planId: String? = null, userId: String? = null): KanbanBoardFragment {
            val fragment = KanbanBoardFragment()
            val args = Bundle()
            args.putString("PLAN_ID", planId)
            args.putString("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }

    private val TAG = "KanbanBoardFragment"
    private var _binding: FragmentKanbanBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskViewModel: TaskViewModel

    private lateinit var pendingAdapter: KanbanTaskAdapter
    private lateinit var inProgressAdapter: KanbanTaskAdapter
    private lateinit var completedAdapter: KanbanTaskAdapter

    private var currentPlanId: String? = null
    private var currentUserId: String? = null

    private var pendingTasks: MutableList<TodoTaskModel> = mutableListOf()
    private var inProgressTasks: MutableList<TodoTaskModel> = mutableListOf()
    private var completedTasks: MutableList<TodoTaskModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentPlanId = it.getString("PLAN_ID")
            currentUserId = it.getString("USER_ID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKanbanBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = TaskViewModel()
        setupRecyclerViews()
        observeTasks()
        loadTasks()
    }

    private fun setupRecyclerViews() {
        pendingAdapter = KanbanTaskAdapter(
            onMoveForward = { task -> moveTask(task, TodoTaskModel.STATUS_IN_PROGRESS) },
            onMoveBack = null
        )

        inProgressAdapter = KanbanTaskAdapter(
            onMoveForward = { task -> moveTask(task, TodoTaskModel.STATUS_COMPLETED) },
            onMoveBack = { task -> moveTask(task, TodoTaskModel.STATUS_PENDING) }
        )

        completedAdapter = KanbanTaskAdapter(
            onMoveForward = null,
            onMoveBack = { task -> moveTask(task, TodoTaskModel.STATUS_IN_PROGRESS) }
        )

        binding.rvPending.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pendingAdapter
        }

        binding.rvInProgress.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inProgressAdapter
        }

        binding.rvCompleted.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = completedAdapter
        }
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            taskViewModel.tasks.collect { tasks ->
                updateKanbanColumns(tasks)
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

        lifecycleScope.launch {
            taskViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateKanbanColumns(tasks: List<TodoTaskModel>) {
        pendingTasks = tasks.filter { it.status == TodoTaskModel.STATUS_PENDING }.toMutableList()
        inProgressTasks = tasks.filter { it.status == TodoTaskModel.STATUS_IN_PROGRESS }.toMutableList()
        completedTasks = tasks.filter { it.status == TodoTaskModel.STATUS_COMPLETED }.toMutableList()

        pendingAdapter.submitList(pendingTasks)
        inProgressAdapter.submitList(inProgressTasks)
        completedAdapter.submitList(completedTasks)

        updateEmptyViews()
    }

    private fun moveTask(task: TodoTaskModel, newStatus: String) {
        val updatedTask = task.copy(
            status = newStatus,
            isCompleted = newStatus == TodoTaskModel.STATUS_COMPLETED
        )

        taskViewModel.updateTask(updatedTask) {
            Log.d(TAG, "Task moved: ${task.title} -> $newStatus")
            loadTasks()
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

    private fun updateEmptyViews() {
        binding.tvEmptyPending.visibility = if (pendingTasks.isEmpty()) View.VISIBLE else View.GONE
        binding.tvEmptyInProgress.visibility = if (inProgressTasks.isEmpty()) View.VISIBLE else View.GONE
        binding.tvEmptyCompleted.visibility = if (completedTasks.isEmpty()) View.VISIBLE else View.GONE
    }

    fun refreshTasks() {
        loadTasks()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}