package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserModel (
    @DocumentId
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var login: String? = null,
    var password: String? = null,
    var planIdList: MutableList<String?>? = null,

    @ServerTimestamp var dataTime: Date? = null,

    @ServerTimestamp var dataTimeUpdate: Date? = null,
)