package com.example.data.model

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MessageType {
    TEXT,
    VOICE,
    PHOTO,
    VIDEO,
    POLL,
    STICKER,
    GIF,
    LOCATION,
    LIVE_LOCATION,
    FILE,
    CONTACT
}

data class Message(
    val id: String = "",
    val chatId: String = "",
    val text: String = "",
    val timestamp: String = "",
    val isOutgoing: Boolean = false,
    val senderId: String = "",
    val senderUsername: String = "",
    val senderName: String? = null,
    val senderAvatarGradient: Long? = null,
    val status: MessageStatus = MessageStatus.READ,
    val type: MessageType = MessageType.TEXT,
    val isEncrypted: Boolean = false,
    val isViewOnce: Boolean = false,
    val hasBeenViewed: Boolean = false,
    val forwardedFrom: String? = null,
    val replyTo: Message? = null,
    val isEdited: Boolean = false,
    val editedTimestamp: String? = null,
    val isDeletedForEveryone: Boolean = false,
    val isStarred: Boolean = false,
    val expirationTimestamp: Long = 0L,
    val voiceDurationSeconds: Int? = null,
    val voiceWaveform: List<Float>? = null,
    val pollData: PollData? = null,
    val locationData: LocationData? = null,
    val photoUrl: String? = null,
    val videoUrl: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactUsername: String? = null,
    val gifUrl: String? = null,
    val stickerEmoji: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val userReaction: String? = null,
    val topicId: String? = null,
    val isPinned: Boolean = false,
    val isAnonymous: Boolean = false,
    val userBadge: String? = null
) {
    fun isOutgoingFor(currentUserId: String, currentUsername: String): Boolean {
        val cleanCurrentUid = currentUserId.trim().lowercase()
        val cleanCurrentUsername = currentUsername.trim().removePrefix("@").lowercase()
        val cleanSenderId = senderId.trim().lowercase()
        val cleanSenderUsername = senderUsername.trim().removePrefix("@").lowercase()

        if (cleanSenderId.isNotBlank() && cleanCurrentUid.isNotBlank() && cleanSenderId == cleanCurrentUid) return true
        if (cleanSenderUsername.isNotBlank() && cleanCurrentUsername.isNotBlank() && cleanSenderUsername == cleanCurrentUsername) return true
        if (cleanSenderId.isNotBlank() && cleanCurrentUid.isNotBlank() && cleanSenderId != cleanCurrentUid) return false
        if (cleanSenderUsername.isNotBlank() && cleanCurrentUsername.isNotBlank() && cleanSenderUsername != cleanCurrentUsername) return false

        return isOutgoing
    }
}
