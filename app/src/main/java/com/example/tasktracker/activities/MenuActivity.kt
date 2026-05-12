package com.example.tasktracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tasktracker.R
import com.example.tasktracker.databinding.ActivityMenuBinding
import com.example.tasktracker.fragments.MainMenu.MainMenuAccountingFragment
import com.example.tasktracker.fragments.MainMenu.MainMenuFilesFragment
import com.example.tasktracker.fragments.MainMenu.MainMenuFriendsFragment
import com.example.tasktracker.fragments.MainMenu.MainMenuProjectsFragment
import com.example.tasktracker.utils.SPHelper

class MenuActivity : AppCompatActivity() {
    private val TAG = "MenuActivity"

    private var _binding: ActivityMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
        refreshCurrentFragment()
    }

    fun initView() {
        binding.bottomNavigationView.selectedItemId = R.id.menu_tasks
        replaceFragment(MainMenuProjectsFragment.newInstance())

        binding.ivSettings.setOnClickListener { transitionToSettingsUserActivity() }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener{
            when(it.itemId) {
                R.id.menu_tasks -> { replaceFragment(MainMenuProjectsFragment.newInstance()) }
                R.id.menu_accounting -> { replaceFragment(MainMenuAccountingFragment.newInstance()) }
                R.id.menu_files -> { replaceFragment(MainMenuFilesFragment.newInstance()) }
                R.id.menu_friends -> { replaceFragment(MainMenuFriendsFragment.newInstance()) }
            }
            true
        }

        updateUserInfo()
    }

    private fun refreshCurrentFragment() {
        val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
        if (currentFragment is MainMenuProjectsFragment) {
            replaceFragment(MainMenuProjectsFragment.newInstance())
        }
    }

    private fun updateUserInfo() {
        val user = SPHelper.getInstance(this).getUser()
        if (user != null && user.login!!.isNotEmpty()) {
            binding.tvUserTitle.text = user.login
            Log.d(TAG, "updateUserInfo - " + user.login)
        } else {
            binding.tvUserTitle.text = "Гость"
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.fragmentContainer.id,
                fragment,
                fragment.tag
            )
            .commit()
    }

    fun transitionToSettingsUserActivity() {
        val intent = Intent(this, SettingsUserActivity::class.java)
        startActivity(intent)
    }
}