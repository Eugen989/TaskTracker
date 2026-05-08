package com.example.tasktracker.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.tasktracker.R
import com.example.tasktracker.components.TaskViewModel
import com.example.tasktracker.components.dialogs.ChangerTaskTypeDialog
import com.example.tasktracker.components.dialogs.CreateTaskDialog
import com.example.tasktracker.databinding.ActivityTaskBinding
import com.example.tasktracker.fragments.AllTasks.TaskListFragment
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class TaskActivity : AppCompatActivity() {
    private val TAG = "TaskActivity"
    private val DISPLAY_LIST = 0
    private val DISPLAY_DATA = 1

    private var selectedDisplayType: Int? = 0
    private var _binding: ActivityTaskBinding? = null
    private val binding get() = _binding!!

    private var dateDay: Int = 0
    private var dateMonth: Int = 0
    private var dateYear: Int = 0

    // Данные из Intent
    private var currentPlanId: String? = null
    private var currentPlanName: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentPlanId = intent.getStringExtra("PLAN_ID")
        currentPlanName = intent.getStringExtra("PLAN_NAME")
        currentUserId = intent.getStringExtra("USER_ID")

        if (currentPlanId != null) {
            supportActionBar?.title = currentPlanName
        }

        initView()
        changeDisplay()
    }

    fun initView() {
        binding.ivTransitionToMenu.setOnClickListener { transitionToMenuActivity() }
        binding.mbtnDisplayType.setOnClickListener { onDisplayTypeDialog() }

        binding.etNewTask.setOnClickListener {
            showCreateTaskDialog()
        }
        binding.etNewTask.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showCreateTaskDialog()
            }
        }
    }

    private fun showCreateTaskDialog() {
        if (currentPlanId == null) {
            Toast.makeText(this, "План не выбран", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = CreateTaskDialog.newInstance(currentPlanId!!, currentUserId ?: "")
        dialog.setOnTaskCreatedListener { newTask ->
            Log.d(TAG, "Task created: ${newTask.title}, ID: ${newTask.id}")

            val fragment = supportFragmentManager.findFragmentByTag("TaskListFragment")

            if (fragment is TaskListFragment) {
                fragment.refreshTasks()
            } else {
                val bundle = Bundle().apply {
                    putString("PLAN_ID", currentPlanId)
                    putString("USER_ID", currentUserId)
                }
                val newFragment = TaskListFragment.newInstance()
                newFragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .replace(binding.fragmentContainer.id, newFragment, "TaskListFragment")
                    .commit()
            }

            binding.etNewTask.text?.clear()
        }
        dialog.show(supportFragmentManager, "CreateTaskDialog")
    }

    fun changeDisplay() {
        when(selectedDisplayType) {
            0 -> showTaskListPart()
            1 -> showTaskDataPart()
            else -> showTaskListPart()
        }
    }

    fun showTaskListPart() {
        binding.dpCalendar.isVisible = false

        binding.mbtnDisplayType.text = "Title"
        binding.mbtnDisplayType.setIconResource(R.drawable.icon_file_list)

        val fragment = TaskListFragment.newInstance()
        val bundle = Bundle().apply {
            putString("PLAN_ID", currentPlanId)
            putString("USER_ID", currentUserId)
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment, "TaskListFragment")
            .commit()
    }

    fun showTaskDataPart() {
        binding.mbtnDisplayType.text = "Data"
        binding.mbtnDisplayType.setIconResource(R.drawable.icon_calendar_desk)

        binding.dpCalendar.isVisible = true

        if(dateDay == dateMonth && dateMonth == dateYear && dateYear == 0) {
            setNowDate()
        }

        binding.dpCalendar.init(
            dateYear,
            dateMonth,
            dateDay,
        ) { view, year, monthOfYear, dayOfMonth ->
            dateDay = dayOfMonth
            dateMonth = monthOfYear
            dateYear = year
        }

        val fragment = TaskListFragment.newInstance()
        val bundle = Bundle().apply {
            putString("PLAN_ID", currentPlanId)
            putString("USER_ID", currentUserId)
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment, "TaskListFragment")
            .commit()
    }

    fun onDisplayTypeDialog() {
        val dialog = ChangerTaskTypeDialog()
        dialog.setOnTypeSelectedListener { type ->
            selectedDisplayType = type
            changeDisplay()
        }
        dialog.show(supportFragmentManager, "ChangerTaskTypeDialog")
    }

    fun setNowDate() {
        val currentDate = Date()
        dateDay = SimpleDateFormat("dd", Locale.getDefault()).format(currentDate).toInt()
        dateMonth = SimpleDateFormat("MM", Locale.getDefault()).format(currentDate).toInt() - 1
        dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(currentDate).toInt()
        Log.d(TAG, "showTaskDataPart - ${dateDay} ${dateMonth} ${dateYear}")
    }

    fun transitionToMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}