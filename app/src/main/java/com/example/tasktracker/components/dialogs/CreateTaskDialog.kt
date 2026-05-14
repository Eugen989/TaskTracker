package com.example.tasktracker.components.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.tasktracker.databinding.DialogCreateTaskBinding
import com.example.tasktracker.models.*
import com.example.tasktracker.services.FirebaseService
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.widget.AutoCompleteTextView

class CreateTaskDialog : DialogFragment() {

    private var _binding: DialogCreateTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseService: FirebaseService
    private var onTaskCreatedListener: ((TodoTaskModel) -> Unit)? = null

    private var todoTypeList = listOf<TodoTypeModel>()
    private var priorityList = listOf<PriorityModel>()

    private var currentPlanId: String? = null
    private var currentUserId: String? = null

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    companion object {
        fun newInstance(planId: String, userId: String): CreateTaskDialog {
            val fragment = CreateTaskDialog()
            val args = Bundle()
            args.putString("PLAN_ID", planId)
            args.putString("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentPlanId = it.getString("PLAN_ID")
            currentUserId = it.getString("USER_ID")
        }
        firebaseService = FirebaseService()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadReferenceData()
        setupClickListeners()
    }

    private fun loadReferenceData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                todoTypeList = firebaseService.getTodoTypes()
                priorityList = firebaseService.getPriorities()

                withContext(Dispatchers.Main) {
                    setupSpinners()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSpinners() {
        val typeNames = todoTypeList.map { it.name }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typeNames)
        (binding.spinnerTodoType as? AutoCompleteTextView)?.setAdapter(typeAdapter)

        val priorityNames = priorityList.map { it.name }
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, priorityNames)
        (binding.spinnerPriority as? AutoCompleteTextView)?.setAdapter(priorityAdapter)
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnCreate.setOnClickListener {
            createTask()
        }

        binding.btnStartDate.setOnClickListener {
            showDatePicker { date ->
                selectedStartDate = date
                binding.tvStartDate.text = formatDate(date)
            }
        }

        binding.btnEndDate.setOnClickListener {
            showDatePicker { date ->
                selectedEndDate = date
                binding.tvEndDate.text = formatDate(date)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            onDateSelected(date)
        }

        datePicker.show(parentFragmentManager, "date_picker")
    }

    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(date)
    }

    private fun createTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTaskTitle.error = "Введите название задачи"
            binding.etTaskTitle.requestFocus()
            return
        }

        if (selectedStartDate == null) {
            Toast.makeText(requireContext(), "Выберите дату начала", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedEndDate == null) {
            Toast.makeText(requireContext(), "Выберите дату окончания", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedTodoTypeName = binding.spinnerTodoType.text.toString()
        val selectedPriorityName = binding.spinnerPriority.text.toString()

        val selectedTodoType = todoTypeList.find { it.name == selectedTodoTypeName }
        val selectedPriority = priorityList.find { it.name == selectedPriorityName }

        if (selectedTodoType == null || selectedPriority == null) {
            Toast.makeText(requireContext(), "Пожалуйста, выберите все параметры", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnCreate.isEnabled = false
        binding.btnCreate.text = "Создание..."

        val task = TodoTaskModel(
            title = title,
            description = description,
            todoTypeId = selectedTodoType.id,
            priorityId = selectedPriority.id,
            userId = currentUserId ?: "",
            planId = currentPlanId ?: "",
            isCompleted = false,
            status = TodoTaskModel.STATUS_PENDING,
            dataTimeStart = selectedStartDate,
            dataTimeEnd = selectedEndDate,
            createdAt = Date()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val taskId = firebaseService.createTask(task)
                val createdTask = task.copy(id = taskId)

                currentPlanId?.let { planId ->
                    firebaseService.addTaskToPlan(planId, taskId)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Задача \"$title\" создана!", Toast.LENGTH_LONG).show()
                    onTaskCreatedListener?.invoke(createdTask)
                    dismiss()
                }
            } catch (e: Exception) {
                Log.e("CreateTaskDialog", "Error creating task: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnCreate.isEnabled = true
                    binding.btnCreate.text = "Создать"
                }
            }
        }
    }

    fun setOnTaskCreatedListener(listener: (TodoTaskModel) -> Unit) {
        onTaskCreatedListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}