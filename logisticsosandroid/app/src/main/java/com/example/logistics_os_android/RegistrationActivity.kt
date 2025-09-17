package com.example.logistics_os_android   // проверь, чтобы совпадало с именем папки-пакета

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.driverapp.R

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val etId = findViewById<EditText>(R.id.etId)
        val etPwd = findViewById<EditText>(R.id.etPwd)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            if (etId.text.isNullOrBlank()) { etId.error = "Введите ID"; return@setOnClickListener }
            if (etPwd.text.isNullOrBlank()) { etPwd.error = "Введите пароль"; return@setOnClickListener }
            startActivity(Intent(this, TasksActivity::class.java))
            finish()
        }
    }
}