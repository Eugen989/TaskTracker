package com.example.tasktracker.fragments.MainMenu

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.tasktracker.R
import com.example.tasktracker.databinding.FragmentMainMenuDrawingBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainMenuDrawingFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuDrawingFragment = MainMenuDrawingFragment()
    }

    private val TAG = "MainMenuDrawingFragment"
    private var _binding: FragmentMainMenuDrawingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuDrawingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTools()

        binding.drawingView.setTool("pen")
        binding.drawingView.setColor(Color.BLACK)
    }

    private fun setupTools() {
        binding.btnPen.setOnClickListener {
            binding.drawingView.setTool("pen")
            updateToolButtonSelection(binding.btnPen)
            Toast.makeText(requireContext(), "Карандаш", Toast.LENGTH_SHORT).show()
        }

        binding.btnEraser.setOnClickListener {
            binding.drawingView.setTool("eraser")
            updateToolButtonSelection(binding.btnEraser)
            Toast.makeText(requireContext(), "Ластик", Toast.LENGTH_SHORT).show()
        }

        binding.btnClear.setOnClickListener {
            showClearDialog()
        }

        binding.btnColorPicker.setOnClickListener {
            showColorPickerDialog()
        }

        binding.btnSave.setOnClickListener {
            saveDrawing()
        }
    }

    private fun updateToolButtonSelection(selectedButton: ImageButton) {
        val buttons = listOf(binding.btnPen, binding.btnEraser, binding.btnClear, binding.btnColorPicker, binding.btnSave)

        buttons.forEach { button ->
            button.background = if (button == selectedButton) {
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_tool_button_selected)
            } else {
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_tool_button)
            }
        }
    }

    private fun showClearDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистить холст")
            .setMessage("Вы уверены, что хотите очистить весь рисунок?")
            .setPositiveButton("Очистить") { _, _ ->
                binding.drawingView.clearCanvas()
                Toast.makeText(requireContext(), "Холст очищен", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showColorPickerDialog() {
        val colors = listOf(
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GRAY
        )

        val colorNames = listOf("Черный", "Красный", "Синий", "Зеленый", "Желтый", "Фиолетовый", "Голубой", "Серый")

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите цвет")
            .setItems(colorNames.toTypedArray()) { _, which ->
                val selectedColor = colors[which]
                binding.drawingView.setColor(selectedColor)
                binding.drawingView.setTool("pen")
                updateToolButtonSelection(binding.btnPen)
                Toast.makeText(requireContext(), "Выбран цвет: ${colorNames[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun saveDrawing() {
        val bitmap = binding.drawingView.saveDrawing()
        if (bitmap == null) {
            Toast.makeText(requireContext(), "Ошибка: не удалось сохранить рисунок", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "drawing_$timeStamp.png"

            val directory: File
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                if (!directory.exists()) {
                    directory.mkdirs()
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                directory = File(picturesDir, "TaskTrackerDrawings")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
            }

            val imageFile = File(directory, fileName)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            Toast.makeText(requireContext(), "Рисунок сохранен: ${imageFile.absolutePath}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}