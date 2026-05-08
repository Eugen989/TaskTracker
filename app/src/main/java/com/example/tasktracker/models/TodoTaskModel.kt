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
    val dataTimeStart: Date? = null,
    val dataTimeEnd: Date? = null,
    @ServerTimestamp val createdAt: Date? = null
)