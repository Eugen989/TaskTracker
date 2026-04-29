package com.example.tasktracker.fragments.MainMenu

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.databinding.FragmentMainMenuAccountingBinding
import com.example.tasktracker.databinding.ItemAnimationProjectBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Data class for animation project
data class AnimationProject(
    val id: String,
    val name: String,
    val taskId: String,
    val localFilePath: String?,
    var cloudFileId: String?,
    val exportFormat: String?,
    val createdAt: Long
)

// Adapter for animation projects list
class AnimationProjectAdapter(
    private val projects: MutableList<AnimationProject>,
    private val onOpenClick: (AnimationProject) -> Unit,
    private val onExportClick: (AnimationProject) -> Unit,
    private val onDeleteClick: (AnimationProject) -> Unit,
    private val onSyncClick: (AnimationProject) -> Unit
) : RecyclerView.Adapter<AnimationProjectAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAnimationProjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnimationProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = projects[position]
        holder.binding.tvProjectName.text = project.name
        holder.binding.tvExportFormat.text = project.exportFormat ?: "Not exported"
        holder.binding.btnOpen.setOnClickListener { onOpenClick(project) }
        holder.binding.btnExport.setOnClickListener { onExportClick(project) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(project) }
        holder.binding.btnSyncCloud.setOnClickListener { onSyncClick(project) }
    }

    override fun getItemCount(): Int = projects.size
}

class MainMenuAccountingFragment : Fragment() {
    companion object {
        fun newInstance(): MainMenuAccountingFragment = MainMenuAccountingFragment()
    }

    private val TAG = "MainMenuAccountingFragment"
    private var _binding: FragmentMainMenuAccountingBinding? = null
    private val binding get() = _binding!!

    // Store animation projects associated with tasks
    private val animationProjects = mutableListOf<AnimationProject>()
    private lateinit var projectsAdapter: AnimationProjectAdapter

    // Kdan Cloud API endpoints (replace with actual endpoints from Kdan API documentation)
    private val KDAN_CLOUD_BASE_URL = "https://api.kdancloud.com/v1"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Authentication token - you'll need to implement OAuth flow
    private var kdanAuthToken: String? = null

