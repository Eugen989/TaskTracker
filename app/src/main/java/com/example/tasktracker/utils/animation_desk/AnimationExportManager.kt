package com.example.tasktracker.APIs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

class AnimationExportManager(private val context: Context) {

    fun exportToGif(animationFileUri: Uri, onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        exportViaAnimationDesk(animationFileUri, "image/gif", onSuccess, onError)
    }

    fun exportToMp4(animationFileUri: Uri, onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        exportViaAnimationDesk(animationFileUri, "video/mp4", onSuccess, onError)
    }

    private fun exportViaAnimationDesk(animationFileUri: Uri, mimeType: String,
                                       onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(animationFileUri, "application/adpackage")
            putExtra("export_format", mimeType)
            putExtra("export_quality", "high")
        }

        (context as? Activity)?.let { activity ->
            activity.startActivityForResult(intent, REQUEST_CODE_EXPORT)
        }
    }

    companion object {
        const val REQUEST_CODE_EXPORT = 1001
    }
}