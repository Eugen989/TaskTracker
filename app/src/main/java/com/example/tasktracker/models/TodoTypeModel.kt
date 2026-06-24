package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId

data class TodoTypeModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val color: String = "#2196F3"
) {
    companion object {
        val TASK = TodoTypeModel(name = "Задача", color = "#2196F3")
        val SKETCH = TodoTypeModel(name = "Скетч", color = "#9C27B0")
        val DRAWING = TodoTypeModel(name = "Рисунок", color = "#FF9800")
        val PAINT = TodoTypeModel(name = "Покрас", color = "#4CAF50")

        fun getDefaults(): List<TodoTypeModel> {
            return listOf(TASK, SKETCH, DRAWING, PAINT)
        }
    }
}