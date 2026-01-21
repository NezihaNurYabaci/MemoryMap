package com.example.memorymapp.data

data class Memory(
    val id: String = "",
    val description: String = "",
    val date: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = "",
    val timestamp: Long = 0L
)