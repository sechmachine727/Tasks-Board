package com.prm.tasksboard.taskboards.entity

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class TaskItem(
    var taskId: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "",
    @get:PropertyName("due_date") @set:PropertyName("due_date") var dueDate: Timestamp,
    @get:PropertyName("priority") @set:PropertyName("priority") var priority: String = "",
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Timestamp,
) {
    constructor() : this("", "", "", "", Timestamp.now(), "", Timestamp.now())
}