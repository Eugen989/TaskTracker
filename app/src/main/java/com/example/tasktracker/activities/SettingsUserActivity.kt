package com.example.tasktracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.ActivitySettingsUserBinding
import com.example.tasktracker.fragments.Settings.SettingsUserFragment

class SettingsUserActivity : AppCompatActivity() {
    private val TAG = "SettingsUserActivity"

    private var _binding: ActivitySettingsUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d(TAG, TAG + " init")

        _binding = ActivitySettingsUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    fun initView() {
//        Log.d(TAG, "init")

        binding.mbTitle.setOnClickListener {
//            Log.d(TAG, "mbTitle click")
            transitionToMenuActivity()
        }

        replaceFragment(SettingsUserFragment.newInstance())
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

    fun transitionToMenuActivity() {
//        val intent = Intent(this, MenuActivity::class.java)
        finish()
//        startActivity(intent)
    }
}