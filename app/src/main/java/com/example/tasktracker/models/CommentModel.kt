package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommentModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val todoId: String = "",
    val text: String = "",
    @ServerTimestamp val dataTime: Date? = null,
    @ServerTimestamp val dataUpdate: Date? = null
)