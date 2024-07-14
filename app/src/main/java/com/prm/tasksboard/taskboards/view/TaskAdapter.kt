package com.prm.tasksboard.taskboards.view

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prm.tasksboard.R
import com.prm.tasksboard.taskboards.entity.TaskItem

class TaskAdapter(private var tasks: List<TaskItem>, private val onTaskFinished: (TaskItem) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(task: TaskItem, onTaskFinished: (TaskItem) -> Unit, context: android.content.Context) {
            val titleView = itemView.findViewById<TextView>(R.id.taskSummaryTextView)
            val expandArrowImageView = itemView.findViewById<ImageView>(R.id.expandArrowImageView)
            val taskDetailsLayout = itemView.findViewById<LinearLayout>(R.id.taskDetailsLayout)

            titleView.text = task.title

            itemView.setOnClickListener {
                val intent = Intent(context, TaskDetailActivity::class.java).apply {
                    putExtra("taskName", task.title)
                    putExtra("taskDescription", task.description)
                    putExtra("taskDueDate", task.dueDate)
                    putExtra("taskPriority", task.priority)
                }
                context.startActivity(intent)
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], onTaskFinished, holder.itemView.context)
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<TaskItem>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}