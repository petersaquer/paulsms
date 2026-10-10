package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class SavedContact(
    val contactUserId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF1976D2,
    val role: String = UserRoles.USER,
    val isOnline: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
) {
    val name: String get() = displayName.ifEmpty { username }
}
