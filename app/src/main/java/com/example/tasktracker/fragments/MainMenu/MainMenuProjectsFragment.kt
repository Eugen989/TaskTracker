package com.example.tasktracker.fragments.MainMenu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasktracker.activities.TaskActivity
import com.example.tasktracker.adapters.PlanListAdapter
import com.example.tasktracker.components.PlanViewModel
import com.example.tasktracker.components.dialogs.CreatePlanBottomSheet
import com.example.tasktracker.components.decorations.ItemDecoration
import com.example.tasktracker.databinding.FragmentMainMenuProjectsBinding
import com.example.tasktracker.models.PlanModel
import kotlinx.coroutines.launch

class MainMenuProjectsFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuProjectsFragment = MainMenuProjectsFragment()
    }

    private val TAG = "MainMenuProjectsFragment"

    private var _binding: FragmentMainMenuProjectsBinding? = null
    private val binding get() = _binding!!

    private lateinit var planViewModel: PlanViewModel
    private lateinit var planAdapter: PlanListAdapter

    private val currentUserId = "Eugenius" // Заменить на реальный ID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        planViewModel = PlanViewModel()
        setupClickListeners()
        setupRecyclerViews()
        loadData()
        observeViewModels()
    }

    private fun setupClickListeners() {
        binding.ivAddProject.setOnClickListener {
            showCreatePlanDialog()
        }
    }

    private fun showCreatePlanDialog() {
        val bottomSheet = CreatePlanBottomSheet()
        bottomSheet.setOnPlanCreatedListener { newPlan ->
            planViewModel.loadPlansByUser(currentUserId)
            Toast.makeText(requireContext(), "Проект \"${newPlan.name}\" создан!", Toast.LENGTH_SHORT).show()
        }
        bottomSheet.show(parentFragmentManager, "CreatePlanBottomSheet")
    }

    private fun setupRecyclerViews() {
        planAdapter = PlanListAdapter(
            onClick = { plan ->
                navigateToTaskActivity(plan.id, plan.name)
            },
            onSettingsClick = { plan ->
                showPlanOptionsDialog(plan)
            }
        )

        binding.rvProjects?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(ItemDecoration(0, 50, 0, 0))
            adapter = planAdapter
        }
    }

    private fun navigateToTaskActivity(planId: String, planName: String) {
        val intent = Intent(requireContext(), TaskActivity::class.java)
        intent.putExtra("PLAN_ID", planId)
        intent.putExtra("PLAN_NAME", planName)
        intent.putExtra("USER_ID", currentUserId)
        startActivity(intent)
    }

    private fun showPlanOptionsDialog(plan: PlanModel) {
        val options = arrayOf("Редактировать", "Удалить", "Поделиться")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(plan.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editPlan(plan)
                    1 -> deletePlan(plan)
                    2 -> sharePlan(plan)
                }
            }
            .show()
    }

    private fun editPlan(plan: PlanModel) {
        Toast.makeText(requireContext(), "Редактирование плана: ${plan.name}", Toast.LENGTH_SHORT).show()
    }

    private fun deletePlan(plan: PlanModel) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить план")
            .setMessage("Вы уверены, что хотите удалить план \"${plan.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                planViewModel.deletePlan(plan.id, currentUserId) {
                    Toast.makeText(requireContext(), "План удален", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun sharePlan(plan: PlanModel) {
        Toast.makeText(requireContext(), "Шаринг плана: ${plan.name}", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        planViewModel.loadPlansByUser(currentUserId)
    }

    private fun observeViewModels() {
        lifecycleScope.launch {
            planViewModel.plans.collect { plans ->
                planAdapter.submitList(plans)
            }
        }

        lifecycleScope.launch {
            planViewModel.isLoading.collect { isLoading ->
                binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            planViewModel.error.collect { error ->
                error?.let {
                    Log.e(TAG, "Error: $it")
                    Toast.makeText(requireContext(), "Ошибка: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}