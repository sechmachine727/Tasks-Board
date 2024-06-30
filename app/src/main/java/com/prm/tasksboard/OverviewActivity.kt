package com.prm.tasksboard

import android.os.Bundle
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OverviewActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var boardPagerAdapter: BoardPagerAdapter
    private lateinit var addBoardButton: Button
    private lateinit var menuButton: MaterialButton
    private val boardList = mutableListOf(
        BoardItem("Board 1", true), //previewDrawable placeholder
        BoardItem("Board 2", true),
    )

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
        boardPagerAdapter = BoardPagerAdapter(boardList)
        viewPager.adapter = boardPagerAdapter

        // Link ViewPager2 and TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = boardList[position].title
        }.attach()

        addBoardButton = findViewById(R.id.addBoardButton)
        addBoardButton.setOnClickListener {
            // Create a new BoardItem
            val newBoard = BoardItem("New Board", true)
            // Add it to your boardList
            boardList.add(newBoard)
            // Notify the adapter that the dataset has changed
            boardPagerAdapter.notifyItemInserted(boardList.size - 1)
            // Refresh the TabLayout
            tabLayout.removeAllTabs()
            TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
                tab.text = boardList[pos].title
            }.attach()
        }

        menuButton = findViewById(R.id.menuButton)
        val popupMenu = PopupMenu(this, menuButton)
        popupMenu.menuInflater.inflate(R.menu.board_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.rename_board -> {
                    // Handle rename board action
                    true
                }
                R.id.delete_board -> {
                    // Handle delete board action
                    true
                }
                else -> false
            }
        }

        menuButton.setOnClickListener {
            popupMenu.show()
        }
    }
}