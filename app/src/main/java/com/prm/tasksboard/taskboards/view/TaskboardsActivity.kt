package com.prm.tasksboard.taskboards.view

import android.app.AlertDialog
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
import com.prm.tasksboard.taskboards.firestore.DatabaseHandler

class TaskboardsActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var boardPagerAdapter: BoardPagerAdapter
    private lateinit var addTaskButton: Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_taskboards)
        setWindowInsetsListener()

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        addTaskButton = findViewById(R.id.addTaskButton)
        menuButton = findViewById(R.id.menuButton)
        emptyView = findViewById(R.id.emptyView)

        boardPagerAdapter = BoardPagerAdapter(boardList)
        viewPager.adapter = boardPagerAdapter

        dbHandler.checkFirestoreConnection()

        // Ensure this is called after viewPager and tabLayout have been initialized
        setupTabLayoutWithViewPager()
        setTabLayoutListeners()
        updateEmptyViewVisibility()

        addTaskButton.setOnClickListener {
            createNewBoard()
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
        dbHandler.addBoardItem(newBoard)
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
        dbHandler.deleteBoardItem(boardList[viewPager.currentItem].boardId)
        // Remove the board from your boardList
        boardList.removeAt(viewPager.currentItem)
        // Notify the adapter that the dataset has changed
        boardPagerAdapter.notifyItemRemoved(viewPager.currentItem)
        if (boardList.isEmpty()) {
            viewPager.adapter = null
            tabLayoutMediator?.detach() // Detach TabLayoutMediator if no items are left
            tabLayoutMediator = null // Clear the TabLayoutMediator reference
        } else {
            boardPagerAdapter.notifyItemRemoved(currentItem)
            setupTabLayoutWithViewPager() // Re-setup TabLayout with ViewPager
        }
        updateEmptyViewVisibility()
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
}
