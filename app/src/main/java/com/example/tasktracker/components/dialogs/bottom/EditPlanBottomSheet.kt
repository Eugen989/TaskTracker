package com.example.tasktracker.components.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.tasktracker.databinding.BottomSheetEditPlanBinding
import com.example.tasktracker.models.PlanModel
import com.example.tasktracker.utils.SPHelper

class EditPlanBottomSheet(
    private val plan: PlanModel
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditPlanBinding? = null
    private val binding get() = _binding!!
    private var onPlanUpdatedListener: ((PlanModel) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetEditPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupData()
        setupClickListeners()
    }

    private fun setupData() {
        binding.etPlanName.setText(plan.name)
        binding.etPlanDescription.setText(plan.description)
    }

    private fun setupClickListeners() {
        binding.btnUpdate.setOnClickListener {
            updatePlan()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun updatePlan() {
        val planName = binding.etPlanName.text.toString().trim()
        val planDescription = binding.etPlanDescription.text.toString().trim()

        if (planName.isEmpty()) {
            binding.etPlanName.error = "Введите название проекта"
            binding.etPlanName.requestFocus()
            return
        }

        binding.btnUpdate.isEnabled = false
        binding.btnUpdate.text = "Сохранение..."

        val updatedPlan = plan.copy(
            name = planName,
            description = planDescription
        )

        onPlanUpdatedListener?.invoke(updatedPlan)
        dismiss()
    }

    fun setOnPlanUpdatedListener(listener: (PlanModel) -> Unit) {
        onPlanUpdatedListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}