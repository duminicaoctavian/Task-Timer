package com.octavian.tasktimer

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

class TaskViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer {
//    var name: TextView = containerView.findViewById(R.id.tli_name)
}

private const val TAG = "CursorRecyclerViewAdapt"

class CursorRecyclerViewAdapter :
        RecyclerView.Adapter<TaskViewHolder>() {

}