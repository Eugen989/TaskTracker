package com.example.tasktracker.services

import android.util.Log
import com.example.tasktracker.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseService {
    private val TAG = "FirebaseService"

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    // Коллекции
    private val usersCollection = db.collection("Users")
    private val tasksCollection = db.collection("Tasks")
    private val filesCollection = db.collection("Files")
    private val commentsCollection = db.collection("Comments")
    private val todoTypesCollection = db.collection("TodoTypes")
    private val importanceCollection = db.collection("Importance")
    private val fileTypesCollection = db.collection("FileTypes")
    private val accountingCollection = db.collection("Accounting")

    private val plansCollection = db.collection("Plans")
    private val prioritiesCollection = db.collection("Priorities")

    // ========== РАБОТА С ПОЛЬЗОВАТЕЛЯМИ ==========

    suspend fun createUser(userModel: UserModel): String {
        val userId = userModel.id ?: ""
        val docRef = if (userId.isNotEmpty()) {
            usersCollection.document(userId)
        } else {
            Log.d(TAG, "createUser error")
            usersCollection.document()
        }
        docRef.set(userModel).await()
        return docRef.id
    }

    suspend fun getUserById(userId: String): UserModel? {
        return try {
            usersCollection.document(userId).get().await().toObject(UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByLogin(login: String): UserModel? {
        return try {
            usersCollection
                .whereEqualTo("login", login)
                .get()
                .await()
                .firstOrNull()
                ?.toObject(UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(userModel: UserModel) {
        val userId = userModel.id ?: ""
        val docRef = if (userId.isNotEmpty()) {
            usersCollection.document(userId).set(userModel).await()
        } else {
            Log.d(TAG, "updateUser error")
        }
    }

    // ========== РАБОТА С ЗАДАЧАМИ ==========

    suspend fun createTask(task: TodoTaskModel): String {
        val docRef = tasksCollection.document()
        val taskWithId = task.copy(id = docRef.id)
        docRef.set(taskWithId).await()
        return docRef.id
    }

    suspend fun getTasksByUser(userId: String): List<TodoTaskModel> {
        return tasksCollection
            .whereEqualTo("userId", userId)
            .orderBy("dataTimeStart", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(TodoTaskModel::class.java)
    }

    suspend fun getTaskById(taskId: String): TodoTaskModel? {
        return tasksCollection.document(taskId).get().await().toObject(TodoTaskModel::class.java)
    }

    suspend fun updateTask(task: TodoTaskModel) {
        tasksCollection.document(task.id).set(task).await()
    }

    suspend fun deleteTask(taskId: String) {
        deleteTaskRelatedData(taskId)
        tasksCollection.document(taskId).delete().await()
    }

    private suspend fun deleteTaskRelatedData(taskId: String) {
        val files = getFilesByTask(taskId)
        files.forEach { file ->
            deleteFileFromStorage(file.fileUrl)
            filesCollection.document(file.id).delete().await()
        }

        val comments = getCommentsByTask(taskId)
        comments.forEach { comment ->
            commentsCollection.document(comment.id).delete().await()
        }
    }

    // ========== РАБОТА С ФАЙЛАМИ ==========

    suspend fun uploadFile(taskId: String, fileName: String, fileTypeId: String, data: ByteArray): TodoFileModel {
        val fileRef = storage.child("tasks/$taskId/$fileName")
        fileRef.putBytes(data).await()
        val downloadUrl = fileRef.downloadUrl.await().toString()

        val file = TodoFileModel(
            name = fileName,
            todoId = taskId,
            fileTypeId = fileTypeId,
            fileUrl = downloadUrl
        )

        val docRef = filesCollection.document()
        val fileWithId = file.copy(id = docRef.id)
        docRef.set(fileWithId).await()

        return fileWithId
    }

    suspend fun updateFile(fileModel: TodoFileModel) {
        filesCollection.document(fileModel.id).set(fileModel).await()
    }

    suspend fun deleteFile(fileId: String) {
        filesCollection.document(fileId).delete().await()
    }

    suspend fun getFilesByTask(taskId: String): List<TodoFileModel> {
        return filesCollection
            .whereEqualTo("todoId", taskId)
            .get()
            .await()
            .toObjects(TodoFileModel::class.java)
    }

    private suspend fun deleteFileFromStorage(fileUrl: String) {
        try {
            val fileRef = storage.child(fileUrl)
            fileRef.delete().await()
        } catch (e: Exception) {
            // Файл мог быть уже удален
        }
    }

    // ========== РАБОТА С УЧЕТОМ ПРОДАЖ (ACCOUNTING) ==========

    suspend fun createAccounting(accounting: AccountingModel): String {
        val docRef = accountingCollection.document()
        val accountingWithId = accounting.copy(id = docRef.id)
        docRef.set(accountingWithId).await()
        return docRef.id
    }

    suspend fun getAccountingById(accountingId: String): AccountingModel? {
        return try {
            accountingCollection.document(accountingId).get().await().toObject(AccountingModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAccountingByUser(userId: String): List<AccountingModel> {
        return try {
            val result = accountingCollection
                .whereEqualTo("createdBy", userId)
                .orderBy("transactionDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val accountingList = result.toObjects(AccountingModel::class.java)
            Log.d(TAG, "Found ${accountingList.size} accounting records for user: $userId")
            accountingList
        } catch (e: Exception) {
            Log.e(TAG, "Error getting accounting: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAccountingByFile(fileId: String): List<AccountingModel> {
        return try {
            accountingCollection
                .whereEqualTo("fileId", fileId)
                .get()
                .await()
                .toObjects(AccountingModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateAccounting(accounting: AccountingModel) {
        val updates = mutableMapOf<String, Any>(
            "title" to accounting.title,
            "description" to accounting.description,
            "fileId" to accounting.fileId,
            "buyerName" to accounting.buyerName,
            "buyerContacts" to accounting.buyerContacts,
            "price" to accounting.price,
            "currency" to accounting.currency,
            "updatedAt" to Date()
        )
        accountingCollection.document(accounting.id).update(updates).await()
    }

    suspend fun deleteAccounting(accountingId: String) {
        accountingCollection.document(accountingId).delete().await()
    }

    suspend fun getAccountingWithFile(accountingId: String): Pair<AccountingModel, TodoFileModel?> {
        val accounting = getAccountingById(accountingId) ?: return Pair(AccountingModel(), null)
        val file = if (accounting.fileId.isNotEmpty()) {
            getFileById(accounting.fileId)
        } else null
        return Pair(accounting, file)
    }

    suspend fun getAllAccountingWithFiles(userId: String): List<Pair<AccountingModel, TodoFileModel?>> {
        Log.d(TAG, "=== getAllAccountingWithFiles START ===")
        Log.d(TAG, "userId: $userId")

        val accountingList = getAccountingByUser(userId)
        Log.d(TAG, "getAccountingByUser returned ${accountingList.size} records")

        val result = mutableListOf<Pair<AccountingModel, TodoFileModel?>>()

        for (accounting in accountingList) {
            Log.d(TAG, "Processing accounting: id=${accounting.id}, title=${accounting.title}, fileId=${accounting.fileId}")
            val file = if (accounting.fileId.isNotEmpty()) {
                getFileById(accounting.fileId)
            } else null
            result.add(Pair(accounting, file))
        }

        Log.d(TAG, "Returning ${result.size} pairs")
        Log.d(TAG, "=== getAllAccountingWithFiles END ===")
        return result
    }

    suspend fun getFileById(fileId: String): TodoFileModel? {
        return try {
            filesCollection.document(fileId).get().await().toObject(TodoFileModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ========== РАБОТА С КОММЕНТАРИЯМИ ==========

    suspend fun addComment(commentModel: CommentModel): String {
        val docRef = commentsCollection.document()
        val commentWithId = commentModel.copy(id = docRef.id)
        docRef.set(commentWithId).await()
        return docRef.id
    }

    suspend fun getCommentsByTask(taskId: String): List<CommentModel> {
        return commentsCollection
            .whereEqualTo("todoId", taskId)
            .orderBy("dataTime", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(CommentModel::class.java)
    }

    suspend fun updateComment(commentModel: CommentModel) {
        commentModel.dataUpdate?.let {
            commentsCollection.document(commentModel.id).update("text", commentModel.text, "dataUpdate", Date())
        } ?: run {
            commentsCollection.document(commentModel.id).update("text", commentModel.text)
        }
    }

    suspend fun deleteComment(commentId: String) {
        commentsCollection.document(commentId).delete().await()
    }

    // ========== РАБОТА СО СПРАВОЧНИКАМИ ==========

    suspend fun getTodoTypes(): List<TodoTypeModel> {
        return todoTypesCollection.get().await().toObjects(TodoTypeModel::class.java)
    }

    suspend fun getImportanceLevels(): List<ImportanceModel> {
        return importanceCollection.orderBy("level", Query.Direction.ASCENDING).get().await().toObjects(ImportanceModel::class.java)
    }

    suspend fun getFileTypes(): List<TodoFileTypeModel> {
        return fileTypesCollection.get().await().toObjects(TodoFileTypeModel::class.java)
    }

    suspend fun getImportanceById(importanceId: String): ImportanceModel? {
        return importanceCollection.document(importanceId).get().await().toObject(ImportanceModel::class.java)
    }

    suspend fun getTodoTypeById(typeId: String): TodoTypeModel? {
        return todoTypesCollection.document(typeId).get().await().toObject(TodoTypeModel::class.java)
    }

    suspend fun initializeReferenceData() {
        try {
            val todoTypesSnapshot = todoTypesCollection.get().await()
            if (todoTypesSnapshot.isEmpty) {
                val todoTypesListModels = TodoTypeModel.getDefaults()
                todoTypesListModels.forEach { todoTypesCollection.add(it).await() }
            }

            val fileTypesSnapshot = fileTypesCollection.get().await()
            if (fileTypesSnapshot.isEmpty) {
                val fileTypesList = listOf(
                    TodoFileTypeModel(name = "Изображение", type = 1),
                    TodoFileTypeModel(name = "Музыка", type = 2)
                )
                fileTypesList.forEach { fileTypesCollection.add(it).await() }
            }

            initializePriorities()
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error initializing reference data: ${e.message}")
            throw e
        }
    }

    // ========== РАБОТА С ПРИОРИТЕТАМИ ==========

    suspend fun getPriorities(): List<PriorityModel> {
        return try {
            prioritiesCollection
                .orderBy("level", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(PriorityModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun initializePriorities() {
        val prioritiesSnapshot = prioritiesCollection.get().await()
        if (prioritiesSnapshot.isEmpty) {
            val prioritiesList = PriorityModel.getDefaults()
            prioritiesList.forEach { prioritiesCollection.add(it).await() }
        }
    }

    // ========== РАБОТА С ТИПАМИ ЗАДАЧ ==========

    suspend fun createTodoType(todoType: TodoTypeModel): String {
        val docRef = todoTypesCollection.document()
        val todoTypeWithId = todoType.copy(id = docRef.id)
        docRef.set(todoTypeWithId).await()
        return docRef.id
    }

    suspend fun createPriority(priority: PriorityModel): String {
        val docRef = prioritiesCollection.document()
        val priorityWithId = priority.copy(id = docRef.id)
        docRef.set(priorityWithId).await()
        return docRef.id
    }

    // ========== РАБОТА С ПЛАНАМИ (PROJECTS) ==========

    suspend fun createPlan(plan: PlanModel): String {
        val docRef = plansCollection.document()
        val planWithId = plan.copy(id = docRef.id)

        Log.d("FirebaseService", "=== Creating Plan ===")
        Log.d("FirebaseService", "Plan ID: ${planWithId.id}")
        Log.d("FirebaseService", "Plan Name: ${planWithId.name}")
        Log.d("FirebaseService", "todoTypeIdList: ${planWithId.todoTypeIdList}")
        Log.d("FirebaseService", "priorityIdList: ${planWithId.priorityIdList}")
        Log.d("FirebaseService", "userIdList: ${planWithId.userIdList}")
        Log.d("FirebaseService", "createdBy: ${planWithId.createdBy}")

        docRef.set(planWithId).await()
        return docRef.id
    }

    suspend fun getPlanById(planId: String): PlanModel? {
        return try {
            plansCollection.document(planId).get().await().toObject(PlanModel::class.java)  // ← используем plansCollection
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPlansByUser(userId: String): List<PlanModel> {
        return try {
            val result = plansCollection
                .whereArrayContains("userIdList", userId)
                .get()
                .await()
            result.toObjects(PlanModel::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error getting plans: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPlansCreatedByUser(userId: String): List<PlanModel> {
        return try {
            plansCollection
                .whereEqualTo("createdBy", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(PlanModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updatePlan(plan: PlanModel) {
        val updates = mutableMapOf<String, Any>(
            "name" to plan.name,
            "description" to plan.description,
            "userIdList" to plan.userIdList,
            "todoIdList" to plan.todoIdList,
            "importanceIdList" to plan.importanceIdList,
            "todoTypeIdList" to plan.todoTypeIdList,
            "priorityIdList" to plan.priorityIdList,
            "updatedAt" to Date()
        )
        plansCollection.document(plan.id).update(updates).await()
    }

    suspend fun deletePlan(planId: String) {
        plansCollection.document(planId).delete().await()
    }

    suspend fun addUserToPlan(planId: String, userId: String) {
        val plan = getPlanById(planId)
        if (plan != null) {
            val updatedUserList = plan.userIdList.toMutableList()
            if (!updatedUserList.contains(userId)) {
                updatedUserList.add(userId)
                plansCollection.document(planId).update("userIdList", updatedUserList).await()
            }
        }
    }

    suspend fun addTaskToPlan(planId: String, taskId: String) {
        val plan = getPlanById(planId)
        if (plan != null) {
            val updatedTaskList = plan.todoIdList.toMutableList()
            if (!updatedTaskList.contains(taskId)) {
                updatedTaskList.add(taskId)
                plansCollection.document(planId).update("todoIdList", updatedTaskList).await()
            }
        }
    }

    suspend fun getTasksByPlan(planId: String): List<TodoTaskModel> {
        return try {
            val query = tasksCollection.whereEqualTo("planId", planId)

            val snapshot = query.get().await()

            val tasks = snapshot.toObjects(TodoTaskModel::class.java)

            tasks.forEach { task ->
                Log.d("FirebaseService", "  - Task ID: ${task.id}, Title: ${task.title}, PlanId: ${task.planId}")
            }

            tasks
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error getting tasks by plan: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPlanUsers(planId: String): List<UserModel> {
        return try {
            val plan = getPlanById(planId) ?: return emptyList()
            val users = mutableListOf<UserModel>()
            for (userId in plan.userIdList) {
                val user = getUserById(userId)
                user?.let { users.add(it) }
            }
            users
        } catch (e: Exception) {
            emptyList()
        }
    }
}