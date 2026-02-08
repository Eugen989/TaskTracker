package com.example.tasktracker.fragments.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.FragmentSettingsUserFragmentBinding

class SettingsUserFragment : Fragment() {
    companion object {
        fun newInstance(): SettingsUserFragment = SettingsUserFragment()
    }
    private val TAG = "SettingsUserFragment"

    private var _binding: FragmentSettingsUserFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsUserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}