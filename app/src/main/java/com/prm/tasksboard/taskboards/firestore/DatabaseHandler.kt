package com.prm.tasksboard.taskboards.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prm.tasksboard.taskboards.entity.BoardItem

class DatabaseHandler {
    private val db = Firebase.firestore
    private val loggedInUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun addBoardItem(boardItem: BoardItem) {
        // Firestore generates a unique ID for the new document
        val docRef = db.collection("boards").document()
        boardItem.boardId = docRef.id // Assign the Firestore-generated ID to the boardItem

        docRef.set(boardItem)
            .addOnSuccessListener {
                Log.d("FirestoreAdd", "BoardItem successfully added with ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreAdd", "Error adding BoardItem", e)
            }
    }

    fun getBoardItems() {
        db.collection("boards")
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
        db.collection("boards") // Replace "known_collection" with your actual collection name
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
}
