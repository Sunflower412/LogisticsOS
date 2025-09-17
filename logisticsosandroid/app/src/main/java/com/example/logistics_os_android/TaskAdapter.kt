package com.example.logistics_os_android

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driverapp.R

class TaskAdapter(
    private val items: MutableList<Task>,
    private val onToggle: (Int) -> Unit   // чёткий тип лямбды, чтобы не было "Cannot infer type"
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvAddr:  TextView = v.findViewById(R.id.tvAddr)
        val tvTime:  TextView = v.findViewById(R.id.tvTime)
        val ivCheck: ImageView = v.findViewById(R.id.ivCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_tasks, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val t = items[pos]
        h.tvTitle.text = t.title
        h.tvAddr.text  = t.address
        h.tvTime.text  = "к ${t.time}"

        // зачёркивание при done
        fun strike(tv: TextView, on: Boolean) {
            tv.paintFlags = if (on) tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else   tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        strike(h.tvTitle, t.done)
        strike(h.tvAddr,  t.done)
        strike(h.tvTime,  t.done)

        h.ivCheck.alpha = if (t.done) 1f else 0.5f
        h.ivCheck.setOnClickListener { onToggle(pos) }
    }

    override fun getItemCount() = items.size
}