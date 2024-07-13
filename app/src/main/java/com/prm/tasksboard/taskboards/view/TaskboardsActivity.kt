package com.prm.tasksboard.taskboards.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.prm.tasksboard.R
import com.prm.tasksboard.taskboards.entity.BoardItem
import com.prm.tasksboard.taskboards.entity.TaskItem
import com.prm.tasksboard.taskboards.firestore.DatabaseHandler
import java.util.UUID
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskboardsActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var boardPagerAdapter: BoardPagerAdapter
    private lateinit var addBoardButton: Button
    private lateinit var menuButton: MaterialButton
    private lateinit var emptyView: TextView
    private var tabLayoutMediator: TabLayoutMediator? = null
    private val boardList = mutableListOf<BoardItem>()
    private val loggedInUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val dbHandler = DatabaseHandler()

    private fun setupTabLayoutWithViewPager() {
        tabLayoutMediator?.detach() // Detach existing TabLayoutMediator if any
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = boardList[position].name
        }
        tabLayoutMediator?.attach()
    }

    private fun setupViewPagerAndTabs() {
        // Assuming boardList is already populated
        boardPagerAdapter = BoardPagerAdapter(boardList)
        viewPager.adapter = boardPagerAdapter

        // Setup TabLayoutMediator after setting the adapter
        tabLayoutMediator?.detach() // Detach existing TabLayoutMediator if any
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = boardList[position].name
        }
        tabLayoutMediator?.attach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_taskboards)
        setWindowInsetsListener()

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        addBoardButton = findViewById(R.id.addBoardButton)
        menuButton = findViewById(R.id.menuButton)
        emptyView = findViewById(R.id.emptyView)

        boardPagerAdapter = BoardPagerAdapter(boardList)
        viewPager.adapter = boardPagerAdapter

        dbHandler.checkFirestoreConnection()

        // Ensure this is called after viewPager and tabLayout have been initialized
        setupTabLayoutWithViewPager()
        setTabLayoutListeners()
        updateEmptyViewVisibility()

        fetchAndDisplayBoards()

        addBoardButton.setOnClickListener {
            showAddTaskDialog() // Correctly placed inside onCreate
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

                    else -> false
                }
            }
            popupMenu.show()
        }
        setupViewPagerPageChangeCallback()
    }

    private fun setupViewPagerPageChangeCallback() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Fetch and display tasks for the newly selected board
                displayTasks(boardList[position].boardId)
            }
        })
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
                val tabView = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(tab?.position ?: 0)
                tabView.setOnLongClickListener {
                    showRenameDialog(tab?.position ?: 0)
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

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Task")

        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskNameInput = view.findViewById<EditText>(R.id.taskNameInput)
        val taskDescriptionInput = view.findViewById<EditText>(R.id.taskDescriptionInput)
        val dueDateTextView = view.findViewById<TextView>(R.id.dueDateTextView)

        var selectedDueDate = ""

        dueDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                // Format the selected date and display it on the TextView
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDueDate = dateFormat.format(selectedDate.time)
                dueDateTextView.text = selectedDueDate
            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 // Disallow past dates
            datePickerDialog.show()
        }

        builder.setView(view)

        builder.setPositiveButton("OK") { _, _ ->
            val taskName = taskNameInput.text.toString()
            val taskDescription = taskDescriptionInput.text.toString()
            addNewTask(taskName, taskDescription)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun addNewTask(taskName: String, taskDescription: String) {
        if (boardList.isEmpty()) {
            val newBoard = BoardItem(
                createdAt = Timestamp.now(),
                name = "Default Board",
                updatedAt = Timestamp.now(),
                userId = loggedInUserId
            )
            // Asynchronously add the new board and get its ID
            dbHandler.addBoardItem(newBoard) { newBoardId ->
                val updatedBoard = newBoard.copy(boardId = newBoardId)
                boardList.add(updatedBoard)
                // Update ViewPager adapter with the new board
                viewPager.adapter = boardPagerAdapter
                viewPager.adapter?.notifyDataSetChanged()
                // Re-setup TabLayout with ViewPager after updating the adapter
                setupTabLayoutWithViewPager()
                updateEmptyViewVisibility()
                // Now add the task to the newly created board
                addTaskToBoard(taskName, newBoardId, loggedInUserId, taskDescription)
            }
        } else {
            val currentBoardId = boardList[viewPager.currentItem].boardId
            addTaskToBoard(taskName, currentBoardId, loggedInUserId, taskDescription)
        }
    }

    private fun addTaskToBoard(taskName: String, boardId: String, userId: String, description: String) {
        val newTask = TaskItem(
            taskId = UUID.randomUUID().toString(),
            boardId = boardId,
            title = taskName,
            description = description, // Now correctly references the parameter
            status = "Pending",
            dueDate = "",
            priority = "Normal",
            userId = loggedInUserId // Assign the logged-in user's ID to the task
        )
        dbHandler.addTaskItem(newTask, boardId) {
            displayTasks(boardId)
        }
    }

    private fun displayTasks(boardId: String) {
        dbHandler.getTasksByBoardId(boardId) { tasks: List<TaskItem> ->
            // Filter tasks to only include those that match the current board's ID
            val filteredTasks = tasks.filter { it.boardId == boardId }
            // Sort tasks by createdAt timestamp or any other criteria if needed
            val sortedTasks = filteredTasks.sortedBy { it.createdAt }
            val recyclerView = findViewById<RecyclerView>(R.id.tasksRecyclerView)
            val taskAdapter = TaskAdapter(sortedTasks) { task: TaskItem ->
                dbHandler.deleteTaskItem(task.boardId, task.taskId) {
                    // Fetch and display tasks again to update the UI
                    displayTasks(boardId)
                }
            }
            if (recyclerView.adapter == null) {
                recyclerView.layoutManager = LinearLayoutManager(this@TaskboardsActivity)
                recyclerView.adapter = taskAdapter
            } else {
                (recyclerView.adapter as TaskAdapter).updateTasks(sortedTasks)
            }
        }
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
        }
        if (boardList.size == 1) { // If this is the first board being added
            viewPager.adapter = boardPagerAdapter
            setupTabLayoutWithViewPager() // Re-setup TabLayout with ViewPager
        } else {
            boardPagerAdapter.notifyItemInserted(boardList.size - 1)
        }
        updateEmptyViewVisibility()
    }

    private fun deleteBoard() {
        val currentItem = viewPager.currentItem
        // Delete the board from firestore
        dbHandler.deleteBoardItem(boardList[currentItem].boardId)
        // Remove the board from your boardList
        boardList.removeAt(currentItem)
        // Notify the adapter that the dataset has changed
        boardPagerAdapter.notifyItemRemoved(currentItem)
        if (boardList.isEmpty()) {
            viewPager.adapter = null
            tabLayoutMediator?.detach() // Detach TabLayoutMediator if no items are left
            tabLayoutMediator = null // Clear the TabLayoutMediator reference
            clearAndHideTaskList() // Clear and hide the task list
        } else {
            boardPagerAdapter.notifyItemRemoved(currentItem)
            setupTabLayoutWithViewPager() // Re-setup TabLayout with ViewPager
            displayTasksForCurrentBoard()
        }
        updateEmptyViewVisibility()
    }

    private fun clearAndHideTaskList() {
        // Find the RecyclerView and clear its adapter
        val recyclerView = findViewById<RecyclerView>(R.id.tasksRecyclerView)
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
            boardPagerAdapter.notifyItemChanged(position)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun fetchAndDisplayBoards() {
        dbHandler.getBoardItemsByUserId().addOnSuccessListener { result ->
            val startPosition = boardList.size // Track start position before adding new items
            boardList.clear() // Optional: Clear existing items if refreshing the entire list
            val newItems = result.map { document ->
                document.toObject(BoardItem::class.java)
            }
            boardList.addAll(newItems)
            if (boardList.isNotEmpty()) {
                // Notify adapter about the range of items inserted
                boardPagerAdapter.notifyItemRangeInserted(startPosition, newItems.size)
                setupTabLayoutWithViewPager()
                // After setting up the ViewPager and TabLayout, fetch and display tasks for the current board
                displayTasksForCurrentBoard()
            }
            setupViewPagerAndTabs()
            updateEmptyViewVisibility()
        }
    }

    private fun displayTasksForCurrentBoard() {
        // Ensure there's at least one board to fetch tasks for
        if (boardList.isNotEmpty()) {
            val currentBoardId = boardList[viewPager.currentItem].boardId
            displayTasks(currentBoardId)
        }
    }
}
