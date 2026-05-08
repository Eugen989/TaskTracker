package com.example.tasktracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.ActivityAuthenticationBinding
import com.example.tasktracker.fragments.Authentication.AuthenticationLoginFragment
import com.example.tasktracker.fragments.MainMenu.MainMenuProjectsFragment

class AuthenticationActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"

    private var _binding: ActivityAuthenticationBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d(TAG, TAG + " init")

        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    fun initView() {
//        Log.d(TAG, "init")

        replaceFragment(AuthenticationLoginFragment.newInstance())
    }

    fun replaceFragment(fragment: Fragment) {
//        Log.d(TAG, "replaceFragment tag - " + fragment.tag + " " + fragment)
        supportFragmentManager.beginTransaction()
            .replace(
                binding.fragmentContainer.id,
                fragment,
                fragment.tag
            )
            .commit()
    }

    fun transitionToSettingsUserActivity() {
        val intent = Intent(this, TaskActivity::class.java)
        startActivity(intent)
    }
}