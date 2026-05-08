package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val login: String = "",
    val password: String = "",
    val planIdList: List<String> = emptyList(),
    @ServerTimestamp val dataTime: Date? = null,
    @ServerTimestamp val dataTimeUpdate: Date? = null
)