package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId

data class TodoFileTypeModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val type: Int = 1
) {
    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_MUSIC = 2
        const val TYPE_DOCUMENT = 3
        const val TYPE_PDF = 4
        const val TYPE_ARCHIVE = 5
        const val TYPE_ACCOUNTING = 6
    }
}