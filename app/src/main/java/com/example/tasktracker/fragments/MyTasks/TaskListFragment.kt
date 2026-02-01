package com.example.tasktracker.fragments.MyTasks

import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tasktracker.R
import com.example.tasktracker.databinding.FragmentTaskListBinding

class TaskListFragment : Fragment() {
    private val TAG = "TaskListFragment"
    companion object {
        fun newInstance() = TaskListFragment()
    }

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

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


    }

}