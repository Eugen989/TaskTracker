package com.example.tasktracker.fragments.Settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tasktracker.activities.SettingsUserActivity
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "init")

        binding.mbAccountBand.setOnClickListener {
            Log.d(TAG, "transition to SettingsUserAccountFragment")
            (activity as SettingsUserActivity).replaceFragment(SettingsUserAccountFragment.newInstance())
        }

        binding.mbExitBand.setOnClickListener {}
    }


}