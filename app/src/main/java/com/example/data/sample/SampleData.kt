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
        subtitle = "Tu nube personal y notas",
        username = "",
        type = ChatType.SAVED_MESSAGES,
        lastMessage = "Bienvenido a tu nube personal. Guarda mensajes, archivos y notas aquí.",
        lastMessageTime = "Ahora",
        unreadCount = 0,
        isOnline = false,
        isPinned = true,
        isMuted = false,
        avatarGradientStart = 0xFF2AABEE,
        avatarGradientEnd = 0xFF1976D2,
        pinnedMessageText = "Tu nube personal segura y cifrada"
    )

    fun initialChats(): List<Chat> = emptyList()

    fun initialMessages(): Map<String, List<Message>> = emptyMap()
}
