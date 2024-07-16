package com.prm.tasksboard.taskboards.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.prm.tasksboard.R
import com.prm.tasksboard.authentication.LoginActivity
import com.prm.tasksboard.taskboards.entity.BoardItem
import com.prm.tasksboard.taskboards.entity.TaskItem
import com.prm.tasksboard.taskboards.firestore.DatabaseHandler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskboardsActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var addBoardButton: Button
    private lateinit var menuButton: MaterialButton
    private lateinit var emptyView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView
    private val boardList = mutableListOf<BoardItem>()
    private val tasks = mutableListOf<TaskItem>()
    private val loggedInUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val dbHandler = DatabaseHandler()
    private var currentBoardId: String? = null
    private var selectedDueDate = ""
    private var selectedPriority = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_taskboards)
        setWindowInsetsListener()
        tabLayout = findViewById(R.id.tabLayout)
        addBoardButton = findViewById(R.id.addTaskButton)
        menuButton = findViewById(R.id.menuButton)
        emptyView = findViewById(R.id.emptyView)
        gridView = findViewById(R.id.boardsGridView)
        recyclerView = findViewById(R.id.tasksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        searchView = findViewById(R.id.searchView)
        setupSearchView()
        taskAdapter = TaskAdapter(tasks, { taskItem ->
            // Handle task finish action here
        }, { taskItem ->
            // Handle task status change here
        }, { taskItem ->
            // Handle edit task action here
            showEditTaskDialog(taskItem)
        }, { taskItem ->
            // Handle delete task action here
            dbHandler.deleteTaskItem(currentBoardId!!, taskItem.taskId) {
                tasks.remove(taskItem)
                taskAdapter.notifyDataSetChanged()
            }
        })
        recyclerView.adapter = taskAdapter

        gridView = GridView(this).apply {
            numColumns = 2
            verticalSpacing = 10
            horizontalSpacing = 10
            setPadding(16, 16, 16, 16) // Add padding
            clipToPadding = false
        }

        dbHandler.checkFirestoreConnection()

        setTabLayoutListeners()
        updateEmptyViewVisibility()

        fetchAndDisplayBoards()

        addBoardButton.setOnClickListener {
            showAddTaskDialog()
        }

        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, menuButton)
            if (boardList.isEmpty()) {
                popupMenu.menuInflater.inflate(
                    R.menu.board_options_menu_without_delete,
                    popupMenu.menu
                )
            } else {
                popupMenu.menuInflater.inflate(R.menu.board_options_menu, popupMenu.menu)
            }
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.add_board -> {
                        createNewBoard()
                        true
                    }

                    R.id.delete_board -> {
                        deleteBoard()
                        true
                    }
                    R.id.change_account -> {
                        triggerChangeAccount()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
        val overviewButton: MaterialButton = findViewById(R.id.overviewButton)
        overviewButton.setOnClickListener {
            showOverviewPopup(currentBoardId)
        }

    }


    private fun setupSearchView() {
        findViewById<ImageButton>(R.id.searchButton).setOnClickListener {
            val isSearchViewVisible = searchView.visibility == View.VISIBLE
            searchView.visibility = if (isSearchViewVisible) View.GONE else View.VISIBLE
            adjustRecyclerViewTopConstraint(!isSearchViewVisible)
            if (!isSearchViewVisible) {
                searchView.requestFocusFromTouch()
                searchView.isIconified = false
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchForTask(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchForTask(it) }
                return true
            }
        })
    }

    private fun adjustRecyclerViewTopConstraint(isSearchViewVisible: Boolean) {
        val layoutParams = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topToBottom = if (isSearchViewVisible) R.id.searchView else R.id.tabLayout
        recyclerView.layoutParams = layoutParams
    }

    private fun searchForTask(query: String) {
        Log.d("TaskSearch", "Searching for tasks with query: $query in board ID: $currentBoardId")
        dbHandler.getTasksByBoardId(currentBoardId!!) { allTasks: List<TaskItem> ->
            val filteredTasks = allTasks.filter { it.title.contains(query, ignoreCase = true) }
            Log.d("TaskSearch", "Found ${filteredTasks.size} tasks matching the query in the current board.")
            tasks.clear()
            tasks.addAll(filteredTasks)
            taskAdapter.notifyDataSetChanged()
        }
    }

    private fun setWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setTabLayoutListeners() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: return
                currentBoardId = boardList[position].boardId
                displayTasks(currentBoardId!!)
                tab.view.setOnLongClickListener {
                    showRenameDialog(position)
                    true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateEmptyViewVisibility() {
        emptyView.visibility = if (boardList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun createNewBoard() {
        // Create a new BoardItem
        val newBoard = BoardItem(
            createdAt = Timestamp.now(),
            name = "New Board #${boardList.size + 1}",
            updatedAt = Timestamp.now(),
            userId = loggedInUserId,
        )
        // Add it to your boardList
        boardList.add(newBoard)
        // Add it to Firestore
        dbHandler.addBoardItem(newBoard) { newBoardId ->
            boardList[boardList.size - 1].boardId = newBoardId
        }
        setupTabLayout()
        updateEmptyViewVisibility()
    }

    private fun deleteBoard() {
        currentBoardId?.let { boardId ->
            val currentItem = boardList.indexOfFirst { it.boardId == boardId }

            // Delete the board from firestore
            dbHandler.deleteBoardItem(boardId)

            // Remove the board from your boardList
            boardList.removeAt(currentItem)

            // Notify the adapter (if any) that the dataset has changed
            tabLayout.removeAllTabs() // Clear existing tabs

            if (boardList.isEmpty()) {
                clearAndHideTaskList() // Clear and hide the task list
            } else {
                // Rebuild tabs
                setupTabLayout()
                displayTasksForCurrentBoard()
            }

            updateEmptyViewVisibility()
        }
    }

    private fun clearAndHideTaskList() {
        // Find the RecyclerView and clear its adapter
        recyclerView.adapter = null
        // Optionally, update visibility or show a placeholder
        emptyView.visibility = View.VISIBLE
    }

    private fun showRenameDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Board")

        val input = EditText(this)
        input.setText(boardList[position].name)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newName = input.text.toString()
            boardList[position].name = newName
            tabLayout.getTabAt(position)?.text = newName
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun fetchAndDisplayBoards() {
        dbHandler.getBoardItemsByUserId().addOnSuccessListener { result ->
            boardList.clear() // Clear existing items if refreshing the entire list
            val newItems = result.map { document ->
                document.toObject(BoardItem::class.java)
            }
                .sortedBy { it.createdAt } // Sort by createdAt timestamp or change it to another attribute like name
            boardList.addAll(newItems)
            setupTabLayout()
            updateEmptyViewVisibility()
        }
    }

    private fun setupTabLayout() {
        tabLayout.removeAllTabs()
        boardList.forEach { board ->
            val tab = tabLayout.newTab().setText(board.name)
            tabLayout.addTab(tab)
        }
        if (boardList.isNotEmpty() && currentBoardId != null) {
            val currentTabIndex = boardList.indexOfFirst { it.boardId == currentBoardId }
            if (currentTabIndex != -1) {
                tabLayout.getTabAt(currentTabIndex)?.select()
            }
        }
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Task")

        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskNameInput = view.findViewById<EditText>(R.id.taskNameInput)
        val taskDescriptionInput = view.findViewById<EditText>(R.id.taskDescriptionInput)
        val dueDateTextView = view.findViewById<TextView>(R.id.dueDateTextView)
        val priorityTextView = view.findViewById<TextView>(R.id.priorityTextView)

        selectedDueDate = ""
        selectedPriority = ""

        dueDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, yearSelected, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(yearSelected, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDueDate = dateFormat.format(selectedDate.time)
                dueDateTextView.text = selectedDueDate
            }, year, month, day).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        priorityTextView.setOnClickListener {
            val popupMenu = PopupMenu(this, priorityTextView)
            popupMenu.menu.add("High")
            popupMenu.menu.add("Medium")
            popupMenu.menu.add("Low")
            popupMenu.setOnMenuItemClickListener { item ->
                selectedPriority = item.title.toString()
                priorityTextView.text = selectedPriority
                true
            }
            popupMenu.show()
        }

        builder.setView(view)

        builder.setPositiveButton("OK") { _, _ ->
            val taskName = taskNameInput.text.toString().trim()
            val taskDescription = taskDescriptionInput.text.toString().trim()

            if (taskName.isEmpty() || taskDescription.isEmpty() || selectedDueDate.isEmpty() || selectedPriority.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                addNewTask(taskName, taskDescription, selectedDueDate, selectedPriority)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }


    private fun addNewTask(taskName: String, taskDescription: String, selectedDueDate: String, selectedPriority: String) {
        if (boardList.isEmpty()) {
            val newBoard = BoardItem(
                createdAt = Timestamp.now(),
                name = "Default Board",
                updatedAt = Timestamp.now(),
                userId = loggedInUserId
            )
            dbHandler.addBoardItem(newBoard) { newBoardId ->
                newBoard.boardId = newBoardId
                runOnUiThread {
                    boardList.add(newBoard)
                    currentBoardId = newBoardId
                    setupTabLayout() // Update TabLayout with the new board
                    tabLayout.getTabAt(tabLayout.tabCount - 1)?.select()
                    tasks.clear()
                    taskAdapter.notifyDataSetChanged()
                    addTaskToBoard(taskName, taskDescription, selectedDueDate, selectedPriority) // Now add the task
                    displayTasks(newBoardId)
                }
            }
        } else {
            addTaskToBoard(taskName, taskDescription, selectedDueDate, selectedPriority)
        }
    }

    private fun addTaskToBoard(taskName: String, taskDescription: String, selectedDueDate: String, selectedPriority: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dueDate = if (selectedDueDate.isNotEmpty()) {
            sdf.parse(selectedDueDate)?.let { Timestamp(it) } ?: Timestamp.now()
        } else {
            Timestamp.now()
        }

        val newTask = TaskItem(
            title = taskName,
            description = taskDescription,
            dueDate = dueDate,
            priority = selectedPriority,
            createdAt = Timestamp.now()
        )

        currentBoardId?.let { boardId ->
            dbHandler.addTaskItem(newTask, boardId) {
                tasks.add(newTask) // Add the new task to the local list
                runOnUiThread {
                    taskAdapter.notifyItemInserted(tasks.size - 1)
                    displayTasks(boardId) // Ensure this method is called to refresh the task list UI
                }
            }
        }
    }

    private fun displayTasksForCurrentBoard() {
        currentBoardId?.let {
            displayTasks(it)
        }
    }

    private fun displayTasks(boardId: String) {
        dbHandler.getTasksByBoardId(boardId) { result ->
            tasks.clear() // Clear existing tasks
            tasks.addAll(result) // Add all fetched tasks
            runOnUiThread {
                taskAdapter.notifyDataSetChanged() // Notify adapter to refresh UI
            }
        }
    }

    private fun showEditTaskDialog(taskItem: TaskItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Task")

        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskNameInput = view.findViewById<EditText>(R.id.taskNameInput)
        val taskDescriptionInput = view.findViewById<EditText>(R.id.taskDescriptionInput)
        val dueDateTextView = view.findViewById<TextView>(R.id.dueDateTextView)
        val priorityTextView = view.findViewById<TextView>(R.id.priorityTextView)

        // Populate dialog with task details
        taskNameInput.setText(taskItem.title)
        taskDescriptionInput.setText(taskItem.description)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dueDateTextView.text = sdf.format(taskItem.dueDate.toDate())
        priorityTextView.text = taskItem.priority

        // Initialize selected values
        selectedDueDate = sdf.format(taskItem.dueDate.toDate())
        selectedPriority = taskItem.priority

        // Due Date Picker
        dueDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                time = taskItem.dueDate.toDate()
            }
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(year, month, dayOfMonth)
                selectedDueDate = sdf.format(newDate.time)
                dueDateTextView.text = selectedDueDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Priority Selection
        priorityTextView.setOnClickListener {
            val popupMenu = PopupMenu(this, priorityTextView)
            popupMenu.menu.add("High")
            popupMenu.menu.add("Medium")
            popupMenu.menu.add("Low")
            popupMenu.setOnMenuItemClickListener { item ->
                selectedPriority = item.title.toString()
                priorityTextView.text = selectedPriority
                true
            }
            popupMenu.show()
        }

        builder.setView(view)

        builder.setPositiveButton("OK") { _, _ ->
            val taskName = taskNameInput.text.toString().trim()
            val taskDescription = taskDescriptionInput.text.toString().trim()

            if (taskName.isEmpty() || taskDescription.isEmpty() || selectedDueDate.isEmpty() || selectedPriority.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Update task with new details
                val dueDateToUse = sdf.parse(selectedDueDate) ?: taskItem.dueDate.toDate()
                val updatedFields = mapOf(
                    "title" to taskName,
                    "description" to taskDescription,
                    "due_date" to Timestamp(dueDateToUse),
                    "priority" to selectedPriority
                )
                currentBoardId?.let { boardId ->
                    dbHandler.updateTaskItem(boardId, taskItem.taskId, updatedFields) {
                        // Find the task in the local list and update it
                        val taskIndex = tasks.indexOfFirst { it.taskId == taskItem.taskId }
                        if (taskIndex != -1) {
                            tasks[taskIndex].apply {
                                title = taskName
                                description = taskDescription
                                dueDate = Timestamp(dueDateToUse)
                                priority = selectedPriority
                            }
                            // Notify the adapter to refresh the item
                            taskAdapter.notifyItemChanged(taskIndex)
                        }
                    }
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }


    private fun showOverviewPopup(currentBoardId: String?) {
        val boardsSorted = boardList.sortedBy { it.createdAt }
        val adapter = BoardAdapter(this, boardsSorted, currentBoardId)
        gridView.adapter = adapter

        // Check if gridView has a parent, and remove it from that parent if necessary
        (gridView.parent as? ViewGroup)?.removeView(gridView)

        val dialog = AlertDialog.Builder(this)
            .setView(gridView)
            .create()

        gridView.setOnItemClickListener { _, _, position, _ ->
            dialog.dismiss()
            val selectedBoardId = boardsSorted[position].boardId
            jumpToBoard(selectedBoardId)
        }

        dialog.show()
        gridView.setSelection(boardsSorted.indexOfFirst { it.boardId == currentBoardId }) // Scroll to current board
    }

    private fun jumpToBoard(boardId: String) {
        // Assuming each tab in the TabLayout corresponds to a board
        val tabIndex = boardList.indexOfFirst { it.boardId == boardId }
        if (tabIndex != -1) {
            tabLayout.getTabAt(tabIndex)?.select()
        }
    }

    private fun triggerChangeAccount() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("action", "changeAccount")
        startActivity(intent)
    }
}
