package com.prm.tasksboard.taskboards.firestore

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prm.tasksboard.taskboards.entity.BoardItem
import com.prm.tasksboard.taskboards.entity.TaskItem

class DatabaseHandler {
    private val db = Firebase.firestore
    private val loggedInUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun addBoardItem(boardItem: BoardItem, callback: (String) -> Unit) {
        val docRef = db.collection("boards").document()
        boardItem.boardId = docRef.id
        boardItem.userId = loggedInUserId
        boardItem.createdAt = Timestamp.now() // Set created_at to current time

        docRef.set(boardItem)
            .addOnSuccessListener {
                callback(docRef.id) // Invoke the callback with the new board's ID
            }
            .addOnFailureListener { e ->
                Log.w("DatabaseHandler", "Error adding BoardItem", e)
            }
    }

    fun getBoardItemsByUserId(): Task<QuerySnapshot> {
        return db.collection("boards")
            .whereEqualTo("user_id", loggedInUserId)
            .orderBy("created_at")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("FirestoreRead", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreRead", "Error getting documents.", exception)
            }
    }

    fun updateBoardItem(boardItemId: String, updatedFields: Map<String, Any>) {
        db.collection("boards").document(boardItemId)
            .update(updatedFields)
            .addOnSuccessListener {
                Log.d(
                    "FirestoreUpdate",
                    "Document $boardItemId successfully updated!"
                )
            }
            .addOnFailureListener { e -> Log.w("FirestoreUpdate", "Error updating document", e) }
    }

    fun deleteBoardItem(boardItemId: String) {
        db.collection("boards").document(boardItemId)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    "FirestoreDelete",
                    "Document $boardItemId successfully deleted!"
                )
            }
            .addOnFailureListener { e -> Log.w("FirestoreDelete", "Error deleting document", e) }
    }

    fun checkFirestoreConnection() {
        db.collection("boards")
            .whereEqualTo("user_id", loggedInUserId).limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirestoreCheck", "Connected to Firestore, but the collection is empty.")
                } else {
                    Log.d("FirestoreCheck", "Successfully connected to Firestore.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreCheck", "Error connecting to Firestore: ", exception)
            }
    }

    fun addTaskItem(taskItem: TaskItem, boardId: String, callback: () -> Unit) {
        val newTaskRef = db.collection("boards").document(boardId).collection("tasks").document()
        taskItem.taskId = newTaskRef.id
        taskItem.userId = loggedInUserId
        taskItem.createdAt = Timestamp.now() // Set created_at to current time

        newTaskRef.set(taskItem)
            .addOnSuccessListener {
                Log.d("FirestoreAdd", "TaskItem successfully added with ID: ${taskItem.taskId}")
                callback()
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreAdd", "Error adding TaskItem", e)
            }
    }

    fun deleteTaskItem(boardId: String, taskId: String, callback: () -> Unit) {
        db.collection("boards").document(boardId).collection("tasks").document(taskId)
            .delete()
            .addOnSuccessListener {
                Log.d("FirestoreDelete", "Task $taskId successfully deleted!")
                callback()
            }
            .addOnFailureListener { e -> Log.w("FirestoreDelete", "Error deleting task", e) }
    }

    fun getTasksByBoardId(boardId: String, callback: (List<TaskItem>) -> Unit) {
        db.collection("boards").document(boardId).collection("tasks")
            .whereEqualTo("user_id", loggedInUserId) // Ensure tasks are for the logged-in user
            .get()
            .addOnSuccessListener { documents ->
                val tasks = documents.toObjects(TaskItem::class.java)
                callback(tasks)
            }
            .addOnFailureListener { exception ->
                Log.w("DatabaseHandler", "Error getting tasks by board ID", exception)
            }
    }
}