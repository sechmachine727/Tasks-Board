package com.prm.tasksboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class BoardAdapter(private val boardList: List<BoardItem>) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val boardTitle: TextView = view.findViewById(R.id.boardTitle)
        val boardStatus: Chip = view.findViewById(R.id.boardStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val boardItem = boardList[position]
        holder.boardTitle.text = boardItem.title

        if (boardItem.isFinished) {
            holder.boardStatus.setChipBackgroundColorResource(R.color.status_finished)
            holder.boardStatus.text = holder.itemView.context.getString(R.string.status_finished)
        } else {
            holder.boardStatus.setChipBackgroundColorResource(R.color.status_not_finished)
            holder.boardStatus.text = holder.itemView.context.getString(R.string.status_not_finished)
        }
    }

    override fun getItemCount() = boardList.size
}

data class BoardItem(var title: String, val isFinished: Boolean)