package com.example.tasktracker.fragments.Authentication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tasktracker.activities.AuthenticationActivity
import com.example.tasktracker.components.LoginViewModel
import com.example.tasktracker.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class AuthenticationLoginFragment : Fragment() {
    companion object {
        fun newInstance(): AuthenticationLoginFragment = AuthenticationLoginFragment()
    }
    private val TAG = "AuthenticationLoginFragment"

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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
//        Log.d(TAG, "init")

        viewModel = LoginViewModel()

        setupClickListeners()
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
                Toast.makeText(requireContext(), "Добро пожаловать, ${user.name}!", Toast.LENGTH_LONG).show()
                activity?.finish()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Ошибка: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}