package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AudioRecorderHelper
import com.example.data.NotificationHelper
import com.example.data.model.AppUpdateInfo
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.GroupConfig
import com.example.data.model.LocationData
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.PollData
import com.example.data.model.PollOption
import com.example.data.model.SavedContact
import com.example.data.model.StatusItem
import com.example.data.model.StatusPrivacy
import com.example.data.model.StatusType
import com.example.data.model.StatusViewer
import com.example.data.model.User
import com.example.data.model.UserStatusGroup
import com.example.data.repository.FirebaseChatRepository
import com.example.data.sample.SampleData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
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
    private val notificationHelper = NotificationHelper(application.applicationContext)
    private val audioRecorderHelper = AudioRecorderHelper(application.applicationContext)

    private val _currentUser = MutableStateFlow<User?>(repository.activeUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isFirebaseSignedIn = MutableStateFlow(repository.isUserSignedIn())
    val isFirebaseSignedIn: StateFlow<Boolean> = _isFirebaseSignedIn.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
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

    // Privacy & Security settings
    private val _privacyPhone = MutableStateFlow("Mis contactos")
    val privacyPhone: StateFlow<String> = _privacyPhone.asStateFlow()

    private val _privacyLastSeen = MutableStateFlow("Todos")
    val privacyLastSeen: StateFlow<String> = _privacyLastSeen.asStateFlow()

    private val _privacyForwardProtect = MutableStateFlow(true)
    val privacyForwardProtect: StateFlow<Boolean> = _privacyForwardProtect.asStateFlow()

    private val _privacyPasscodeLock = MutableStateFlow(false)
    val privacyPasscodeLock: StateFlow<Boolean> = _privacyPasscodeLock.asStateFlow()

    // Notification settings
    private val _showNotifications = MutableStateFlow(true)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _notificationSound = MutableStateFlow("Predeterminado")
    val notificationSound: StateFlow<String> = _notificationSound.asStateFlow()

    private val _notificationVibration = MutableStateFlow(true)
    val notificationVibration: StateFlow<Boolean> = _notificationVibration.asStateFlow()

    // Status / Stories
    private val _userStatusGroups = MutableStateFlow<List<UserStatusGroup>>(emptyList())
    val userStatusGroups: StateFlow<List<UserStatusGroup>> = _userStatusGroups.asStateFlow()

    private val _selectedStatusGroup = MutableStateFlow<UserStatusGroup?>(null)
    val selectedStatusGroup: StateFlow<UserStatusGroup?> = _selectedStatusGroup.asStateFlow()

    private val _selectedStatusIndex = MutableStateFlow(0)
    val selectedStatusIndex: StateFlow<Int> = _selectedStatusIndex.asStateFlow()

    // Contacts search
    private val _searchContactQuery = MutableStateFlow("")
    val searchContactQuery: StateFlow<String> = _searchContactQuery.asStateFlow()

    private val _searchContactResults = MutableStateFlow<List<User>>(emptyList())
    val searchContactResults: StateFlow<List<User>> = _searchContactResults.asStateFlow()

    private val _isSearchingContacts = MutableStateFlow(false)
    val isSearchingContacts: StateFlow<Boolean> = _isSearchingContacts.asStateFlow()

    private val _savedContacts = MutableStateFlow<List<SavedContact>>(emptyList())
    val savedContacts: StateFlow<List<SavedContact>> = _savedContacts.asStateFlow()

    private val _activeGroupConfigs = MutableStateFlow<Map<String, GroupConfig>>(emptyMap())
    val activeGroupConfigs: StateFlow<Map<String, GroupConfig>> = _activeGroupConfigs.asStateFlow()

    private val _registeredCount = MutableStateFlow(0)
    val registeredCount: StateFlow<Int> = _registeredCount.asStateFlow()

    val allUsers: StateFlow<List<User>> = repository.observeAllUsers()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val latestAppUpdate: StateFlow<AppUpdateInfo?> = repository.observeLatestAppUpdate()
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    init {
        initializeSampleData()
        loadRegisteredCount()
    }

    private fun loadRegisteredCount() {
        viewModelScope.launch {
            _registeredCount.value = repository.getRegisteredUsersCount()
        }
    }

    private fun initializeSampleData() {
        val user = repository.activeUser ?: User(
            userId = "user_local",
            displayName = "Paul Usuario",
            username = "@paul_user",
            email = "usuario@paul.com"
        )

        val savedChat = SampleData.createSavedMessagesChat(user)
        val generalGroup = Chat(
            id = "group_general",
            title = "Comunidad Paul Oficial",
            type = ChatType.GROUP,
            lastMessage = "¡Bienvenidos a Paul Messenger!",
            lastMessageTime = "10:00",
            avatarGradientStart = 0xFF7C4DFF,
            avatarGradientEnd = 0xFF536DFE,
            description = "Grupo público de bienvenida para todos los usuarios"
        )
        val botChat = Chat(
            id = "bot_paul",
            title = "Paul Bot Asistente",
            type = ChatType.BOT,
            lastMessage = "Hola, ¿en qué puedo ayudarte hoy?",
            lastMessageTime = "Ayer",
            avatarGradientStart = 0xFF00B0FF,
            avatarGradientEnd = 0xFF0091EA,
            description = "Asistente inteligente de Paul Messenger"
        )

        _chats.value = listOf(savedChat, generalGroup, botChat)

        val sampleGroupMsgs = listOf(
            Message(
                id = "m1",
                chatId = "group_general",
                senderId = "system",
                senderName = "Paul Messenger",
                text = "¡Bienvenidos a la plataforma de mensajería Paul! Disfruta de chats seguros, estados y notas de voz.",
                timestamp = "10:00",
                createdAt = System.currentTimeMillis() - 60000,
                isOutgoing = false
            )
        )
        _messages.value = mapOf("group_general" to sampleGroupMsgs)
    }

    fun loginUser(identifier: String, pass: String, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.loginUser(identifier, pass)
            res.onSuccess { user ->
                _currentUser.value = user
                _isFirebaseSignedIn.value = true
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        pass: String,
        phone: String,
        bio: String,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.registerUser(fullName, username, email, pass, phone, bio)
            res.onSuccess { user ->
                _currentUser.value = user
                _isFirebaseSignedIn.value = true
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al registrar usuario")
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        repository.onGoogleSignInClicked(
            activity = activity,
            scope = viewModelScope,
            onComplete = {
                _currentUser.value = repository.activeUser
                _isFirebaseSignedIn.value = true
                onComplete()
            },
            onError = onError
        )
    }

    fun signOut(onComplete: () -> Unit = {}) {
        repository.signOut {
            _currentUser.value = null
            _isFirebaseSignedIn.value = false
            onComplete()
        }
    }

    fun updateUserProfile(
        newDisplayName: String,
        newBio: String,
        newProfilePictureUrl: String? = null,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.updateUserProfile(newDisplayName, newBio, newProfilePictureUrl)
            res.onSuccess {
                _currentUser.value = repository.activeUser ?: _currentUser.value?.copy(
                    displayName = newDisplayName,
                    bio = newBio,
                    profilePictureUrl = newProfilePictureUrl ?: _currentUser.value?.profilePictureUrl ?: ""
                )
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al actualizar perfil")
            }
        }
    }

    fun setFolder(folder: ChatFolder) { _selectedFolder.value = folder }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun toggleSearch(searching: Boolean) {
        _isSearching.value = searching
        if (!searching) _searchQuery.value = ""
    }
    fun toggleDarkMode() {
        _isDarkMode.value = !(_isDarkMode.value ?: false)
    }

    fun selectChat(chat: Chat?) {
        _selectedChat.value = chat
        _replyingMessage.value = null
    }

    fun createNewChat(title: String, type: ChatType, description: String) {
        val newChat = Chat(
            id = "chat_${System.currentTimeMillis()}",
            title = title,
            type = type,
            description = description,
            lastMessage = "Chat creado",
            lastMessageTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            avatarGradientStart = 0xFF00C853,
            avatarGradientEnd = 0xFF00796B
        )
        _chats.update { listOf(newChat) + it }
        _selectedChat.value = newChat
    }

    fun deleteChat(chatId: String) {
        _chats.update { it.filter { c -> c.id != chatId } }
        _messages.update { it.toMutableMap().apply { remove(chatId) } }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.value = null
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

    fun toggleBlockChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isBlocked = !it.isBlocked) else it }
        }
    }

    fun unblockChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isBlocked = false) else it }
        }
    }

    fun sendMessage(chatId: String, text: String, isViewOnce: Boolean = false) {
        if (text.isBlank()) return
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = text,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true,
            status = MessageStatus.SENT,
            isViewOnce = isViewOnce,
            replyToMessageId = _replyingMessage.value?.id,
            replyToText = _replyingMessage.value?.text,
            replyToSender = _replyingMessage.value?.senderName
        )

        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(lastMessage = text, lastMessageTime = time)
                else it
            }
        }
        _replyingMessage.value = null
    }

    fun sendVoiceMessage(chatId: String, durationSeconds: Int) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = "🎤 Nota de voz (${durationSeconds}s)",
            type = MessageType.VOICE,
            durationSeconds = durationSeconds,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun sendPhotoMessage(chatId: String, url: String, caption: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = caption.ifBlank { "📷 Foto" },
            type = MessageType.PHOTO,
            mediaUrl = url,
            mediaCaption = caption,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun sendVideoMessage(chatId: String, url: String, caption: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = caption.ifBlank { "🎬 Video" },
            type = MessageType.VIDEO,
            mediaUrl = url,
            mediaCaption = caption,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun sendFileMessage(chatId: String, url: String, fileName: String, fileSize: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = "📎 $fileName",
            type = MessageType.FILE,
            mediaUrl = url,
            fileName = fileName,
            fileSize = fileSize,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun sendGifMessage(chatId: String, title: String, url: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = title,
            type = MessageType.GIF,
            mediaUrl = url,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun sendLocationMessage(chatId: String, loc: LocationData) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = "📍 ${loc.address}",
            type = MessageType.LOCATION,
            location = loc,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun sendPoll(chatId: String, question: String, options: List<String>) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val pollData = PollData(
            question = question,
            options = options.mapIndexed { idx, opt -> PollOption(id = "opt_$idx", text = opt, votes = 0) }
        )
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = "📊 Encuesta: $question",
            type = MessageType.POLL,
            poll = pollData,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun votePoll(messageId: String, optionId: String) {
        _messages.update { map ->
            map.mapValues { (_, msgs) ->
                msgs.map { msg ->
                    if (msg.id == messageId && msg.poll != null) {
                        val currentPoll = msg.poll
                        val updatedOpts = currentPoll.options.map { opt ->
                            if (opt.id == optionId) opt.copy(votes = opt.votes + 1) else opt
                        }
                        msg.copy(poll = currentPoll.copy(options = updatedOpts, totalVotes = currentPoll.totalVotes + 1))
                    } else msg
                }
            }
        }
    }

    fun sendContactMessage(chatId: String, name: String, phone: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            text = "👤 Contacto: $name ($phone)",
            type = MessageType.CONTACT,
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true
        )
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (list + msg))
        }
    }

    fun startVoiceRecording() {
        val started = audioRecorderHelper.startRecording()
        if (started) {
            _isRecordingVoice.value = true
            _recordingSeconds.value = 0
            recordingTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _recordingSeconds.update { it + 1 }
                }
            }
        }
    }

    fun stopVoiceRecordingAndSend(chatId: String) {
        recordingTimerJob?.cancel()
        _isRecordingVoice.value = false
        val secs = _recordingSeconds.value
        val path = audioRecorderHelper.stopRecording()
        if (secs > 0) {
            sendVoiceMessage(chatId, secs)
        }
    }

    fun cancelVoiceRecording() {
        recordingTimerJob?.cancel()
        _isRecordingVoice.value = false
        _recordingSeconds.value = 0
        audioRecorderHelper.cancelRecording()
    }

    fun addReaction(messageId: String, emoji: String) {
        _messages.update { map ->
            map.mapValues { (_, msgs) ->
                msgs.map { msg ->
                    if (msg.id == messageId) {
                        val currentReactions = msg.reactions.toMutableMap()
                        val count = currentReactions[emoji] ?: 0
                        currentReactions[emoji] = count + 1
                        msg.copy(reactions = currentReactions, userReaction = emoji)
                    } else msg
                }
            }
        }
    }

    fun setReplyingMessage(message: Message?) { _replyingMessage.value = message }

    fun markMessageAsViewed(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId] ?: return@update map
            map + (chatId to list.map { if (it.id == messageId) it.copy(isViewed = true) else it })
        }
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        _messages.update { map ->
            val list = map[chatId] ?: return@update map
            map + (chatId to list.map { if (it.id == messageId) it.copy(text = newText, isEdited = true) else it })
        }
    }

    fun deleteMessageForMe(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId] ?: return@update map
            map + (chatId to list.filter { it.id != messageId })
        }
    }

    fun deleteMessageForEveryone(chatId: String, messageId: String) {
        deleteMessageForMe(chatId, messageId)
    }

    fun toggleStarMessage(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId] ?: return@update map
            map + (chatId to list.map { if (it.id == messageId) it.copy(isStarred = !it.isStarred) else it })
        }
    }

    fun forwardMessage(sourceMsg: Message, targetChatId: String, keepAuthor: Boolean) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val fwd = sourceMsg.copy(
            id = "msg_${UUID.randomUUID()}",
            chatId = targetChatId,
            senderId = _currentUser.value?.userId ?: "me",
            senderName = _currentUser.value?.name ?: "Yo",
            timestamp = time,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true,
            isForwarded = true,
            originalAuthor = if (keepAuthor) sourceMsg.senderName else ""
        )
        _messages.update { map ->
            val list = map[targetChatId] ?: emptyList()
            map + (targetChatId to (list + fwd))
        }
    }

    fun setDisappearingTimer(chatId: String, seconds: Int) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(disappearingMessageDuration = seconds) else it }
        }
    }

    fun toggleChatLock(chatId: String, pin: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isLocked = pin.isNotBlank(), lockPin = pin) else it }
        }
    }

    fun reportUser(chatId: String, reason: String) {}

    suspend fun searchUserInFirebase(query: String): User? = repository.searchUserInFirebase(query)

    fun addContactAndOpenChat(title: String, username: String, phone: String, isFirebase: Boolean) {
        val newChat = Chat(
            id = "chat_${System.currentTimeMillis()}",
            title = title,
            otherUserUsername = username,
            lastMessage = "¡Hola! Ahora estamos conectados.",
            lastMessageTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            avatarGradientStart = if (isFirebase) 0xFF00C853L else 0xFFFF9800L,
            avatarGradientEnd = if (isFirebase) 0xFF00796BL else 0xFFE65100L
        )
        _chats.update { listOf(newChat) + it }
        _selectedChat.value = newChat
    }

    fun openStatusGroup(group: UserStatusGroup) {
        _selectedStatusGroup.value = group
        _selectedStatusIndex.value = 0
    }

    fun addStatusItem(item: StatusItem) {
        val currentGroup = _userStatusGroups.value.find { it.userId == item.userId }
        if (currentGroup != null) {
            val updated = currentGroup.copy(items = currentGroup.items + item)
            _userStatusGroups.update { list -> list.map { if (it.userId == item.userId) updated else it } }
        } else {
            val newGroup = UserStatusGroup(
                userId = item.userId,
                displayName = item.displayName,
                userAvatarStart = item.userAvatarStart,
                userAvatarEnd = item.userAvatarEnd,
                items = listOf(item),
                hasUnseen = true
            )
            _userStatusGroups.update { listOf(newGroup) + it }
        }
    }

    fun markStatusViewed(groupId: String, index: Int) {
        _selectedStatusIndex.value = index
    }

    fun observeGroupConfig(groupId: String) {}

    fun updateGroupConfig(groupId: String, config: GroupConfig) {
        _activeGroupConfigs.update { it + (groupId to config) }
    }

    fun banUserFromGroup(groupId: String, targetUserId: String, massDelete: Boolean) {}
    fun unbanUserFromGroup(groupId: String, targetUserId: String) {}

    fun toggleUserBan(targetUserId: String, isBanned: Boolean) {
        viewModelScope.launch { repository.toggleUserBan(targetUserId, isBanned) }
    }

    fun updateUserRole(userId: String, newRole: String, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.updateUserRole(userId, newRole)
            res.onSuccess { onComplete() }.onFailure { onError(it.localizedMessage ?: "Error") }
        }
    }

    fun setPrivacyPhone(value: String) { _privacyPhone.value = value }
    fun setPrivacyLastSeen(value: String) { _privacyLastSeen.value = value }
    fun setPrivacyForwardProtect(value: Boolean) { _privacyForwardProtect.value = value }
    fun setPrivacyPasscodeLock(value: Boolean) { _privacyPasscodeLock.value = value }
    fun setShowNotifications(value: Boolean) { _showNotifications.value = value }
    fun setNotificationSound(value: String) { _notificationSound.value = value }
    fun setNotificationVibration(value: Boolean) { _notificationVibration.value = value }

    fun createCloudBackup(onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        onComplete()
    }

    fun restoreCloudBackup(onComplete: (Int) -> Unit = {}, onError: (String) -> Unit = {}) {
        onComplete(_chats.value.size)
    }
}
