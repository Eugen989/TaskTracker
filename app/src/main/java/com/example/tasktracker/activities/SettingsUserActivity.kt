package com.example.tasktracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.databinding.ActivitySettingsUserBinding

class SettingsUserActivity : AppCompatActivity() {
    private val TAG = "SettingsUserActivity"

    private var _binding: ActivitySettingsUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, TAG + " init")

        _binding = ActivitySettingsUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    fun initView() {
        binding.mbTitle.setOnClickListener {
            Log.d(TAG, "mbTitle click")
            transitionToMenuActivity() }
    }

    fun transitionToMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}