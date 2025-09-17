package com.example.logistics_os_android
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.driverapp.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Кнопка "i" — переход на правила начисления поинтов
        findViewById<ImageButton>(R.id.btnInfo).setOnClickListener {
            startActivity(Intent(this, PointsInfoActivity::class.java))
        }

        // Пример установки прогресса (52% = 520 поинтов из 1000)
        findViewById<ProgressBar>(R.id.progressPoints).progress = 52
    }
}