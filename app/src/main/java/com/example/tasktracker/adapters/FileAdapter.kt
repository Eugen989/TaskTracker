package com.example.tasktracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.databinding.ItemFileBinding
import com.example.tasktracker.models.TodoFileModel
import com.example.tasktracker.models.TodoFileTypeModel
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private val onFileClick: (TodoFileModel) -> Unit,
    private val onMenuClick: (TodoFileModel) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var items: List<TodoFileModel> = emptyList()

    fun submitList(newItems: List<TodoFileModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    private fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf(".")
        return if (lastDot > 0) fileName.substring(lastDot + 1).uppercase() else "FILE"
    }

    private fun getFileTypeDisplay(type: String): String {
        return when (type.uppercase()) {
            "PNG", "JPG", "JPEG", "GIF", "BMP", "WEBP" -> "Изобр"
            "MP3", "WAV", "FLAC", "AAC", "OGG" -> "Аудио"
            "PDF" -> "PDF"
            "DOC", "DOCX" -> "Word"
            "XLS", "XLSX" -> "Excel"
            "PPT", "PPTX" -> "PowerPoint"
            "TXT" -> "Текст"
            "ZIP", "RAR", "7Z" -> "Архив"
            else -> type.uppercase().take(3)
        }
    }

    inner class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fileItem: TodoFileModel) {
            val fileName = fileItem.name
            val fileExtension = getFileExtension(fileName)

            binding.tvFileName.text = fileName
            binding.tvFileType.text = getFileTypeDisplay(fileExtension)

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            fileItem.uploadedAt?.let {
                binding.tvFileDate.text = dateFormat.format(it)
            } ?: run {
                binding.tvFileDate.text = "Дата неизвестна"
            }

            binding.root.setOnClickListener {
                onFileClick(fileItem)
            }

            binding.btnMenu.setOnClickListener {
                onMenuClick(fileItem)
            }
        }
    }
}