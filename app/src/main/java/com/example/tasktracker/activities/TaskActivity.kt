package com.example.tasktracker.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.LocaleData
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.DatePicker
import com.example.tasktracker.R
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.tasktracker.components.dialogs.ChangerTaskTypeDialog
import com.example.tasktracker.databinding.ActivityTaskBinding
import com.example.tasktracker.fragments.AllTasks.TaskListFragment
import com.google.android.material.button.MaterialButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, TAG + " init");

        _binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    fun initView() {
        Log.d(TAG, "init")

        binding.ivTransitionToMenu.setOnClickListener { transitionToMenuActivity() }
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

        binding.dpCalendar.isVisible = false

        binding.mbtnDisplayType.text = "Title"
        binding.mbtnDisplayType.setIconResource(R.drawable.icon_file_list)
        supportFragmentManager.beginTransaction()
            .replace(
                binding.fragmentContainer.id,
                TaskListFragment.newInstance(),
                "TaskListFragment"
            )
            .commit()
    }

    fun showTaskDataPart() {
        Log.d(TAG, "showTaskDataPart")
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
            Log.d(TAG, "Date changed to: $dateDay ${dateMonth + 1} $dateYear")
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, TaskListFragment.newInstance(), null)
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

    fun setNowDate() {
        val currentDate = Date()
        dateDay = SimpleDateFormat("dd", Locale.getDefault()).format(currentDate).toInt()
        dateMonth = SimpleDateFormat("MM", Locale.getDefault()).format(currentDate).toInt() - 1
        dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(currentDate).toInt()
        Log.d(TAG, "showTaskDataPart - " +
                "${dateDay} " +
                "${dateMonth} " +
                "${dateYear}")
    }

    fun transitionToMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

}