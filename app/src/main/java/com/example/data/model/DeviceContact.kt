package com.example.data.model

data class DeviceContact(
    val name: String,
    val phoneNumber: String,
    val isRegisteredInApp: Boolean = false,
    val registeredUser: User? = null
)
