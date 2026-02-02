package com.example.tasktracker.activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.tasktracker.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tasktracker.components.dialogs.ChangerTaskTypeDialog
import com.example.tasktracker.databinding.ActivityTaskBinding
import com.example.tasktracker.fragments.MyTasks.TaskListFragment
import com.google.android.material.button.MaterialButton

class TaskActivity : AppCompatActivity() {
    private val TAG = "TaskActivity"
    private val DISPLAY_LIST = 0
    private val DISPLAY_DATA = 1

    private var selectedDisplayType: Int? = 0
    private var _binding: ActivityTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, TAG + " init");

        _binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mbtnDisplayType.setOnClickListener { onDisplayTypeDialog() }

        changeDisplay()
    }

    fun changeDisplay() {
        when(selectedDisplayType) {
            0 -> showTaskListPart()
            1 -> showTaskDataPart()
            else -> showTaskListPart()
        }
    }

    fun showTaskListPart() {
        Log.d(TAG, "showTaskListPart")
        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, TaskListFragment.newInstance(), null)
            .commit()
    }

    fun showTaskDataPart() {
        Log.d(TAG, "showTaskDataPart")
        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, TaskListFragment.newInstance(), null)
            .commit()
    }

    fun onDisplayTypeDialog() {
//        val dialogBindoing = layoutInflater.inflate(R.layout.dialog_changer_task_display_type, null)
//        val dialog = Dialog(this)
//        dialog.setContentView(dialogBindoing)
//        dialog.setCancelable(true)
//
//        dialog.findViewById<MaterialButton>(R.id.mbtnList).setOnClickListener {
//            selectedDisplayType = 0
//        }
//
//        dialog.findViewById<MaterialButton>(R.id.mbtnCalendar).setOnClickListener {
//            selectedDisplayType = 1
//        }
//
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.show()

        val dialog = ChangerTaskTypeDialog()
        dialog.setOnTypeSelectedListener { type ->
            selectedDisplayType = type
            Log.d(TAG, "Dialog type - " + selectedDisplayType)
            changeDisplay()
        }

        dialog.show(supportFragmentManager, "ChangerTaskTypeDialog")
    }

}