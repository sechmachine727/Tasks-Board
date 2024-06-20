package com.prm.tasksboard

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OverviewActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var boardAdapter: BoardAdapter
    private lateinit var addBoardButton: Button
    private val boardList = mutableListOf(
        BoardItem("Board 1", R.drawable.ic_launcher_foreground), //previewDrawable placeholder
        BoardItem("Board 2", R.drawable.ic_launcher_foreground),
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
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        boardAdapter = BoardAdapter(boardList)
        recyclerView.adapter = boardAdapter

        // Set up ItemTouchHelper for swipe gestures
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Handle swipe-to-dismiss logic here
                val position = viewHolder.adapterPosition
                boardList.removeAt(position)
                boardAdapter.notifyItemRemoved(position)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addBoardButton = findViewById(R.id.addBoardButton)
        addBoardButton.setOnClickListener {
            // Create a new TabItem
            val newBoard = BoardItem("New Tab", R.drawable.ic_launcher_foreground)
            // Add it to your tabList
            boardList.add(newBoard)
            // Notify the adapter that the dataset has changed
            boardAdapter.notifyItemInserted(boardList.size - 1)
        }
        //TODO: Pop-up to ask for new board name
    }
}