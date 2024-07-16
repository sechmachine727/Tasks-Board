package com.prm.tasksboard.taskboards.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.prm.tasksboard.R
import com.prm.tasksboard.taskboards.entity.BoardItem

class BoardAdapter(
    private val context: Context,
    private val boards: List<BoardItem>,
    private val currentBoardId: String?
) : BaseAdapter() {
    override fun getCount(): Int = boards.size

    override fun getItem(position: Int): Any = boards[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.board_overview_item, parent, false)
        val boardNameTextView = view.findViewById<TextView>(R.id.boardNameTextView)
        boardNameTextView.text = boards[position].name

        if (boards[position].boardId == currentBoardId) {
            // Highlight the whole item for the current board
            view.setBackgroundResource(R.drawable.selected_board_background) // Use a drawable for selected state
            boardNameTextView.setTextColor(
                context.resources.getColor(
                    R.color.selected_text_color,
                    null
                )
            ) // Change the text color for visibility
        } else {
            // Reset colors for non-selected items
            view.setBackgroundResource(R.drawable.rounded_corner_border) // Reset the background to default
            boardNameTextView.setTextColor(
                context.resources.getColor(
                    R.color.default_text_color,
                    null
                )
            ) // Reset the text color
        }
        return view
    }
}