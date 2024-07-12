package com.prm.tasksboard.taskboards.entity

data class TaskItem(
    var taskId: String = "",
    var boardId: String = "",
    var title: String = "", // Changed from name to title
    var description: String = "",
    var status: String = "",
    var dueDate: String = "", // Added dueDate field
    var priority: String = "" // Added priority field
)