package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val userId: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val bio: String = "",
    val isOnline: Boolean = true,
    val isFirebaseUser: Boolean = true,
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF2275B3
) {
    val name: String get() = displayName.ifEmpty { username }
}
