package com.prm.tasksboard.taskboards.entity

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class TaskItem(
    @get:PropertyName("task_id") @set:PropertyName("task_id") var taskId: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "",
    @get:PropertyName("due_date") @set:PropertyName("due_date") var dueDate: String = "",
    @get:PropertyName("priority") @set:PropertyName("priority") var priority: String = "",
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Timestamp? = null,
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String = "",
    @get:PropertyName("board_id") @set:PropertyName("board_id") var boardId: String = "" // Add this field
)