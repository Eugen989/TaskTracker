package com.example.tasktracker.fragments.AllTasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasktracker.adapters.TaskListAdapter
import com.example.tasktracker.components.decorations.ItemDecoration
import com.example.tasktracker.databinding.FragmentTaskListBinding
import com.example.tasktracker.models.TodoTask

class TaskListFragment : Fragment() {
    companion object {
        fun newInstance() = TaskListFragment()
    }
    private val TAG = "TaskListFragment"

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }



    fun init() {
        Log.d(TAG, "init")

        adapter = TaskListAdapter({ updateElementFragment() })
        binding.rvTasks.addItemDecoration(ItemDecoration(0, 50, 0, 0))
        binding.rvTasks.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvTasks.adapter = adapter

        var ListTodoTasks = mutableListOf<TodoTask>()
        ListTodoTasks.addAll(listOf(
            TodoTask(
                id = 1,
                title = "Первый",
                description = "User 1",
                Importance = 1,
                dataTimeStart = "19.01.2026",
                dataTimeEnd = "20.01.2026",
                filesList = emptyList()
            ),
            TodoTask(
                id = 2,
                title = "Первый 2",
                description = "User 2",
                Importance = 2,
                dataTimeStart = "20.01.2026",
                dataTimeEnd = "22.01.2026",
                filesList = emptyList()
            ),
            TodoTask(
                id = 3,
                title = "Третий",
                description = "User 3",
                Importance = 3,
                dataTimeStart = "24.01.2026",
                dataTimeEnd = "3.02.2026",
                filesList = emptyList()
            ),
            TodoTask(
                id = 34,
                title = "Четвертый",
                description = "User 4",
                Importance = 0,
                dataTimeStart = "24.01.2026",
                dataTimeEnd = "3.02.2026",
                filesList = emptyList()
            )
            ))

        Log.d(TAG, "ListTodoTasks - " + ListTodoTasks)
        adapter.submitList(ListTodoTasks)
    }

    fun updateElementFragment() {

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}