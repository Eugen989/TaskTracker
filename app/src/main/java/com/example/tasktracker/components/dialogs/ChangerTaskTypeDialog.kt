package com.example.tasktracker.components.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import coil.EventListener
import com.example.tasktracker.R
import com.example.tasktracker.databinding.DialogChangerTaskDisplayTypeBinding
import com.google.android.material.button.MaterialButton

class ChangerTaskTypeDialog : DialogFragment() {
    private val TAG = "ChangerTaskTypeDialog"

    private var _binding: DialogChangerTaskDisplayTypeBinding? = null
    private val binding get() = _binding!!


    private var selectedDisplayType: ((Int) -> Unit)? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "init")
        _binding = DialogChangerTaskDisplayTypeBinding.inflate(LayoutInflater.from(context))

        binding.mbtnList.setOnClickListener {
            selectedDisplayType?.invoke(0)
            dismiss()
        }

        binding.mbtnCalendar.setOnClickListener {
            selectedDisplayType?.invoke(1)
            dismiss()
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        return dialog
    }

    fun setOnTypeSelectedListener (type: ((Int) -> Unit)) {
        selectedDisplayType = type
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        selectedDisplayType = null
    }
}