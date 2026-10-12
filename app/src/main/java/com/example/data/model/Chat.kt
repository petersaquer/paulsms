package com.example.data.model

enum class ChatType {
    DIRECT,
    GROUP,
    CHANNEL,
    BOT,
    SAVED_MESSAGES,
    SECRET
}

data class Chat(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val username: String = "",
    val type: ChatType = ChatType.DIRECT,
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isVerified: Boolean = false,
    val isEncrypted: Boolean = false,
    val isCloudSynced: Boolean = false,
    val isBlocked: Boolean = false,
    val encryptionKeyFingerprint: String? = null,
    val contactPhone: String? = null,
    val typingStatus: String? = null,
    val memberCount: Int? = null,
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF2275B3,
    val pinnedMessageText: String? = null,
    val groupConfig: GroupConfig? = null,
    val pinnedMessageIds: List<String> = emptyList(),
    val selectedTopicId: String? = null,
    val disappearingTimerSeconds: Int = 0, // 0 = disabled, 86400 (24h), 604800 (7d), 7776000 (90d)
    val isLocked: Boolean = false,
    val lockPin: String = "",
    val isArchived: Boolean = false,
    val keepArchivedOnNewMessage: Boolean = true,
    val isMarkedUnread: Boolean = false,
    val mutedUntilTimestamp: Long = 0L, // 0 = not muted, -1 = forever, >0 = millis
    val readReceiptsEnabled: Boolean = true
)
