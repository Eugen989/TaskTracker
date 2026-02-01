package com.example.tasktracker.activities

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.ActivityTaskBinding
import com.example.tasktracker.fragments.MyTasks.TaskListFragment

class TaskActivity : AppCompatActivity() {
    private val TAG = "TaskActivity"

    private var _binding: ActivityTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, TAG + " init");

        _binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, TaskListFragment.newInstance(), null)
            .commit()
    }

}