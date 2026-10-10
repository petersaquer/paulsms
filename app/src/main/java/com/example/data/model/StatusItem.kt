package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class StatusType {
    TEXT,
    PHOTO,
    REEL
}

enum class StatusPrivacy(val label: String, val description: String) {
    MY_CONTACTS("Mis contactos", "Todos tus contactos pueden ver este estado"),
    MY_CONTACTS_EXCEPT("Mis contactos excepto...", "Ocultar a contactos seleccionados"),
    ONLY_SHARE_WITH("Solo compartir con...", "Compartir únicamente con usuarios seleccionados"),
    PUBLIC("Público", "Visible para toda la comunidad de Paul Messenger")
}

@IgnoreExtraProperties
data class StatusViewer(
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF1976D2,
    val viewedAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class StatusItem(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val userAvatarStart: Long = 0xFF2AABEE,
    val userAvatarEnd: Long = 0xFF1976D2,
    val type: String = StatusType.TEXT.name,
    val contentText: String = "",
    val mediaUrl: String = "",
    val backgroundColor: Long = 0xFF0D5C46, // WhatsApp teal/green default
    val fontStyle: String = "NORMAL", // NORMAL, SERIF, MONO, CURSIVE
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
    val privacy: String = StatusPrivacy.MY_CONTACTS.name,
    val excludedUserIds: List<String> = emptyList(),
    val includedUserIds: List<String> = emptyList(),
    val viewers: List<StatusViewer> = emptyList(),
    val reactions: Map<String, String> = emptyMap() // userId -> emoji
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
    val isReel: Boolean get() = type == StatusType.REEL.name
    val isPhoto: Boolean get() = type == StatusType.PHOTO.name
    val isText: Boolean get() = type == StatusType.TEXT.name

    fun canUserView(viewerUserId: String): Boolean {
        if (viewerUserId == userId) return true
        if (isExpired) return false
        return when (privacy) {
            StatusPrivacy.PUBLIC.name -> true
            StatusPrivacy.MY_CONTACTS.name -> true
            StatusPrivacy.MY_CONTACTS_EXCEPT.name -> !excludedUserIds.contains(viewerUserId)
            StatusPrivacy.ONLY_SHARE_WITH.name -> includedUserIds.contains(viewerUserId)
            else -> true
        }
    }
}

/**
 * Group of active stories by a single user
 */
data class UserStatusGroup(
    val userId: String,
    val displayName: String,
    val username: String,
    val userAvatarStart: Long,
    val userAvatarEnd: Long,
    val isCurrentUser: Boolean,
    val statuses: List<StatusItem>,
    val hasUnseen: Boolean
) {
    val latestTimestamp: Long get() = statuses.maxOfOrNull { it.createdAt } ?: 0L
}
