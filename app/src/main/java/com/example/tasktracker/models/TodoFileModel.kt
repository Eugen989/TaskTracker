package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TodoFileModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val todoId: String = "",
    val fileTypeId: String = "",
    val fileUrl: String = "",
    val isFavorite: Boolean = false,
    @ServerTimestamp val uploadedAt: Date? = null
)