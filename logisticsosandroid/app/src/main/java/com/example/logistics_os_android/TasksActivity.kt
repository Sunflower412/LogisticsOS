package com.example.logistics_os_android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driverapp.R

class TasksActivity : AppCompatActivity() {
    private val tasks = mutableListOf(
        Task("Забрать груз", "ул. Ленина, 15", "10:00"),
        Task("Доставить в офис “Ромашка”", "пр. Мира, 42", "11:30"),
        Task("Доставить на склад", "ш. Энтузиастов, 8", "13:00"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        val rv = findViewById<RecyclerView>(R.id.rv)
        val adapter = TaskAdapter(tasks) { i ->
            tasks[i] = tasks[i].copy(done = !tasks[i].done)
            rv.adapter?.notifyItemChanged(i)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnNavigate).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }
}