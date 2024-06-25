package com.prm.tasksboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// kotlin/com/example/yourapp/BoardAdapter.kt
class BoardAdapter(private val boardList: List<BoardItem>) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val boardTitle: TextView = view.findViewById(R.id.boardTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val boardItem = boardList[position]
        holder.boardTitle.text = boardItem.title
        // Set the image for holder.boardPreview using your image loading library
    }

    override fun getItemCount() = boardList.size
}

data class BoardItem(val title: String, val previewDrawable: Int)
