package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId

data class PriorityModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val level: Int = 0,
    val color: String = "#9E9E9E"
) {
    companion object {
        val HIGH = PriorityModel(name = "Важно", level = 1, color = "#F44336")
        val MEDIUM = PriorityModel(name = "Средне", level = 2, color = "#FFC107")
        val LOW = PriorityModel(name = "Не важно", level = 3, color = "#4CAF50")

        fun getDefaults(): List<PriorityModel> {
            return listOf(HIGH, MEDIUM, LOW)
        }
    }
}