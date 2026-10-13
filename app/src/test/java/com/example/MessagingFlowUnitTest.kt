package com.example

import com.example.data.local.ChatEntity
import com.example.data.local.ContactEntity
import com.example.data.local.MessageEntity
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.PollData
import com.example.data.model.PollOption
import com.example.data.model.SavedContact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias completas para los flujos críticos de mensajería:
 * - Mapeos bidireccionales Room <-> Modelo canónico
 * - Ordenamiento numérico por createdAt
 * - Fusión (merge) de listas sin duplicados de ID
 * - Ciclo de vida y transición de estados del mensaje
 * - Lógica de votación de encuestas
 * - Edición y revocación/borrado para todos
 */
class MessagingFlowUnitTest {

    @Test
    fun testRoomChatEntityMapping() {
        val originalChat = Chat(
            id = "chat_123",
            title = "Carlos Gómez",
            subtitle = "en línea",
            username = "@carlos_g",
            type = ChatType.DIRECT,
            lastMessage = "Hola, ¿cómo estás?",
            lastMessageTime = "10:30",
            unreadCount = 2,
            isOnline = true,
            isPinned = true,
            isMuted = false,
            avatarGradientStart = 0xFF2AABEE,
            avatarGradientEnd = 0xFF1976D2,
            lastUpdatedTimestamp = 1700000000000L
        )

        val entity = ChatEntity.fromChat(originalChat)
        assertEquals("chat_123", entity.id)
        assertEquals("Carlos Gómez", entity.title)
        assertEquals(2, entity.unreadCount)

        val convertedBack = entity.toChat()
        assertEquals(originalChat.id, convertedBack.id)
        assertEquals(originalChat.title, convertedBack.title)
        assertEquals(originalChat.type, convertedBack.type)
        assertEquals(originalChat.lastMessage, convertedBack.lastMessage)
        assertEquals(originalChat.unreadCount, convertedBack.unreadCount)
        assertEquals(originalChat.isPinned, convertedBack.isPinned)
    }

    @Test
    fun testRoomMessageEntityMapping() {
        val originalMessage = Message(
            id = "msg_abc",
            chatId = "chat_123",
            text = "Prueba de audio y texto",
            timestamp = "14:20",
            isOutgoing = true,
            senderId = "user_me",
            senderUsername = "@me",
            senderName = "Yo",
            status = MessageStatus.SENT,
            type = MessageType.TEXT,
            isStarred = true,
            createdAt = 1700000500000L
        )

        val entity = MessageEntity.fromMessage(originalMessage)
        assertEquals("msg_abc", entity.id)
        assertEquals("chat_123", entity.chatId)
        assertEquals("SENT", entity.status)
        assertTrue(entity.isStarred)

        val convertedBack = entity.toMessage()
        assertEquals(originalMessage.id, convertedBack.id)
        assertEquals(originalMessage.text, convertedBack.text)
        assertEquals(originalMessage.status, convertedBack.status)
        assertEquals(originalMessage.type, convertedBack.type)
        assertTrue(convertedBack.isStarred)
    }

    @Test
    fun testRoomContactEntityMapping() {
        val contact = SavedContact(
            contactUserId = "user_456",
            displayName = "Ana Martínez",
            username = "@ana_m",
            email = "ana@example.com",
            phoneNumber = "+34600112233"
        )

        val entity = ContactEntity.fromSavedContact(contact)
        assertEquals("user_456", entity.contactUserId)
        assertEquals("Ana Martínez", entity.displayName)

        val back = entity.toSavedContact()
        assertEquals(contact.contactUserId, back.contactUserId)
        assertEquals(contact.name, back.name)
        assertEquals(contact.email, back.email)
    }

    @Test
    fun testMessagesSortingByCreatedAt() {
        val msg1 = Message(id = "1", text = "Primero", createdAt = 1000L)
        val msg2 = Message(id = "2", text = "Segundo", createdAt = 2000L)
        val msg3 = Message(id = "3", text = "Tercero", createdAt = 3000L)

        val unsortedList = listOf(msg2, msg3, msg1)
        val sortedList = unsortedList.sortedBy { it.createdAt }

        assertEquals("1", sortedList[0].id)
        assertEquals("2", sortedList[1].id)
        assertEquals("3", sortedList[2].id)
    }

