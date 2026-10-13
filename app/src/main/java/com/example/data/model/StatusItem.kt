package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class StatusType {
    TEXT,
    IMAGE,
    VIDEO,
    REEL
}

enum class StatusPrivacy {
    ALL,
    CONTACTS,
    EXCEPT,
    ONLY_SHARE_WITH
}

@IgnoreExtraProperties
data class StatusViewer(
    val userId: String = "",
    val displayName: String = "",
    val viewedAt: Long = System.currentTimeMillis(),
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF1976D2
)

@IgnoreExtraProperties
data class StatusItem(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val textContent: String = "",
    val mediaUrl: String = "",
    val type: StatusType = StatusType.TEXT,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
    val userAvatarStart: Long = 0xFF2AABEE,
    val userAvatarEnd: Long = 0xFF1976D2,
    val backgroundColor: Long = 0xFF2AABEE,
    val caption: String = "",
    val privacy: StatusPrivacy = StatusPrivacy.ALL,
    val viewers: List<StatusViewer> = emptyList()
)

data class UserStatusGroup(
    val userId: String = "",
    val displayName: String = "",
    val userAvatarStart: Long = 0xFF2AABEE,
    val userAvatarEnd: Long = 0xFF1976D2,
    val items: List<StatusItem> = emptyList(),
    val hasUnseen: Boolean = false
)
