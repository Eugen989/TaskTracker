package com.example.tasktracker.fragments.Settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.FragmentSettingsUserAccountFragmentBinding

class SettingsUserAccountFragment : Fragment() {
    companion object {
        fun newInstance(): SettingsUserAccountFragment = SettingsUserAccountFragment()
    }
    private val TAG = "SettingsUserAccountFragment"

    private var _binding: FragmentSettingsUserAccountFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsUserAccountFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "init")


    }
}