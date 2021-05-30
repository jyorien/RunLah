package com.example.runlah.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runlah.R

class HistoryListAdapter(val recordList: ArrayList<Record>, val onItemClick: (Record) -> Unit, val onDeleteClick: (Record) -> Unit): RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val history_date: TextView = itemView.findViewById(R.id.history_date)
        val history_distance: TextView = itemView.findViewById(R.id.history_distance)
        val history_time: TextView = itemView.findViewById(R.id.history_time)
        val history_speed: TextView = itemView.findViewById(R.id.history_speed)
        val history_steps: TextView = itemView.findViewById(R.id.history_steps)
        val history_delete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //  pass layout to viewholder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_list_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // bind viewholder to data
        val item = recordList[position]
        holder.history_date.text = item.formattedDate
        holder.history_distance.text = item.distance
        holder.history_time.text = item.timeTaken
        holder.history_speed.text = item.speed
        holder.history_steps.text = item.steps
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
        holder.history_delete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = recordList.size
}