package com.prm.tasksboard

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

class OverviewActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var boardPagerAdapter: BoardPagerAdapter
    private lateinit var addBoardButton: Button
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
        boardPagerAdapter = BoardPagerAdapter(boardList)
        viewPager.adapter = boardPagerAdapter

        addBoardButton = findViewById(R.id.addBoardButton)
        addBoardButton.setOnClickListener {
            // Create a new BoardItem
            val newBoard = BoardItem("New Board", true)
            // Add it to your boardList
            boardList.add(newBoard)
            // Notify the adapter that the dataset has changed
            boardPagerAdapter.notifyItemInserted(boardList.size - 1)
        }
        //TODO: Pop-up to ask for new board name
    }
}