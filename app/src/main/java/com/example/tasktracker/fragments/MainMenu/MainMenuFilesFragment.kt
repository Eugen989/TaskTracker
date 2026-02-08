package com.example.tasktracker.fragments.MainMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.FragmentMainMenuFilesBinding

class MainMenuFilesFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuFilesFragment = MainMenuFilesFragment()
    }

    private var _binding: FragmentMainMenuFilesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuFilesBinding.inflate(inflater, container, false)
        return binding.root
    }
}