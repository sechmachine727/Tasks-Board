package com.prm.tasksboard.taskboards.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prm.tasksboard.databinding.ActivityTaskDetailBinding

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve task details passed from the previous Activity
        val taskName = intent.getStringExtra("taskName")
        val taskDescription = intent.getStringExtra("taskDescription")
        val taskDueDate = intent.getStringExtra("taskDueDate")
        val taskPriority = intent.getStringExtra("taskPriority")

        // Set the task details to TextViews
        binding.taskNameTextView.text = taskName
        binding.taskDescriptionTextView.text = taskDescription
        binding.taskDueDateTextView.text = taskDueDate
        binding.taskPriorityTextView.text = taskPriority
    }
}