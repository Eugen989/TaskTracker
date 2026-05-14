package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TodoTaskModel(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val todoTypeId: String = "",
    val priorityId: String = "",
    val userId: String = "",
    val planId: String = "",
    val isCompleted: Boolean = false,
    val status: String = STATUS_PENDING,
    @ServerTimestamp val dataTimeStart: Date? = null,
    @ServerTimestamp val dataTimeEnd: Date? = null,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
    }
}