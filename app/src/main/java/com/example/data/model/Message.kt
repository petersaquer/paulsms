package com.example.data.model

enum class MessageStatus {
    SENDING,
    SENT,
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
    val senderName: String? = null,
    val senderAvatarGradient: Long? = null,
    val status: MessageStatus = MessageStatus.READ,
    val type: MessageType = MessageType.TEXT,
    val isEncrypted: Boolean = false,
    val isViewOnce: Boolean = false,
    val hasBeenViewed: Boolean = false,
    val forwardedFrom: String? = null,
    val replyTo: Message? = null,
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
    val userReaction: String? = null
)
