package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class SavedContact(
    val contactId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF1976D2,
    val isFirebaseUser: Boolean = false
) {
    val phone: String get() = phoneNumber
}

data class DeviceContact(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = ""
)
