package com.example.tasktracker.APIs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

// AnimationDeskIntentHelper.kt
class AnimationDeskIntentHelper(private val context: Context) {

    fun openAdPackageForEditing(adPackageUri: Uri): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_EDIT).apply {
                setDataAndType(adPackageUri, "application/adpackage")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Проверяем, установлено ли приложение
            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                openInGooglePlay()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createNewAnimationForTask(taskId: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                setType("application/adpackage")
                putExtra(Intent.EXTRA_TITLE, "task_${taskId}_animation.adpackage")
            }
            (context as? Activity)?.startActivityForResult(intent, REQUEST_CODE_CREATE)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun openInGooglePlay() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.kdanmobile.android.animationdesk")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.kdanmobile.android.animationdesk")
            }
            context.startActivity(intent)
        }
    }

    companion object {
        const val REQUEST_CODE_CREATE = 1002
    }
}