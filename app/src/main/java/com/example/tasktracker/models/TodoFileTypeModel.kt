package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId

data class TodoFileTypeModel(
    @DocumentId val id: String = "",
    val name: String = "",
    // 1 - изображение, 2 - музыка
    val type: Int = 1
)