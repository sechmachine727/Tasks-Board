package com.prm.tasksboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class TaskManagementFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskList: MutableList<TaskItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.task_management, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTasks)
        val addTaskFab = view.findViewById<FloatingActionButton>(R.id.addTaskFab)

        taskList = mutableListOf(
            TaskItem("Task 1", "Description 1"),
            TaskItem("Task 2", "Description 2")
        )

        taskAdapter = TaskAdapter(taskList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter

        addTaskFab.setOnClickListener {
            val newTask = TaskItem("New Task", "New Description")
            taskList.add(newTask)
            taskAdapter.notifyItemInserted(taskList.size - 1)
            Snackbar.make(view, "New task added", Snackbar.LENGTH_SHORT).show()
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                taskList.removeAt(position)
                taskAdapter.notifyItemRemoved(position)
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return view
    }
}
