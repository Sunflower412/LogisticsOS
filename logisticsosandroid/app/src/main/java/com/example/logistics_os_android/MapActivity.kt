package com.example.logistics_os_android
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.driverapp.R

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_map)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}