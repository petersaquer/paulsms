package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Chat
import com.example.data.model.ChatType

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val username: String,
    val type: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val isOnline: Boolean,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val isVerified: Boolean,
    val isEncrypted: Boolean,
    val isCloudSynced: Boolean,
    val isBlocked: Boolean,
    val encryptionKeyFingerprint: String?,
    val contactPhone: String?,
    val typingStatus: String?,
    val memberCount: Int?,
    val avatarGradientStart: Long,
    val avatarGradientEnd: Long,
    val pinnedMessageText: String?,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toChat(): Chat = Chat(
        id = id,
        title = title,
        subtitle = subtitle,
        username = username,
        type = try { ChatType.valueOf(type) } catch (e: Exception) { ChatType.DIRECT },
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount,
        isOnline = isOnline,
        isPinned = isPinned,
        isMuted = isMuted,
        isVerified = isVerified,
        isEncrypted = isEncrypted,
        isCloudSynced = isCloudSynced,
        isBlocked = isBlocked,
        encryptionKeyFingerprint = encryptionKeyFingerprint,
        contactPhone = contactPhone,
        typingStatus = typingStatus,
        memberCount = memberCount,
        avatarGradientStart = avatarGradientStart,
        avatarGradientEnd = avatarGradientEnd,
        pinnedMessageText = pinnedMessageText
    )

    companion object {
        fun fromChat(chat: Chat): ChatEntity = ChatEntity(
            id = chat.id,
            title = chat.title,
            subtitle = chat.subtitle,
            username = chat.username,
            type = chat.type.name,
            lastMessage = chat.lastMessage,
            lastMessageTime = chat.lastMessageTime,
            unreadCount = chat.unreadCount,
            isOnline = chat.isOnline,
            isPinned = chat.isPinned,
            isMuted = chat.isMuted,
            isVerified = chat.isVerified,
            isEncrypted = chat.isEncrypted,
            isCloudSynced = chat.isCloudSynced,
            isBlocked = chat.isBlocked,
            encryptionKeyFingerprint = chat.encryptionKeyFingerprint,
            contactPhone = chat.contactPhone,
            typingStatus = chat.typingStatus,
            memberCount = chat.memberCount,
            avatarGradientStart = chat.avatarGradientStart,
            avatarGradientEnd = chat.avatarGradientEnd,
            pinnedMessageText = chat.pinnedMessageText
        )
    }
}
