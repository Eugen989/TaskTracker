package com.example.tasktracker.fragments.MainMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tasktracker.databinding.FragmentMainMenuFriendsBinding

class MainMenuFriendsFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuFriendsFragment = MainMenuFriendsFragment()
    }

    private var _binding: FragmentMainMenuFriendsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }
}