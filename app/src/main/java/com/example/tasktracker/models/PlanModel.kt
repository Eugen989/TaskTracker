package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PlanModel (
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val userIdList: List<String> = emptyList(),
    val todoIdList: List<String> = emptyList(),
    val todoTypeIdList: List<String> = emptyList(),
    val priorityIdList: List<String> = emptyList(),
    val createdBy: String = "",
    val importanceIdList: List<String> = emptyList(), // не используемое поле
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)