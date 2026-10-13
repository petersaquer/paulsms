package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class ChatType {
    DIRECT,
    GROUP,
    CHANNEL,
    SAVED_MESSAGES,
    BOT
}

@IgnoreExtraProperties
data class Chat(
    val id: String = "",
    val title: String = "",
    val type: ChatType = ChatType.DIRECT,
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isBlocked: Boolean = false,
    val isOnline: Boolean = false,
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF2275B3,
    val participants: List<String> = emptyList(),
    val otherUserId: String = "",
    val otherUserUsername: String = "",
    val description: String = "",
    val disappearingMessageDuration: Int = 0,
    val isLocked: Boolean = false,
    val lockPin: String = "",
    val groupConfig: GroupConfig = GroupConfig(groupId = id)
) {
    val isSavedMessages: Boolean get() = type == ChatType.SAVED_MESSAGES || title == "Mensajes Guardados"
}
