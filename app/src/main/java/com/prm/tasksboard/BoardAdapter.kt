package com.prm.tasksboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class BoardAdapter(private val boardList: List<BoardItem>) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val boardCreatedAt: TextView = view.findViewById(R.id.boardCreatedAt)
        val boardDescription: TextView = view.findViewById(R.id.boardDescription)
        val boardDueDate: TextView = view.findViewById(R.id.boardDueDate)
        val boardPriority: TextView = view.findViewById(R.id.boardPriority)
        val boardStatus: Chip = view.findViewById(R.id.boardStatus)
        val boardUpdatedAt: TextView = view.findViewById(R.id.boardUpdatedAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val boardItem = boardList[position]
        holder.boardCreatedAt.text = boardItem.createdAt
        holder.boardDescription.text = boardItem.description
        holder.boardDueDate.text = boardItem.dueDate
        holder.boardPriority.text = boardItem.priority
        holder.boardUpdatedAt.text = boardItem.updatedAt

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

data class BoardItem(
    var createdAt: String,
    var description: String,
    var dueDate: String,
    var priority: String,
    var updatedAt: String,
    val isFinished: Boolean
)