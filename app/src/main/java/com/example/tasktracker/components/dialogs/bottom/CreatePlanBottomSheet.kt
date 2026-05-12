package com.example.tasktracker.components.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.tasktracker.components.PlanViewModel
import com.example.tasktracker.databinding.BottomSheetCreatePlanBinding
import com.example.tasktracker.models.PlanModel
import com.example.tasktracker.utils.SPHelper
import kotlinx.coroutines.launch

class CreatePlanBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreatePlanBinding? = null
    private val binding get() = _binding!!

    private val planViewModel: PlanViewModel by viewModels()

    private var onPlanCreatedListener: ((PlanModel) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetCreatePlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnCreate.setOnClickListener {
            createPlan()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun createPlan() {
        val planName = binding.etPlanName.text.toString().trim()
        val planDescription = binding.etPlanDescription.text.toString().trim()

        if (planName.isEmpty()) {
            binding.etPlanName.error = "Введите название проекта"
            binding.etPlanName.requestFocus()
            return
        }

        binding.btnCreate.isEnabled = false
        binding.btnCreate.text = "Создание..."

        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            binding.btnCreate.isEnabled = true
            binding.btnCreate.text = "Создать"
            return
        }

        planViewModel.createPlan(
            name = planName,
            description = planDescription,
            createdBy = currentUserId,
            userIdList = listOf(currentUserId),
            onSuccess = { planId ->
                Toast.makeText(requireContext(), "Проект \"$planName\" создан!", Toast.LENGTH_LONG).show()

                val newPlan = PlanModel(
                    id = planId,
                    name = planName,
                    description = planDescription,
                    userIdList = listOf(currentUserId),
                    createdBy = currentUserId
                )
                onPlanCreatedListener?.invoke(newPlan)
                dismiss()
            }
        )

        lifecycleScope.launch {
            planViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), "Ошибка: $it", Toast.LENGTH_SHORT).show()
                    binding.btnCreate.isEnabled = true
                    binding.btnCreate.text = "Создать"
                }
            }
        }
    }

    private fun getCurrentUserId(): String {
        val user = SPHelper.getInstance(requireContext()).getUser()
        return user?.id ?: ""
    }

    fun setOnPlanCreatedListener(listener: (PlanModel) -> Unit) {
        onPlanCreatedListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}