package com.example.tasktracker.fragments.MainMenu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.FragmentMainMenuAccountingBinding

class MainMenuAccountingFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuAccountingFragment = MainMenuAccountingFragment()
    }
    private val TAG = "MainMenuAccountingFragment"

    private var _binding: FragmentMainMenuAccountingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuAccountingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "init")
    }
}