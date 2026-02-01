package com.example.tasktracker.models

import com.google.gson.annotations.SerializedName

data class TodoTask (
    @SerializedName("Id") val id: Int,
    @SerializedName("Title") val title: String,
    @SerializedName("Description") val description: String,
    @SerializedName("Importance") val Importance: Int,
    @SerializedName("DataTimeStart") val dataTimeStart: String,
    @SerializedName("DataTimeEnd") val dataTimeEnd: String,
    @SerializedName("FilesList") val filesList: List<TodoFile>,
)

data class TodoFile(
    @SerializedName("Id") val id: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("FileType") val fileType: Int,
    @SerializedName("FileUrl") val fileUrl: String,
)