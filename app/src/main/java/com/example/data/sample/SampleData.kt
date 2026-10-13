package com.example.data.sample

import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.User

object SampleData {
    fun createSavedMessagesChat(user: User): Chat {
        return Chat(
            id = "saved_messages_${user.userId}",
            title = "Mensajes Guardados",
            type = ChatType.SAVED_MESSAGES,
            lastMessage = "Tu nube personal para guardar notas, archivos y enlaces",
            lastMessageTime = "Ahora",
            avatarGradientStart = 0xFF2AABEE,
            avatarGradientEnd = 0xFF1976D2,
            participants = listOf(user.userId),
            description = "Almacenamiento personal en la nube"
        )
    }
}