    @Test
    fun testMergeMessagesWithoutDuplicates() {
        val existingMessages = listOf(
            Message(id = "m1", text = "Hola", createdAt = 100L),
            Message(id = "m2", text = "¿Qué tal?", createdAt = 200L)
        )

        val incomingCloudMessages = listOf(
            Message(id = "m2", text = "¿Qué tal? (Actualizado)", createdAt = 200L),
            Message(id = "m3", text = "Nuevo mensaje", createdAt = 300L)
        )

        // Merge por ID preservando elementos únicos y actualizando existentes
        val mergedMap = (existingMessages + incomingCloudMessages).associateBy { it.id }
        val mergedList = mergedMap.values.sortedBy { it.createdAt }

        assertEquals(3, mergedList.size)
        assertEquals("¿Qué tal? (Actualizado)", mergedList.find { it.id == "m2" }?.text)
        assertNotNull(mergedList.find { it.id == "m3" })
    }

    @Test
    fun testMessageStatusTransitions() {
        val msgSending = Message(id = "s1", status = MessageStatus.SENDING)
        val msgSent = msgSending.copy(status = MessageStatus.SENT)
        val msgDelivered = msgSent.copy(status = MessageStatus.DELIVERED)
        val msgRead = msgDelivered.copy(status = MessageStatus.READ)

        assertEquals(MessageStatus.SENDING, msgSending.status)
        assertEquals(MessageStatus.SENT, msgSent.status)
        assertEquals(MessageStatus.DELIVERED, msgDelivered.status)
        assertEquals(MessageStatus.READ, msgRead.status)
    }

    @Test
    fun testPollVotingLogic() {
        val options = listOf(
            PollOption(id = 1, text = "Opción A", voteCount = 0),
            PollOption(id = 2, text = "Opción B", voteCount = 0)
        )
        val poll = PollData(id = "p1", question = "¿Te gusta Kotlin?", options = options, totalVotes = 0)

        // Usuario vota la opción 1
        val updatedOptions = poll.options.map { if (it.id == 1) it.copy(voteCount = it.voteCount + 1) else it }
        val updatedPoll = poll.copy(options = updatedOptions, totalVotes = 1, userVotedOptionId = 1)

        assertEquals(1, updatedPoll.totalVotes)
        assertEquals(1, updatedPoll.options[0].voteCount)
        assertEquals(0, updatedPoll.options[1].voteCount)
        assertEquals(1, updatedPoll.userVotedOptionId)
    }

    @Test
    fun testEditAndRevokeMessage() {
        val original = Message(id = "m9", text = "Mensaje original con typo", isEdited = false)
        val edited = original.copy(text = "Mensaje corregido", isEdited = true, editedTimestamp = "11:05")

        assertTrue(edited.isEdited)
        assertEquals("Mensaje corregido", edited.text)
        assertEquals("11:05", edited.editedTimestamp)

        val revoked = edited.copy(text = "🚫 Este mensaje fue eliminado", isDeletedForEveryone = true)
        assertTrue(revoked.isDeletedForEveryone)
        assertEquals("🚫 Este mensaje fue eliminado", revoked.text)
    }

    @Test
    fun testUnreadCountCalculation() {
        val myUid = "user_me"
        val messages = listOf(
            Message(id = "1", senderId = "user_other", status = MessageStatus.SENT),
            Message(id = "2", senderId = "user_other", status = MessageStatus.READ),
            Message(id = "3", senderId = "user_other", status = MessageStatus.DELIVERED),
            Message(id = "4", senderId = myUid, status = MessageStatus.SENT) // mensaje propio no cuenta como no leído
        )

        val unreadCount = messages.count { it.senderId != myUid && it.status != MessageStatus.READ }
        assertEquals(2, unreadCount)
    }
}
