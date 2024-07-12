package com.prm.tasksboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

class BoardAdapter(private val boardList: List<BoardItem>) :
    RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        holder.boardCreatedAt.text = dateFormat.format(boardItem.createdAt.toDate())
        holder.boardUpdatedAt.text = dateFormat.format(boardItem.updatedAt.toDate())
    }

    override fun getItemCount() = boardList.size
}

data class BoardItem(
    var boardId: String = "",
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Timestamp,
    @get:PropertyName("name") @set:PropertyName("name") var name: String,
    @get:PropertyName("updated_at") @set:PropertyName("updated_at") var updatedAt: Timestamp,
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String,
)