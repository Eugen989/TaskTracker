package com.example.tasktracker.fragments.Authentication

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
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
import com.example.tasktracker.components.RegistrationViewModel
import com.example.tasktracker.databinding.FragmentRegistrationBinding
import com.example.tasktracker.utils.SPHelper
import kotlinx.coroutines.launch

class AuthenticationRegistrationFragment : Fragment() {
    companion object {
        fun newInstance(): AuthenticationRegistrationFragment = AuthenticationRegistrationFragment()
    }
    private val TAG = "AuthenticationRegistrationFragment"

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = RegistrationViewModel()

        setupClickListeners()
        setupPasswordToggles()
    }

    private fun setupPasswordToggles() {
        // Тогл для пароля
        binding.ivTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_eye_closed)
                )
                isPasswordVisible = false
            } else {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_eye_open)
                )
                isPasswordVisible = true
            }
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }

        // Тогл для подтверждения пароля
        binding.ivToggleConfirmPassword.setOnClickListener {
            if (isConfirmPasswordVisible) {
                binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivToggleConfirmPassword.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_eye_closed)
                )
                isConfirmPasswordVisible = false
            } else {
                binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivToggleConfirmPassword.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_eye_open)
                )
                isConfirmPasswordVisible = true
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text?.length ?: 0)
        }
    }

    private fun setupClickListeners() {
        binding.mbRegistration.setOnClickListener {
            performRegistration()
        }

        binding.mbToAuthorization.setOnClickListener {
            (activity as AuthenticationActivity).replaceFragment(
                AuthenticationLoginFragment.newInstance()
            )
        }
    }

    private fun performRegistration() {
        val name = binding.etName.text.toString().trim()
        val login = binding.etLogin.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (!validateInput(name, login, email, password, confirmPassword)) {
            return
        }

        binding.mbRegistration.isEnabled = false
        binding.mbRegistration.text = "Регистрация..."

        lifecycleScope.launch {
            val result = viewModel.registerUser(name, login, email, password)

            binding.mbRegistration.isEnabled = true
            binding.mbRegistration.text = "Зарегистрироваться"

            result.onSuccess { user ->
                SPHelper.getInstance(requireContext()).saveUser(user)
                Toast.makeText(requireContext(), "Регистрация успешна!", Toast.LENGTH_LONG).show()

                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Ошибка: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Registration error: ${error.message}")
            }
        }
    }

    private fun validateInput(
        name: String,
        login: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        when {
            name.isEmpty() -> {
                binding.etName.error = "Введите имя"
                binding.etName.requestFocus()
                return false
            }
            login.isEmpty() -> {
                binding.etLogin.error = "Введите логин"
                binding.etLogin.requestFocus()
                return false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Введите email"
                binding.etEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Введите корректный email"
                binding.etEmail.requestFocus()
                return false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Введите пароль"
                binding.etPassword.requestFocus()
                return false
            }
            password.length < 4 -> {
                binding.etPassword.error = "Пароль должен быть не менее 4 символов"
                binding.etPassword.requestFocus()
                return false
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Подтвердите пароль"
                binding.etConfirmPassword.requestFocus()
                return false
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Пароли не совпадают"
                binding.etConfirmPassword.requestFocus()
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}