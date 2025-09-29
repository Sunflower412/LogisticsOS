package com.example.logistics_os_android
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.driverapp.R

class PointsInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points_info)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}