    // Activity result launchers
    private val exportGifLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleExportResult(result.data)
        }
    }

    private val exportMp4Launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleExportResult(result.data)
        }
    }

    private val openAdPackageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Animation Desk returned with result: ${result.resultCode}")
    }

    private val pickAdPackageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val uri = data?.data
            uri?.let { handleImportedAdPackage(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainMenuAccountingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "init")

        setupRecyclerView()
        setupButtons()
        loadAnimationProjects()
    }

    private fun setupRecyclerView() {
        projectsAdapter = AnimationProjectAdapter(
            projects = animationProjects,
            onOpenClick = { project -> openAnimationInAnimationDesk(project) },
            onExportClick = { project -> showExportOptions(project) },
            onDeleteClick = { project -> deleteAnimationProject(project) },
            onSyncClick = { project -> syncWithKdanCloud(project) }
        )
        binding.rvAnimationProjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAnimationProjects.adapter = projectsAdapter
    }

    private fun setupButtons() {
        binding.btnImportAdPackage.setOnClickListener {
            importAdPackageFromStorage()
        }
        binding.btnUploadToCloud.setOnClickListener {
            uploadCurrentProjectToCloud()
        }
        binding.btnDownloadFromCloud.setOnClickListener {
            downloadProjectsFromCloud()
        }
    }

    private fun loadAnimationProjects() {
        // Load saved animation projects from SharedPreferences or database
        val prefs = requireContext().getSharedPreferences("animation_projects", android.content.Context.MODE_PRIVATE)
        val projectsJson = prefs.getString("projects_list", "[]")
        // Parse and load projects - simplified for demonstration
        Log.d(TAG, "Loading animation projects")
    }

    private fun saveAnimationProjects() {
        val prefs = requireContext().getSharedPreferences("animation_projects", android.content.Context.MODE_PRIVATE)
        val editor = prefs.edit()
        // Save projects list as JSON - simplified for demonstration
        editor.apply()
    }

    // MARK: - Export Functions

    private fun showExportOptions(project: AnimationProject) {
        val options = arrayOf("Export as GIF", "Export as MP4")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Export Animation")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportAnimation(project, "GIF")
                    1 -> exportAnimation(project, "MP4")
                }
            }
            .show()
    }

    private fun exportAnimation(project: AnimationProject, format: String) {
        // Launch Animation Desk with export intent
        // Animation Desk supports export to GIF and MP4 formats [citation:2][citation:5]
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            // Use Animation Desk's package name - verify this with latest Kdan documentation
            setPackage("com.kdanmobile.android.animationdesk")

            // Include export parameters
            putExtra("export_format", format)
            putExtra("source_file", project.localFilePath)
            putExtra("export_quality", "high")
            putExtra("resolution", "1920x1080")
        }

        val launcher = if (format == "GIF") exportGifLauncher else exportMp4Launcher

        try {
            launcher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Animation Desk not installed", e)
            Toast.makeText(requireContext(), "Animation Desk is not installed", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleExportResult(data: Intent?) {
        // Get exported file URI from Animation Desk return intent
        val exportedFileUri = data?.data
        val exportedFilePath = data?.getStringExtra("file_path")

        if (exportedFileUri != null || exportedFilePath != null) {
            saveExportedVideoToDevice(exportedFileUri, exportedFilePath)
        } else {
            Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveExportedVideoToDevice(uri: Uri?, filePath: String?) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "animation_${System.currentTimeMillis()}")
                put(MediaStore.MediaColumns.MIME_TYPE, if (uri.toString().contains("gif")) "image/gif" else "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/TaskTracker")
            }

            val resolver = requireContext().contentResolver
            val newUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            newUri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    if (uri != null) {
                        resolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    } else if (filePath != null) {
                        File(filePath).inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
                Toast.makeText(requireContext(), "Video saved to device", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Video saved successfully: $newUri")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving video", e)
            Toast.makeText(requireContext(), "Failed to save video", Toast.LENGTH_SHORT).show()
        }
    }

    // MARK: - AD.Package Management [citation:3][citation:8][citation:9]

    private fun importAdPackageFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/ad-package"
            // Also try to accept .adpackage files
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/ad-package", "application/octet-stream"))
        }
        pickAdPackageLauncher.launch(intent)
    }

    private fun handleImportedAdPackage(uri: Uri) {
        try {
            val fileName = "imported_${System.currentTimeMillis()}.adpackage"
            val destFile = File(requireContext().filesDir, "ad_packages/$fileName")
            destFile.parentFile?.mkdirs()

            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Create project entry
            val project = AnimationProject(
                id = UUID.randomUUID().toString(),
                name = fileName,
                taskId = getCurrentTaskId(), // You need to implement this based on your task tracker
                localFilePath = destFile.absolutePath,
                cloudFileId = null,
                exportFormat = null,
                createdAt = System.currentTimeMillis()
            )

            animationProjects.add(project)
            projectsAdapter.notifyItemInserted(animationProjects.size - 1)
            saveAnimationProjects()

            Toast.makeText(requireContext(), "AD.Package imported successfully", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            Log.e(TAG, "Error importing AD.Package", e)
            Toast.makeText(requireContext(), "Failed to import AD.Package", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAnimationInAnimationDesk(project: AnimationProject) {
        val file = File(project.localFilePath ?: return)
        if (!file.exists()) {
            Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Use FileProvider for Android 7+ [citation:3]
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/ad-package")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.kdanmobile.android.animationdesk") // Verify this package name
        }

        try {
            openAdPackageLauncher.launch(intent)
            // Track which task this project is bound to [citation:1][citation:4]
            logProjectTaskAssociation(project)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open Animation Desk", e)
            Toast.makeText(requireContext(), "Animation Desk not found", Toast.LENGTH_LONG).show()
        }
    }

    private fun logProjectTaskAssociation(project: AnimationProject) {
        // Track which animation project is bound to which task [citation:1][citation:4]
        Log.d(TAG, "Project '${project.name}' (ID: ${project.id}) opened for Task ID: ${project.taskId}")
        // You can store this association in your local database
        val prefs = requireContext().getSharedPreferences("project_task_association", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString(project.id, project.taskId).apply()
    }

    private fun deleteAnimationProject(project: AnimationProject) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Project")
            .setMessage("Delete '${project.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete local file
                project.localFilePath?.let { filePath ->
                    File(filePath).delete()
                }
                animationProjects.remove(project)
                projectsAdapter.notifyDataSetChanged()
                saveAnimationProjects()
                Toast.makeText(requireContext(), "Project deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // MARK: - Kdan Cloud API Integration [citation:1][citation:4][citation:7]

    private fun uploadCurrentProjectToCloud() {
        val currentProject = getCurrentSelectedProject()
        if (currentProject == null) {
            Toast.makeText(requireContext(), "No project selected", Toast.LENGTH_SHORT).show()
            return
        }

        uploadFileToKdanCloud(currentProject)
    }

    private fun uploadFileToKdanCloud(project: AnimationProject) {
        val file = File(project.localFilePath ?: return)
        if (!file.exists()) {
            Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Kdan Cloud API endpoint for file upload
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/ad-package".toMediaTypeOrNull()))
            .addFormDataPart("project_id", project.id)
            .addFormDataPart("task_id", project.taskId)
            .build()

        val request = Request.Builder()
            .url("$KDAN_CLOUD_BASE_URL/files/upload")
            .addHeader("Authorization", "Bearer $kdanAuthToken")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Upload failed", e)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        val json = JSONObject(responseBody ?: "{}")
                        val fileId = json.optString("file_id")

                        // Update project with cloud file ID
                        project.cloudFileId = fileId
                        saveAnimationProjects()

                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Uploaded to Kdan Cloud", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "Upload response error: ${it.code}")
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Upload failed: ${it.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun downloadProjectsFromCloud() {
        if (kdanAuthToken == null) {
            Toast.makeText(requireContext(), "Please login to Kdan Cloud first", Toast.LENGTH_SHORT).show()
            // Trigger Kdan Cloud login flow
            loginToKdanCloud()
            return
        }

        val request = Request.Builder()
            .url("$KDAN_CLOUD_BASE_URL/projects/list")
            .addHeader("Authorization", "Bearer $kdanAuthToken")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Download list failed", e)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to fetch cloud projects", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        handleCloudProjectsList(responseBody)
                    }
                }
            }
        })
    }

    private fun handleCloudProjectsList(responseBody: String?) {
        val json = JSONObject(responseBody ?: "{\"projects\":[]}")
        val projectsArray = json.optJSONArray("projects") ?: org.json.JSONArray()

        for (i in 0 until projectsArray.length()) {
            val projectJson = projectsArray.getJSONObject(i)
            val cloudFileId = projectJson.optString("id")
            val fileName = projectJson.optString("name")

            // Download each project file
            downloadFileFromKdanCloud(cloudFileId, fileName)
        }
    }

    private fun downloadFileFromKdanCloud(fileId: String, fileName: String) {
        val request = Request.Builder()
            .url("$KDAN_CLOUD_BASE_URL/files/$fileId/download")
            .addHeader("Authorization", "Bearer $kdanAuthToken")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Download failed for $fileId", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val destFile = File(requireContext().filesDir, "ad_packages/cloud_$fileName")
                        destFile.parentFile?.mkdirs()

                        it.body?.byteStream()?.use { inputStream ->
                            FileOutputStream(destFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        val project = AnimationProject(
                            id = UUID.randomUUID().toString(),
                            name = fileName,
                            taskId = getCurrentTaskId(),
                            localFilePath = destFile.absolutePath,
                            cloudFileId = fileId,
                            exportFormat = null,
                            createdAt = System.currentTimeMillis()
                        )

                        requireActivity().runOnUiThread {
                            animationProjects.add(project)
                            projectsAdapter.notifyItemInserted(animationProjects.size - 1)
                            saveAnimationProjects()
                            Toast.makeText(requireContext(), "Downloaded: $fileName", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun syncWithKdanCloud(project: AnimationProject) {
        if (project.cloudFileId == null) {
            uploadFileToKdanCloud(project)
        } else {
            // Download latest version from cloud
            downloadFileFromKdanCloud(project.cloudFileId!!, project.name)
        }
    }

    private fun loginToKdanCloud() {
        // Implement OAuth flow for Kdan Cloud [citation:1][citation:4]
        // This should open Kdan ID login page
        Toast.makeText(requireContext(), "Kdan Cloud login - implement OAuth flow", Toast.LENGTH_LONG).show()
    }

    // MARK: - Helper Methods

    private fun getCurrentTaskId(): String {
        // TODO: Implement this based on your task tracker's current task context
        // For now, return a placeholder
        return "task_${System.currentTimeMillis()}"
    }

    private fun getCurrentSelectedProject(): AnimationProject? {
        return animationProjects.firstOrNull()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}