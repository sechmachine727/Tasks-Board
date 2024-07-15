package com.prm.tasksboard.taskboards.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.prm.tasksboard.R
import com.prm.tasksboard.taskboards.entity.BoardItem

internal class GridRVAdapter (
    private val boardList: MutableList<BoardItem> = mutableListOf<BoardItem>(),
    private val context: Context
):
    BaseAdapter() {

        private var layoutInflater:LayoutInflater? = null
        private lateinit var boardName: TextView

        override fun getCount(): Int {
            return boardList.size
        }

        override fun getItem(p0: Int): Any?{
            return null
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var convertView = convertView
            if (layoutInflater == null){
                layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            }
            if (convertView == null){
                convertView = layoutInflater!!.inflate(R.layout.grid_items, null)
            }
            boardName = convertView!!.findViewById(R.id.boardName)
            boardName.setText(boardList.get(position).name)
            return convertView
        }
}