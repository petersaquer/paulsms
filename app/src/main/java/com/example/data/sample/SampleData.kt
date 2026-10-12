package com.example.data.sample

import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.Message
import com.example.data.model.User
import com.example.data.model.UserRoles

object SampleData {
    fun createSavedMessagesChat(user: User): Chat = Chat(
        id = "saved_${user.userId}",
        title = "Mensajes Guardados",
        subtitle = "Tus notas y mensajes personales",
        username = "",
        type = ChatType.SAVED_MESSAGES,
        lastMessage = "Guarda mensajes, audios, fotos y notas aquí.",
        lastMessageTime = "Ahora",
        unreadCount = 0,
        isOnline = false,
        isPinned = true,
        isMuted = false,
        avatarGradientStart = 0xFF2AABEE,
        avatarGradientEnd = 0xFF1976D2,
        pinnedMessageText = "Notas personales seguras"
    )

    fun initialChats(): List<Chat> = emptyList()

    fun initialMessages(): Map<String, List<Message>> = emptyMap()
}
