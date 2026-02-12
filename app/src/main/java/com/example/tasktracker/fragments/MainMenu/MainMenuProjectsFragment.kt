package com.example.tasktracker.fragments.MainMenu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.FragmentMainMenuProjectsBinding

class MainMenuProjectsFragment : Fragment() {
    companion object{
        fun newInstance(): MainMenuProjectsFragment = MainMenuProjectsFragment()
    }
    private val TAG = "MainMenuProjectsFragment"

    private var _binding: FragmentMainMenuProjectsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "init")
    }
}