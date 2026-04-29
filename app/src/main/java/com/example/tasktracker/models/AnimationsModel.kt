package com.example.tasktracker.models

data class AnimationsModel(
    val id: String,
    val name: String,
    val taskId: String,
    val adPackageUri: String,
    val exportedVideoUri: String?,
    val cloudField: String?
)

data class KdanCloudResponse(
    val fileId: String,
    val downloadUri: String,
    val uploadUri: String
)
