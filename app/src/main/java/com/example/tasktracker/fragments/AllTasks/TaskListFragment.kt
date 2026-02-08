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
    private val TAG = "TaskListFragment"
    companion object {
        fun newInstance() = TaskListFragment()
    }

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
        Log.d(TAG, TAG + " init")

        adapter = TaskListAdapter()
        binding.rvTasks.addItemDecoration(ItemDecoration(0, 50, 0, 0))
        binding.rvTasks.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvTasks.adapter = adapter

        var ltt = mutableListOf<TodoTask>()
        ltt.addAll(listOf(
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
            )
            ))

        Log.d(TAG, "ltt - " + ltt)
        adapter.submitList(ltt)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}