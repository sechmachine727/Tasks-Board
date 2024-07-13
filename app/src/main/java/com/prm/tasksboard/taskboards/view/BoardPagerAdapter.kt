package com.prm.tasksboard.taskboards.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prm.tasksboard.R
import com.prm.tasksboard.taskboards.entity.BoardItem
import java.text.SimpleDateFormat
import java.util.Locale

class BoardPagerAdapter(private val boardList: MutableList<BoardItem>) :
    RecyclerView.Adapter<BoardPagerAdapter.BoardViewHolder>() {

    class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Define the view components of each item here
        val boardCreatedAt: TextView = view.findViewById(R.id.boardCreatedAt)
        val boardUpdatedAt: TextView = view.findViewById(R.id.boardUpdatedAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val boardItem = boardList[position]
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        holder.boardCreatedAt.text = holder.itemView.context.getString(
            R.string.board_created_at,
            dateFormat.format(boardItem.createdAt.toDate())
        )

        holder.boardUpdatedAt.text = holder.itemView.context.getString(
            R.string.board_updated_at,
            dateFormat.format(boardItem.updatedAt.toDate())
        )
    }

    override fun getItemCount(): Int {
        return boardList.size
    }
}