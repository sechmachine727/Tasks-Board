package com.prm.tasksboard.taskboards.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prm.tasksboard.R
import com.prm.tasksboard.taskboards.entity.TaskItem
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

class TaskAdapter(private var tasks: List<TaskItem>, private val onTaskFinished: (TaskItem) -> Unit, private val onTaskStatusChanged: (TaskItem) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(task: TaskItem, onTaskStatusChanged: (TaskItem) -> Unit) {
            val checkBox = itemView.findViewById<CheckBox>(R.id.taskCheckBox)
            val titleView = itemView.findViewById<TextView>(R.id.taskSummaryTextView)
            val expandArrowImageView = itemView.findViewById<ImageView>(R.id.expandArrowImageView)
            val taskDetailsLayout = itemView.findViewById<LinearLayout>(R.id.taskDetailsLayout)
            val descriptionView = itemView.findViewById<TextView>(R.id.taskDescriptionTextView)
            val dueDateView = itemView.findViewById<TextView>(R.id.taskDueDateTextView)
            val priorityView = itemView.findViewById<TextView>(R.id.taskPriorityTextView)

            checkBox.isChecked = task.status == "finished"
            titleView.text = task.title
            descriptionView.text = itemView.context.getString(R.string.task_description, task.description)
            dueDateView.text = itemView.context.getString(R.string.task_due_date, convertTimestampToString(task.dueDate))
            priorityView.text = itemView.context.getString(R.string.task_priority, task.priority)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val newStatus = if (isChecked) "finished" else "pending"
                if (task.status != newStatus) {
                    task.status = newStatus
                    onTaskStatusChanged(task)
                    Log.d("TaskStatusChange", "Task ID: ${task.taskId}, New Status: $newStatus")
                }
            }
            expandArrowImageView.setOnClickListener {
                if (taskDetailsLayout.visibility == View.GONE) {
                    taskDetailsLayout.visibility = View.VISIBLE
                    expandArrowImageView.setImageResource(R.drawable.ic_arrow_down)
                } else {
                    taskDetailsLayout.visibility = View.GONE
                    expandArrowImageView.setImageResource(R.drawable.ic_arrow_right)
                }
            }
        }

        private fun convertTimestampToString(timestamp: Timestamp): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(timestamp.toDate())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], onTaskStatusChanged)
    }

    override fun getItemCount() = tasks.size
}