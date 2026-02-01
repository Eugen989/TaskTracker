package com.example.tasktracker.components.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecoration (
    private val spaceTop: Int,
    private val spaceBottom: Int,
    private val spaceLeft: Int,
    private val spaceRight: Int,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        val position = parent.getChildAdapterPosition(view)
//        val itemCount = parent.adapter?.itemCount ?: 0

        outRect.top = spaceTop
        outRect.bottom = spaceBottom
        outRect.left = spaceLeft
        outRect.right = spaceRight

        super.getItemOffsets(outRect, view, parent, state)
    }
}