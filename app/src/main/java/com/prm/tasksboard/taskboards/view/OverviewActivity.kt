package com.prm.tasksboard.taskboards.view

import android.os.Bundle
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prm.tasksboard.R
import com.prm.tasksboard.taskboards.entity.BoardItem
import com.prm.tasksboard.taskboards.firestore.DatabaseHandler


class OverviewActivity : AppCompatActivity() {
    lateinit var boardGridView: GridView
    private var boardList = mutableListOf<BoardItem>()
    private val dbHandler = DatabaseHandler()
    val taskboardsActivity = TaskboardsActivity()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grid_outer)
        boardGridView = findViewById(R.id.idGRV)
        dbHandler.getBoardItemsByUserId().addOnSuccessListener { result ->
            boardList.clear()
            val newItems = result.map { document ->
                document.toObject(BoardItem::class.java)
            }
                .sortedBy { it.createdAt }
            boardList.addAll(newItems)
        }
        val gridAdapter = GridRVAdapter(boardList, this)
        boardGridView.adapter = gridAdapter
        boardGridView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText( applicationContext, boardList[position].name + " selected", Toast.LENGTH_SHORT).show()
            taskboardsActivity.setupSelectedTabLayout(position)
        }
    }


}