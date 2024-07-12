package com.prm.tasksboard

import android.os.Bundle
import android.view.View
import android.widget.Button
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
import android.app.AlertDialog
import android.widget.EditText
import android.view.ViewGroup

class OverviewActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var boardPagerAdapter: BoardPagerAdapter
    private lateinit var addBoardButton: Button
    private lateinit var menuButton: MaterialButton
    private lateinit var emptyView: TextView
    private val boardList = mutableListOf<BoardItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_overview)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        addBoardButton = findViewById(R.id.addBoardButton)
        menuButton = findViewById(R.id.menuButton)
        emptyView = findViewById(R.id.emptyView)

        boardPagerAdapter = BoardPagerAdapter(boardList)
        viewPager.adapter = boardPagerAdapter

        // Link ViewPager2 and TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = boardList[position].description // Use description instead of title
        }.attach()

        tabLayout.post {
            for (i in 0 until tabLayout.tabCount) {
                val tab = tabLayout.getTabAt(i)
                val tabView = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(tab?.position ?: 0)
                tabView.setOnLongClickListener {
                    showRenameDialog(i)
                    true
                }
            }
        }

        // Set click listener on TabLayout
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

        emptyView.visibility = if (boardList.isEmpty()) View.VISIBLE else View.GONE

        addBoardButton.setOnClickListener {
            // Create a new BoardItem
            val newBoard = BoardItem(
                createdAt = "New Board Created At",
                description = "New Board",
                dueDate = "New Board Due Date",
                priority = "New Board Priority",
                updatedAt = "New Board Updated At",
                isFinished = true
            )
            // Add it to your boardList
            boardList.add(newBoard)
            // Notify the adapter that the dataset has changed
            boardPagerAdapter.notifyItemInserted(boardList.size - 1)
            // Refresh the TabLayout
            tabLayout.removeAllTabs()
            TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
                tab.text = boardList[pos].description // Use description instead of title
            }.attach()
            emptyView.visibility = if (boardList.isEmpty()) View.VISIBLE else View.GONE
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
                        // Create a new BoardItem
                        val newBoard = BoardItem(
                            createdAt = "New Board Created At",
                            description = "New Board",
                            dueDate = "New Board Due Date",
                            priority = "New Board Priority",
                            updatedAt = "New Board Updated At",
                            isFinished = true
                        )
                        // Add it to your boardList
                        boardList.add(newBoard)
                        // If this is the first board after all boards have been deleted, reattach the adapter
                        if (boardList.size == 1) {
                            viewPager.adapter = boardPagerAdapter
                        }
                        // Notify the adapter that the dataset has changed
                        boardPagerAdapter.notifyItemInserted(boardList.size - 1)
                        // Refresh the TabLayout
                        tabLayout.removeAllTabs()
                        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
                            tab.text = boardList[pos].description // Use description instead of title
                        }.attach()
                        emptyView.visibility = if (boardList.isEmpty()) View.VISIBLE else View.GONE
                        true
                    }

                    R.id.delete_board -> {
                        // Remove the current board from the boardList
                        boardList.removeAt(viewPager.currentItem)
                        // Notify the adapter that the dataset has changed
                        boardPagerAdapter.notifyItemRemoved(viewPager.currentItem)
                        // If there are no boards left, detach the adapter
                        if (boardList.isEmpty()) {
                            viewPager.adapter = null
                        }
                        // Refresh the TabLayout
                        tabLayout.removeAllTabs()
                        if (boardList.isNotEmpty()) {
                            TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
                                tab.text = boardList[pos].description // Use description instead of title
                            }.attach()
                        }
                        emptyView.visibility = if (boardList.isEmpty()) View.VISIBLE else View.GONE
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }
    private fun showRenameDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Board")

        val input = EditText(this)
        input.setText(boardList[position].description) // Use description instead of title
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newName = input.text.toString()
            boardList[position].description = newName // Use description instead of title
            tabLayout.getTabAt(position)?.text = newName
            boardPagerAdapter.notifyItemChanged(position)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
