package com.example.tasktracker.fragments.MainMenu

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.adapters.FileAdapter
import com.example.tasktracker.databinding.FragmentMainMenuFilesBinding
import com.example.tasktracker.models.TodoFileModel
import com.example.tasktracker.services.FirebaseService
import com.example.tasktracker.utils.SPHelper
import kotlinx.coroutines.launch
import java.io.File

class MainMenuFilesFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuFilesFragment = MainMenuFilesFragment()
    }

    private val TAG = "MainMenuFilesFragment"
    private var _binding: FragmentMainMenuFilesBinding? = null
    private val binding get() = _binding!!

    private var allDrawingsExpanded = false
    private var favoritesExpanded = false
    private var documentsExpanded = false
    private var drawingsExpanded = false

    private lateinit var allDrawingsRecyclerView: RecyclerView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var documentsRecyclerView: RecyclerView
    private lateinit var drawingsRecyclerView: RecyclerView

    private lateinit var allDrawingsAdapter: FileAdapter
    private lateinit var favoritesAdapter: FileAdapter
    private lateinit var documentsAdapter: FileAdapter
    private lateinit var drawingsAdapter: FileAdapter

    private val firebaseService = FirebaseService()
    private var allFiles: List<TodoFileModel> = emptyList()
    private var localDrawings: List<TodoFileModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "init")

        setupRecyclerViews()
        setupClickListeners()
        loadLocalDrawings()
        loadFilesFromFirebase()
    }

    private fun setupRecyclerViews() {
        allDrawingsAdapter = FileAdapter(
            onFileClick = { fileItem -> openLocalFile(fileItem) },
            onMenuClick = { fileItem -> showLocalFileOptionsDialog(fileItem) }
        )

        favoritesAdapter = FileAdapter(
            onFileClick = { fileItem -> openFile(fileItem) },
            onMenuClick = { fileItem -> showFileOptionsDialog(fileItem) }
        )

        documentsAdapter = FileAdapter(
            onFileClick = { fileItem -> openFile(fileItem) },
            onMenuClick = { fileItem -> showFileOptionsDialog(fileItem) }
        )

        drawingsAdapter = FileAdapter(
            onFileClick = { fileItem -> openFile(fileItem) },
            onMenuClick = { fileItem -> showFileOptionsDialog(fileItem) }
        )

        allDrawingsRecyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = allDrawingsAdapter
        }

        favoritesRecyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoritesAdapter
        }

        documentsRecyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = documentsAdapter
        }

        drawingsRecyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = drawingsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAllDrawings.setOnClickListener {
            toggleExpanded(
                binding.allDrawingsContainer,
                allDrawingsRecyclerView,
                allDrawingsExpanded,
                binding.btnAllDrawings
            )
            allDrawingsExpanded = !allDrawingsExpanded
            if (allDrawingsExpanded) {
                loadAllDrawings()
            }
        }

        binding.btnFavorites.setOnClickListener {
            toggleExpanded(
                binding.favoritesContainer,
                favoritesRecyclerView,
                favoritesExpanded,
                binding.btnFavorites
            )
            favoritesExpanded = !favoritesExpanded
            if (favoritesExpanded) {
                loadFavorites()
            }
        }

        binding.btnDocuments.setOnClickListener {
            toggleExpanded(
                binding.documentsContainer,
                documentsRecyclerView,
                documentsExpanded,
                binding.btnDocuments
            )
            documentsExpanded = !documentsExpanded
            if (documentsExpanded) {
                loadDocuments()
            }
        }

        binding.btnDrawings.setOnClickListener {
            toggleExpanded(
                binding.drawingsContainer,
                drawingsRecyclerView,
                drawingsExpanded,
                binding.btnDrawings
            )
            drawingsExpanded = !drawingsExpanded
            if (drawingsExpanded) {
                loadDrawings()
            }
        }

        binding.btnPersonal.setOnClickListener {
            updateTabSelection(true)
            loadPersonalFiles()
        }

        binding.btnShared.setOnClickListener {
            updateTabSelection(false)
            loadSharedFiles()
        }
    }

    private fun updateTabSelection(isPersonal: Boolean) {
        if (isPersonal) {
            binding.btnPersonal.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_button_active_square)
            binding.btnShared.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_button_no_active_square)
        } else {
            binding.btnPersonal.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_button_no_active_square)
            binding.btnShared.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_button_active_square)
        }
    }

    private fun toggleExpanded(container: LinearLayout, recyclerView: RecyclerView, expanded: Boolean, button: View) {
        if (expanded) {
            container.removeAllViews()
            container.visibility = View.GONE
            setButtonIcon(button, false)
        } else {
            container.removeAllViews()
            container.addView(recyclerView)
            container.visibility = View.VISIBLE
            setButtonIcon(button, true)
        }
    }

    private fun setButtonIcon(button: View, isExpanded: Boolean) {
        when (button.id) {
            R.id.btnAllDrawings -> {
                if (isExpanded) {
                    binding.btnAllDrawings.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_down)
                } else {
                    binding.btnAllDrawings.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_half_right)
                }
            }
            R.id.btnFavorites -> {
                if (isExpanded) {
                    binding.btnFavorites.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_down)
                } else {
                    binding.btnFavorites.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_half_right)
                }
            }
            R.id.btnDocuments -> {
                if (isExpanded) {
                    binding.btnDocuments.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_down)
                } else {
                    binding.btnDocuments.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_half_right)
                }
            }
            R.id.btnDrawings -> {
                if (isExpanded) {
                    binding.btnDrawings.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_down)
                } else {
                    binding.btnDrawings.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_arrow_half_right)
                }
            }
        }
    }

    private fun loadLocalDrawings() {
        val drawingsDirectory = getDrawingsDirectory()
        val drawings = mutableListOf<TodoFileModel>()

        drawingsDirectory?.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".png")) {
                drawings.add(TodoFileModel(
                    id = file.absolutePath,
                    name = file.name,
                    todoId = "",
                    fileTypeId = "",
                    fileUrl = file.absolutePath,
                    isFavorite = false,
                    uploadedAt = null
                ))
            }
        }

        localDrawings = drawings
        loadAllDrawings()
    }

    private fun getDrawingsDirectory(): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            File(picturesDir, "TaskTrackerDrawings")
        }
    }

    private fun loadFilesFromFirebase() {
        lifecycleScope.launch {
            try {
                val currentUserId = SPHelper.getInstance(requireContext()).getUserId()
                if (currentUserId.isEmpty()) return@launch

                val tasks = firebaseService.getTasksByUser(currentUserId)
                val files = mutableListOf<TodoFileModel>()

                for (task in tasks) {
                    val taskFiles = firebaseService.getFilesByTask(task.id)
                    files.addAll(taskFiles)
                }

                allFiles = files
                loadPersonalFiles()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading files: ${e.message}")
//                Toast.makeText(requireContext(), "Ошибка загрузки файлов", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPersonalFiles() {
        val personalFiles = allFiles
        updateAllAdapters(personalFiles)
    }

    private fun loadSharedFiles() {
        val sharedFiles = allFiles.filter { false }
        updateAllAdapters(sharedFiles)
    }

    private fun updateAllAdapters(files: List<TodoFileModel>) {
        val favorites = files.filter { it.isFavorite }
        val documents = files.filter { isDocumentFile(it) }
        val drawings = files.filter { isDrawingFile(it) }

        favoritesAdapter.submitList(favorites)
        documentsAdapter.submitList(documents)
        drawingsAdapter.submitList(drawings)
    }

    private fun loadAllDrawings() {
        val allDrawings = localDrawings
        allDrawingsAdapter.submitList(allDrawings)
    }

    private fun loadFavorites() {
        val firebaseFavorites = allFiles.filter { it.isFavorite }
        val localFavorites = localDrawings.filter { it.isFavorite }
        val allFavorites = firebaseFavorites + localFavorites
        favoritesAdapter.submitList(allFavorites)
    }

    private fun loadDocuments() {
        val documents = allFiles.filter { isDocumentFile(it) }
        documentsAdapter.submitList(documents)
    }

    private fun loadDrawings() {
        val drawings = allFiles.filter { isDrawingFile(it) }
        drawingsAdapter.submitList(drawings)
    }

    private fun isDocumentFile(file: TodoFileModel): Boolean {
        val extension = file.name.substringAfterLast(".", "").lowercase()
        return extension in listOf("pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx")
    }

    private fun isDrawingFile(file: TodoFileModel): Boolean {
        val extension = file.name.substringAfterLast(".", "").lowercase()
        return extension in listOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
    }

    private fun openFile(fileItem: TodoFileModel) {
        try {
            val fileUrl = fileItem.fileUrl
            if (fileUrl.startsWith("http")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                startActivity(intent)
            } else {
                val file = File(fileUrl)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        file
                    )

                    val mimeType = getMimeType(fileItem.name)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Файл не найден", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file: ${e.message}")
            Toast.makeText(requireContext(), "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openLocalFile(fileItem: TodoFileModel) {
        try {
            val file = File(fileItem.fileUrl)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val mimeType = getMimeType(fileItem.name)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Файл не найден", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file: ${e.message}")
            Toast.makeText(requireContext(), "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            "mp3" -> "audio/mpeg"
            "mp4" -> "video/mp4"
            else -> "*/*"
        }
    }

    private fun showFileOptionsDialog(fileItem: TodoFileModel) {
        val options = mutableListOf("Поделиться", "Удалить")
        if (fileItem.isFavorite) {
            options.add(0, "Убрать из избранного")
        } else {
            options.add(0, "Добавить в избранное")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(fileItem.name)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Добавить в избранное" -> addToFavorites(fileItem)
                    "Убрать из избранного" -> removeFromFavorites(fileItem)
                    "Поделиться" -> shareFile(fileItem)
                    "Удалить" -> deleteFile(fileItem)
                }
            }
            .show()
    }

    private fun showLocalFileOptionsDialog(fileItem: TodoFileModel) {
        val options = mutableListOf("Поделиться", "Удалить")
        if (fileItem.isFavorite) {
            options.add(0, "Убрать из избранного")
        } else {
            options.add(0, "Добавить в избранное")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(fileItem.name)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Добавить в избранное" -> addToFavorites(fileItem)
                    "Убрать из избранного" -> removeFromFavorites(fileItem)
                    "Поделиться" -> shareLocalFile(fileItem)
                    "Удалить" -> deleteLocalFile(fileItem)
                }
            }
            .show()
    }

    private fun addToFavorites(fileItem: TodoFileModel) {
        lifecycleScope.launch {
            try {
                val updatedFile = fileItem.copy(isFavorite = true)

                // Проверяем, из локального ли файла или из Firebase
                if (fileItem.id.startsWith("/")) {
                    // Локальный файл - сохраняем в избранное локально
                    val index = localDrawings.indexOfFirst { it.id == fileItem.id }
                    if (index != -1) {
                        (localDrawings as MutableList)[index] = updatedFile
                        loadAllDrawings()
                    }
                    Toast.makeText(requireContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                } else {
                    // Файл из Firebase
                    firebaseService.updateFile(updatedFile)
                    val index = allFiles.indexOfFirst { it.id == fileItem.id }
                    if (index != -1) {
                        (allFiles as MutableList)[index] = updatedFile
                    }
                    loadPersonalFiles()
                    Toast.makeText(requireContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeFromFavorites(fileItem: TodoFileModel) {
        lifecycleScope.launch {
            try {
                val updatedFile = fileItem.copy(isFavorite = false)

                // Проверяем, из локального ли файла или из Firebase
                if (fileItem.id.startsWith("/")) {
                    // Локальный файл
                    val index = localDrawings.indexOfFirst { it.id == fileItem.id }
                    if (index != -1) {
                        (localDrawings as MutableList)[index] = updatedFile
                        loadAllDrawings()
                    }
                    Toast.makeText(requireContext(), "Убрано из избранного", Toast.LENGTH_SHORT).show()
                } else {
                    // Файл из Firebase
                    firebaseService.updateFile(updatedFile)
                    val index = allFiles.indexOfFirst { it.id == fileItem.id }
                    if (index != -1) {
                        (allFiles as MutableList)[index] = updatedFile
                    }
                    loadPersonalFiles()
                    Toast.makeText(requireContext(), "Убрано из избранного", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareFile(fileItem: TodoFileModel) {
        try {
            val fileUrl = fileItem.fileUrl
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType(fileItem.name)
                putExtra(Intent.EXTRA_TEXT, fileUrl)
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться файлом"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось поделиться", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareLocalFile(fileItem: TodoFileModel) {
        try {
            val file = File(fileItem.fileUrl)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType(fileItem.name)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться изображением"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось поделиться", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFile(fileItem: TodoFileModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить файл")
            .setMessage("Вы уверены, что хотите удалить ${fileItem.name}?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    try {
                        firebaseService.deleteFile(fileItem.id)
                        allFiles = allFiles.filter { it.id != fileItem.id }
                        loadPersonalFiles()
                        Toast.makeText(requireContext(), "Файл удален", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Не удалось удалить файл", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteLocalFile(fileItem: TodoFileModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить файл")
            .setMessage("Вы уверены, что хотите удалить ${fileItem.name}?")
            .setPositiveButton("Удалить") { _, _ ->
                val file = File(fileItem.fileUrl)
                if (file.delete()) {
                    localDrawings = localDrawings.filter { it.id != fileItem.id }
                    loadAllDrawings()
                    Toast.makeText(requireContext(), "Файл удален", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Не удалось удалить файл", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}