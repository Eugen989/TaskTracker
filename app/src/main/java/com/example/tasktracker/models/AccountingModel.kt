package com.example.tasktracker.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AccountingModel(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val fileId: String = "",
    val buyerName: String = "",
    val buyerContacts: String = "",
    val price: Double = 0.0,
    val currency: String = "₽",
    val createdBy: String = "",
    @ServerTimestamp val transactionDate: Date? = null,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)