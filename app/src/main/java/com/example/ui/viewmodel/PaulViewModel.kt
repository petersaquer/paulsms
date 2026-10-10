package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.LocationData
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.PollData
import com.example.data.model.PollOption
import com.example.data.model.User
import com.example.data.repository.FirebaseChatRepository
import com.example.data.sample.SampleData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ChatFolder(val label: String) {
    ALL("Todos"),
    DIRECT("Privados"),
    GROUPS("Grupos"),
    CHANNELS("Canales"),
    BOTS("Bots")
}

class PaulViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseChatRepository(application.applicationContext)

    private val _currentUser = MutableStateFlow<User?>(repository.currentUser ?: SampleData.currentUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isFirebaseSignedIn = MutableStateFlow(repository.isUserSignedIn())
    val isFirebaseSignedIn: StateFlow<Boolean> = _isFirebaseSignedIn.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(SampleData.initialChats())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(SampleData.initialMessages())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _selectedChat = MutableStateFlow<Chat?>(null)
    val selectedChat: StateFlow<Chat?> = _selectedChat.asStateFlow()

    private val _selectedFolder = MutableStateFlow(ChatFolder.ALL)
    val selectedFolder: StateFlow<ChatFolder> = _selectedFolder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _replyingMessage = MutableStateFlow<Message?>(null)
    val replyingMessage: StateFlow<Message?> = _replyingMessage.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private var recordingTimerJob: Job? = null

    // Privacy and Security settings
    private val _privacyPhone = MutableStateFlow("Mis contactos")
    val privacyPhone: StateFlow<String> = _privacyPhone.asStateFlow()

    private val _privacyLastSeen = MutableStateFlow("Todos")
    val privacyLastSeen: StateFlow<String> = _privacyLastSeen.asStateFlow()

    private val _privacyProfilePhoto = MutableStateFlow("Todos")
    val privacyProfilePhoto: StateFlow<String> = _privacyProfilePhoto.asStateFlow()

    private val _privacyForwardProtect = MutableStateFlow(true)
    val privacyForwardProtect: StateFlow<Boolean> = _privacyForwardProtect.asStateFlow()

    private val _privacyPasscodeLock = MutableStateFlow(false)
    val privacyPasscodeLock: StateFlow<Boolean> = _privacyPasscodeLock.asStateFlow()

    private val _accountSelfDestructPeriod = MutableStateFlow("6 meses")
    val accountSelfDestructPeriod: StateFlow<String> = _accountSelfDestructPeriod.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDemoFirebaseUsers()
            _isFirebaseSignedIn.value = repository.isUserSignedIn()
            if (repository.isUserSignedIn()) {
                _currentUser.value = repository.currentUser
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onComplete: () -> Unit, onError: (String) -> Unit) {
        repository.onGoogleSignInClicked(
            activity = activity,
            onAuthSuccess = {
                _isFirebaseSignedIn.value = true
                _currentUser.value = repository.currentUser
                onComplete()
            },
            onAuthError = onError,
            scope = viewModelScope
        )
    }

    fun signOut(onComplete: () -> Unit) {
        repository.signOut(viewModelScope) {
            _isFirebaseSignedIn.value = false
            _currentUser.value = SampleData.currentUser
            onComplete()
        }
    }

    suspend fun searchUserInFirebase(query: String): User? {
        return repository.searchUserInFirebase(query)
    }

    fun addContactAndOpenChat(title: String, username: String, phone: String, isFirebase: Boolean) {
        val newId = "contact_" + UUID.randomUUID().toString().take(6)
        val currentUid = _currentUser.value?.userId ?: "local_user"
        val partnerId = if (isFirebase) username.ifEmpty { phone } else "e2ee_$newId"
        val fingerprint = if (!isFirebase) repository.generateE2EEFingerprint(currentUid, partnerId) else null

        val newChat = Chat(
            id = newId,
            title = title,
            subtitle = if (isFirebase) "en línea • Firebase Cloud" else "🔒 Chat Secreto E2EE",
            username = username,
            type = if (isFirebase) ChatType.DIRECT else ChatType.SECRET,
            lastMessage = if (isFirebase) "Chat de Firebase Cloud iniciado" else "🔒 Chat Secreto iniciado con cifrado E2EE",
            lastMessageTime = getCurrentTimeString(),
            unreadCount = 0,
            isOnline = isFirebase,
            isEncrypted = !isFirebase,
            isCloudSynced = isFirebase,
            encryptionKeyFingerprint = fingerprint,
            contactPhone = phone,
            avatarGradientStart = if (isFirebase) 0xFF00C853L else 0xFFFF9800L,
            avatarGradientEnd = if (isFirebase) 0xFF00796BL else 0xFFE65100L
        )

        val initialWelcomeMessage = Message(
            id = "msg_" + UUID.randomUUID().toString().take(6),
            chatId = newId,
            text = if (isFirebase) {
                "✨ Has iniciado una conversación con $title conectada a Firebase Cloud Database."
            } else {
                "🔒 Chat Secreto activado con cifrado de extremo a extremo.\n• Las claves nunca salen de este dispositivo\n• Sin almacenamiento en la nube\n• Huella criptográfica: ${fingerprint ?: "#D4A8"}"
            },
            timestamp = getCurrentTimeString(),
            isOutgoing = false,
            senderName = title,
            isEncrypted = !isFirebase,
            status = MessageStatus.READ
        )

        _chats.update { listOf(newChat) + it }
        _messages.update { it + (newId to listOf(initialWelcomeMessage)) }

        selectChat(newChat)
    }

    fun selectChat(chat: Chat?) {
        _selectedChat.value = chat
        _replyingMessage.value = null
        if (chat != null && chat.unreadCount > 0) {
            _chats.update { list ->
                list.map { if (it.id == chat.id) it.copy(unreadCount = 0) else it }
            }
        }
    }

    fun setFolder(folder: ChatFolder) {
        _selectedFolder.value = folder
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch(active: Boolean) {
        _isSearching.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.update { current ->
            when (current) {
                null -> true
                true -> false
                false -> true
            }
        }
    }

    fun setReplyingMessage(msg: Message?) {
        _replyingMessage.value = msg
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    fun sendMessage(chatId: String, text: String, isViewOnce: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val targetChat = _chats.value.find { it.id == chatId }
        if (targetChat?.isBlocked == true) return

        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)

        val time = getCurrentTimeString()
        val newMessage = Message(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = trimmed,
            timestamp = time,
            isOutgoing = true,
            isEncrypted = isEncrypted,
            isViewOnce = isViewOnce,
            hasBeenViewed = false,
            status = MessageStatus.READ,
            replyTo = _replyingMessage.value
        )

        _replyingMessage.value = null

        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (chatMessages + newMessage))
        }

        val displayLastMessage = if (isViewOnce) "🔒 Mensaje de lectura única (1)" else trimmed
        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(
                        lastMessage = displayLastMessage,
                        lastMessageTime = time
                    )
                } else it
            }
        }

        // Auto replies for interactive contacts/bot
        if (chatId == "paul_bot") {
            handleBotResponse(trimmed)
        } else if (chatId == "elena" || (targetChat != null && !targetChat.isCloudSynced && targetChat.type == ChatType.SECRET)) {
            handleContactAutoReply(chatId, targetChat?.title ?: "Contacto", isEncrypted)
        }
    }

    fun markMessageAsViewed(chatId: String, messageId: String) {
        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            val updated = chatMessages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(hasBeenViewed = true, text = "💥 [Mensaje autodestruido]")
                } else msg
            }
            currentMap + (chatId to updated)
        }
        _chats.update { list ->
            list.map {
                if (it.id == chatId && it.lastMessage.contains("lectura única", ignoreCase = true)) {
                    it.copy(lastMessage = "💥 Mensaje de lectura única expirado")
                } else it
            }
        }
    }

    fun sendVoiceMessage(chatId: String, durationSeconds: Int) {
        val targetChat = _chats.value.find { it.id == chatId }
        val isEncrypted = targetChat?.isEncrypted ?: false

        val time = getCurrentTimeString()
        val waveform = List(16) { ((it * 3 + 2) % 10 + 2) / 12f }
        val newMessage = Message(
            id = "voice_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = "Mensaje de voz",
            timestamp = time,
            isOutgoing = true,
            type = MessageType.VOICE,
            isEncrypted = isEncrypted,
            voiceDurationSeconds = durationSeconds.coerceAtLeast(1),
            voiceWaveform = waveform,
            status = MessageStatus.READ
        )

        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (chatMessages + newMessage))
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(
                        lastMessage = "🎤 Mensaje de voz (0:${if (durationSeconds < 10) "0$durationSeconds" else "$durationSeconds"})",
                        lastMessageTime = time
                    )
                } else it
            }
        }

        if (chatId == "paul_bot") {
            handleBotResponse("audio")
        }
    }

    fun sendGifMessage(chatId: String, title: String, gifUrl: String) {
        val targetChat = _chats.value.find { it.id == chatId }
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()
        val newMessage = Message(
            id = "gif_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = title,
            timestamp = time,
            isOutgoing = true,
            type = MessageType.GIF,
            gifUrl = gifUrl,
            isEncrypted = isEncrypted,
            status = MessageStatus.READ
        )

        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (chatMessages + newMessage))
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(
                        lastMessage = "GIF: $title",
                        lastMessageTime = time
                    )
                } else it
            }
        }
    }

    fun sendLocationMessage(chatId: String, location: LocationData) {
        val targetChat = _chats.value.find { it.id == chatId }
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()
        val isLive = location.isLive
        val newMessage = Message(
            id = "loc_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = if (isLive) "📍 Ubicación en tiempo real" else "📍 ${location.title}",
            timestamp = time,
            isOutgoing = true,
            type = if (isLive) MessageType.LIVE_LOCATION else MessageType.LOCATION,
            locationData = location,
            isEncrypted = isEncrypted,
            status = MessageStatus.READ
        )

        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (chatMessages + newMessage))
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(
                        lastMessage = if (isLive) "📍 En directo (${location.liveDurationMinutes} min)" else "📍 ${location.title}",
                        lastMessageTime = time
                    )
                } else it
            }
        }
    }

    fun sendPoll(chatId: String, question: String, optionTexts: List<String>) {
        val time = getCurrentTimeString()
        val options = optionTexts.mapIndexed { idx, title ->
            PollOption(id = idx + 1, text = title, voteCount = 0)
        }
        val pollData = PollData(
            id = "poll_" + UUID.randomUUID().toString().take(8),
            question = question,
            options = options,
            totalVotes = 0,
            userVotedOptionId = null,
            isAnonymous = true
        )

        val newMessage = Message(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = "Encuesta: $question",
            timestamp = time,
            isOutgoing = true,
            type = MessageType.POLL,
            pollData = pollData,
            status = MessageStatus.READ
        )

        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (chatMessages + newMessage))
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(
                        lastMessage = "📊 Encuesta: $question",
                        lastMessageTime = time
                    )
                } else it
            }
        }
    }

    fun votePoll(messageId: String, optionId: Int) {
        _messages.update { currentMap ->
            currentMap.mapValues { (_, messageList) ->
                messageList.map { msg ->
                    if (msg.id == messageId && msg.pollData != null) {
                        val currentPoll = msg.pollData
                        val previousVote = currentPoll.userVotedOptionId
                        if (previousVote == optionId) {
                            val newOptions = currentPoll.options.map { opt ->
                                if (opt.id == optionId) opt.copy(voteCount = (opt.voteCount - 1).coerceAtLeast(0))
                                else opt
                            }
                            msg.copy(
                                pollData = currentPoll.copy(
                                    options = newOptions,
                                    totalVotes = (currentPoll.totalVotes - 1).coerceAtLeast(0),
                                    userVotedOptionId = null
                                )
                            )
                        } else {
                            val newOptions = currentPoll.options.map { opt ->
                                when (opt.id) {
                                    optionId -> opt.copy(voteCount = opt.voteCount + 1)
                                    previousVote -> opt.copy(voteCount = (opt.voteCount - 1).coerceAtLeast(0))
                                    else -> opt
                                }
                            }
                            val voteDelta = if (previousVote == null) 1 else 0
                            msg.copy(
                                pollData = currentPoll.copy(
                                    options = newOptions,
                                    totalVotes = currentPoll.totalVotes + voteDelta,
                                    userVotedOptionId = optionId
                                )
                            )
                        }
                    } else msg
                }
            }
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        _messages.update { currentMap ->
            currentMap.mapValues { (_, msgList) ->
                msgList.map { msg ->
                    if (msg.id == messageId) {
                        val currentReactions = msg.reactions.toMutableMap()
                        val previous = msg.userReaction
                        if (previous == emoji) {
                            val count = (currentReactions[emoji] ?: 1) - 1
                            if (count <= 0) currentReactions.remove(emoji)
                            else currentReactions[emoji] = count
                            msg.copy(reactions = currentReactions, userReaction = null)
                        } else {
                            if (previous != null) {
                                val prevCount = (currentReactions[previous] ?: 1) - 1
                                if (prevCount <= 0) currentReactions.remove(previous)
                                else currentReactions[previous] = prevCount
                            }
                            currentReactions[emoji] = (currentReactions[emoji] ?: 0) + 1
                            msg.copy(reactions = currentReactions, userReaction = emoji)
                        }
                    } else msg
                }
            }
        }
    }

    fun togglePinChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it }
        }
    }

    fun toggleMuteChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isMuted = !it.isMuted) else it }
        }
    }

    fun deleteChat(chatId: String) {
        _chats.update { list -> list.filterNot { it.id == chatId } }
        _messages.update { currentMap -> currentMap - chatId }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.value = null
        }
    }

    fun toggleBlockChat(chatId: String) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    val newBlocked = !it.isBlocked
                    it.copy(
                        isBlocked = newBlocked,
                        subtitle = if (newBlocked) "🚫 Bloqueado" else if (it.type == ChatType.BOT) "bot" else if (it.isOnline) "en línea" else "última vez recientemente"
                    )
                } else it
            }
        }
        _selectedChat.update { current ->
            if (current?.id == chatId) {
                val newBlocked = !current.isBlocked
                current.copy(
                    isBlocked = newBlocked,
                    subtitle = if (newBlocked) "🚫 Bloqueado" else if (current.type == ChatType.BOT) "bot" else if (current.isOnline) "en línea" else "última vez recientemente"
                )
            } else current
        }
    }

    fun unblockChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isBlocked = false, subtitle = if (it.type == ChatType.BOT) "bot" else if (it.isOnline) "en línea" else "última vez recientemente") else it }
        }
        _selectedChat.update { current ->
            if (current?.id == chatId) current.copy(isBlocked = false, subtitle = if (current.type == ChatType.BOT) "bot" else if (current.isOnline) "en línea" else "última vez recientemente") else current
        }
    }

    fun blockChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isBlocked = true, subtitle = "🚫 Bloqueado") else it }
        }
        _selectedChat.update { current ->
            if (current?.id == chatId) current.copy(isBlocked = true, subtitle = "🚫 Bloqueado") else current
        }
    }

    fun pinMessageInChat(chatId: String, messageText: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(pinnedMessageText = messageText) else it }
        }
        _selectedChat.update { current ->
            if (current?.id == chatId) current.copy(pinnedMessageText = messageText) else current
        }
    }

    fun unpinMessageInChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(pinnedMessageText = null) else it }
        }
        _selectedChat.update { current ->
            if (current?.id == chatId) current.copy(pinnedMessageText = null) else current
        }
    }

    fun deleteSingleMessage(chatId: String, messageId: String) {
        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to chatMessages.filterNot { it.id == messageId })
        }
    }

    fun setPrivacyPhone(value: String) { _privacyPhone.value = value }
    fun setPrivacyLastSeen(value: String) { _privacyLastSeen.value = value }
    fun setPrivacyProfilePhoto(value: String) { _privacyProfilePhoto.value = value }
    fun setPrivacyForwardProtect(value: Boolean) { _privacyForwardProtect.value = value }
    fun setPrivacyPasscodeLock(value: Boolean) { _privacyPasscodeLock.value = value }
    fun setAccountSelfDestructPeriod(value: String) { _accountSelfDestructPeriod.value = value }

    fun createNewChat(title: String, type: ChatType, description: String) {
        val newId = "chat_" + UUID.randomUUID().toString().take(6)
        val (startColor, endColor) = when (type) {
            ChatType.GROUP -> 0xFFFF9800L to 0xFFE65100L
            ChatType.CHANNEL -> 0xFF9C27B0L to 0xFF4A148CL
            ChatType.BOT -> 0xFF00C853L to 0xFF1B5E20L
            ChatType.SECRET -> 0xFF00C853L to 0xFF00796BL
            else -> 0xFF03A9F4L to 0xFF0288D1L
        }

        val isEncrypted = type == ChatType.SECRET
        val newChat = Chat(
            id = newId,
            title = title,
            subtitle = if (type == ChatType.CHANNEL) "1 suscriptor" else if (type == ChatType.GROUP) "1 miembro" else if (type == ChatType.SECRET) "🔒 Chat Secreto E2EE" else "recién creado",
            username = "@" + title.lowercase().replace(" ", "_"),
            type = type,
            isEncrypted = isEncrypted,
            isCloudSynced = !isEncrypted && type != ChatType.BOT,
            encryptionKeyFingerprint = if (isEncrypted) repository.generateE2EEFingerprint("current", newId) else null,
            lastMessage = if (type == ChatType.CHANNEL) "Canal creado" else if (type == ChatType.GROUP) "Grupo creado" else if (type == ChatType.SECRET) "🔒 Chat Secreto iniciado" else "Chat iniciado",
            lastMessageTime = getCurrentTimeString(),
            unreadCount = 0,
            avatarGradientStart = startColor,
            avatarGradientEnd = endColor
        )

        _chats.update { listOf(newChat) + it }
        _messages.update { it + (newId to listOf(
            Message(
                id = "init_" + UUID.randomUUID().toString().take(6),
                chatId = newId,
                text = if (description.isNotBlank()) description else if (isEncrypted) "🔒 Este chat cuenta con cifrado de extremo a extremo." else "¡Bienvenido a $title!",
                timestamp = getCurrentTimeString(),
                isOutgoing = false,
                senderName = title,
                isEncrypted = isEncrypted,
                status = MessageStatus.READ
            )
        )) }

        selectChat(newChat)
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _recordingSeconds.value = 0
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _recordingSeconds.update { it + 1 }
            }
        }
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        _recordingSeconds.value = 0
        recordingTimerJob?.cancel()
    }

    fun stopVoiceRecordingAndSend(chatId: String) {
        val seconds = _recordingSeconds.value
        cancelVoiceRecording()
        if (seconds >= 1) {
            sendVoiceMessage(chatId, seconds)
        }
    }

    private fun handleBotResponse(userPrompt: String) {
        viewModelScope.launch {
            _chats.update { list ->
                list.map { if (it.id == "paul_bot") it.copy(typingStatus = "escribiendo...") else it }
            }
            if (_selectedChat.value?.id == "paul_bot") {
                _selectedChat.update { it?.copy(typingStatus = "escribiendo...") }
            }

            delay(1200)

            val replyText = when {
                userPrompt.equals("/ayuda", ignoreCase = true) ->
                    "📋 **Comandos de Paul Bot:**\n\n• /ayuda - Muestra esta ayuda\n• /firebase - Estado de conexión a la nube\n• /e2ee - Verificación de cifrado extremo a extremo\n• /clima - Pronóstico del tiempo actual\n• /chiste - Humor dev"
                userPrompt.equals("/firebase", ignoreCase = true) ->
                    "☁️ **Estado Firebase Cloud:**\nBase de datos: Conectada (${getApplication<Application>().getString(com.example.R.string.firestore_database_id).take(20)}...)\nUsuarios en la nube: Detectados y sincronizados\nBúsqueda por @usuario y teléfono: Activa"
                userPrompt.equals("/e2ee", ignoreCase = true) ->
                    "🔒 **Cifrado Extremo a Extremo (E2EE):**\nLos contactos que no se encuentran en la base de datos de Firebase se comunican a través de Chats Secretos con cifrado local exclusivo. ¡Tus datos están protegidos!"
                userPrompt.equals("/clima", ignoreCase = true) ->
                    "☀️ **Clima hoy:**\nTemperatura: 23°C\nCondición: Despejado y soleado\n¡Excelente día para mensajear en Paul!"
                userPrompt.equals("/chiste", ignoreCase = true) ->
                    "😄 ¿Por qué los programadores confían en Firebase y E2EE?\n¡Porque uno sincroniza las bases de datos y el otro no deja rastro para los espías! 🔒⚡"
                else ->
                    "🤖 Recibido: \"$userPrompt\"\nPuedes agregar contactos buscando su @usuario o teléfono. Si están en Firebase se integran a la nube, y los demás pasan a cifrado E2EE."
            }

            val time = getCurrentTimeString()
            val botMessage = Message(
                id = "bot_reply_" + UUID.randomUUID().toString().take(8),
                chatId = "paul_bot",
                text = replyText,
                timestamp = time,
                isOutgoing = false,
                senderName = "Paul Bot",
                status = MessageStatus.READ
            )

            _messages.update { currentMap ->
                val list = currentMap["paul_bot"] ?: emptyList()
                currentMap + ("paul_bot" to (list + botMessage))
            }

            _chats.update { list ->
                list.map {
                    if (it.id == "paul_bot") {
                        it.copy(
                            lastMessage = replyText.take(50) + "...",
                            lastMessageTime = time,
                            typingStatus = null
                        )
                    } else it
                }
            }

            if (_selectedChat.value?.id == "paul_bot") {
                _selectedChat.update { it?.copy(typingStatus = null) }
            }
        }
    }

    private fun handleContactAutoReply(chatId: String, contactName: String, isEncrypted: Boolean) {
        viewModelScope.launch {
            _chats.update { list ->
                list.map { if (it.id == chatId) it.copy(typingStatus = "escribiendo...") else it }
            }
            if (_selectedChat.value?.id == chatId) {
                _selectedChat.update { it?.copy(typingStatus = "escribiendo...") }
            }

            delay(1800)

            val replyText = if (isEncrypted) {
                "🔒 Mensaje cifrado recibido y desencriptado en el dispositivo con éxito."
            } else {
                "¡Recibido! Gracias por el mensaje 🚀"
            }
            val time = getCurrentTimeString()
            val contactMessage = Message(
                id = "contact_reply_" + UUID.randomUUID().toString().take(8),
                chatId = chatId,
                text = replyText,
                timestamp = time,
                isOutgoing = false,
                isEncrypted = isEncrypted,
                senderName = contactName,
                status = MessageStatus.READ
            )

            _messages.update { currentMap ->
                val list = currentMap[chatId] ?: emptyList()
                currentMap + (chatId to (list + contactMessage))
            }

            _chats.update { list ->
                list.map {
                    if (it.id == chatId) {
                        it.copy(
                            lastMessage = replyText,
                            lastMessageTime = time,
                            typingStatus = null
                        )
                    } else it
                }
            }

            if (_selectedChat.value?.id == chatId) {
                _selectedChat.update { it?.copy(typingStatus = null) }
            }
        }
    }
}
