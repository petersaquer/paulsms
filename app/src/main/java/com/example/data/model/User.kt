package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

object UserRoles {
    const val ADMIN = "ADMIN"
    const val MODERATOR = "MODERATOR"
    const val USER = "USER"
}

@IgnoreExtraProperties
data class User(
    val userId: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val role: String = UserRoles.USER, // ADMIN, MODERATOR, USER
    val isBanned: Boolean = false,
    val isBlocked: Boolean = false,
    val isOnline: Boolean = true,
    val isFirebaseUser: Boolean = true,
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF2275B3,
    val privacyLastSeen: String = "Todos", // "Todos", "Mis contactos", "Nadie"
    val privacyPhone: String = "Mis contactos",
    val privacyReadReceipts: Boolean = true,
    val profilePictureUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val name: String get() = displayName.ifEmpty { username }
    val isAdmin: Boolean get() = role.equals(UserRoles.ADMIN, ignoreCase = true)
    val isModerator: Boolean get() = role.equals(UserRoles.MODERATOR, ignoreCase = true) || isAdmin
}
