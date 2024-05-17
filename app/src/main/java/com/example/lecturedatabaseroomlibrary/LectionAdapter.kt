package com.example.lecturedatabaseroomlibrary


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LectionAdapter(
    private var lections: List<T_Lection>,
    private val onLectionClickListener: (T_Lection) -> Unit
) : RecyclerView.Adapter<LectionAdapter.LectionViewHolder>() {

    inner class LectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.lectionTitleTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_lection, parent, false)
        return LectionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LectionViewHolder, position: Int) {
        val lection = lections[position]
        holder.titleTextView.text = lection.title
        holder.itemView.setOnClickListener {
            onLectionClickListener(lection)
        }
    }

    override fun getItemCount() = lections.size

    fun updateLections(newLections: List<T_Lection>) {
        lections = newLections
        notifyDataSetChanged()
    }
}

