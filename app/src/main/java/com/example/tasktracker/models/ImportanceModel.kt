package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId

data class ImportanceModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val level: Int = 1
)