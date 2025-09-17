package com.example.logistics_os_android  // ← твой пакет

// data class НУЖНА, чтобы работал copy()
data class Task(
    val title: String,
    val address: String,
    val time: String,
    var done: Boolean = false   // var, чтобы мы могли менять флаг
)