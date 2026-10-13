package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class MessageType {
    TEXT,
    VOICE,
    PHOTO,
    VIDEO,
    FILE,
    LOCATION,
    POLL,
    GIF,
    CONTACT
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

@IgnoreExtraProperties
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)

@IgnoreExtraProperties
data class PollOption(
    val id: String = "",
    val text: String = "",
    val votes: Int = 0,
    val voterIds: List<String> = emptyList()
)

@IgnoreExtraProperties
data class PollData(
    val question: String = "",
    val options: List<PollOption> = emptyList(),
    val totalVotes: Int = 0
)

@IgnoreExtraProperties
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val mediaCaption: String = "",
    val durationSeconds: Int = 0,
    val fileName: String = "",
    val fileSize: String = "",
    val location: LocationData? = null,
    val poll: PollData? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val userReaction: String = "",
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val isViewOnce: Boolean = false,
    val isViewed: Boolean = false,
    val isStarred: Boolean = false,
    val isEdited: Boolean = false,
    val isForwarded: Boolean = false,
    val originalAuthor: String = ""
)
