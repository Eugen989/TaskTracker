package com.example.tasktracker.fragments.Authentication

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tasktracker.R
import com.example.tasktracker.activities.AuthenticationActivity
import com.example.tasktracker.activities.MainActivity
import com.example.tasktracker.components.LoginViewModel
import com.example.tasktracker.databinding.FragmentLoginBinding
import com.example.tasktracker.utils.SPHelper
import kotlinx.coroutines.launch

class AuthenticationLoginFragment : Fragment() {
    companion object {
        fun newInstance(): AuthenticationLoginFragment = AuthenticationLoginFragment()
    }
    private val TAG = "AuthenticationLoginFragment"

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var isPasswordVisible = false

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = LoginViewModel()

        setupClickListeners()
        setupPasswordToggle()
    }

    private fun setupPasswordToggle() {
        binding.ivTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                // Скрыть пароль
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_eye_closed)
                )
                isPasswordVisible = false
            } else {
                // Показать пароль
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_eye_open)
                )
                isPasswordVisible = true
            }
            // Устанавливаем курсор в конец текста
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }
    }

    fun setupClickListeners() {
        binding.mbAuthorization.setOnClickListener {
            performLogin()
        }

        binding.mbToRegistration.setOnClickListener {
            (activity as AuthenticationActivity).replaceFragment(
                AuthenticationRegistrationFragment.newInstance()
            )
        }
    }

    private fun performLogin() {
        val login = binding.etLogin.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = viewModel.loginUser(login, password)

            result.onSuccess { user ->
                SPHelper.getInstance(requireContext()).saveUser(user)
                Toast.makeText(requireContext(), "Добро пожаловать, ${user.name}!", Toast.LENGTH_LONG).show()

                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Ошибка: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}