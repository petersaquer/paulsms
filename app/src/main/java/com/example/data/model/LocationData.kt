package com.example.data.model

data class LocationData(
    val latitude: Double = 40.416775,
    val longitude: Double = -3.703790,
    val title: String = "Madrid Centro",
    val address: String = "Plaza Mayor, Madrid, España",
    val isLive: Boolean = false,
    val liveDurationMinutes: Int = 15,
    val remainingMinutes: Int = 15,
    val accuracyMeters: Float = 8.5f,
    val isMoving: Boolean = true
)
