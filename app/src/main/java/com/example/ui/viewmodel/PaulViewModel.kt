package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppUpdateInfo
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.DeviceContact
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
import com.example.data.model.GroupConfig
import com.example.data.model.AuditLogEntry
import com.example.data.model.UserStatusGroup
import com.example.data.NotificationHelper
import com.example.data.repository.FirebaseChatRepository
import com.example.data.sample.SampleData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    private val _currentUser = MutableStateFlow<User?>(repository.activeUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isFirebaseSignedIn = MutableStateFlow(repository.isUserSignedIn())
    val isFirebaseSignedIn: StateFlow<Boolean> = _isFirebaseSignedIn.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(
        repository.activeUser?.let { listOf(SampleData.createSavedMessagesChat(it)) } ?: emptyList()
    )
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
    private var messagesListenerJob: Job? = null

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

    // Notification settings
    private val _showNotifications = MutableStateFlow(true)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _notificationSound = MutableStateFlow("Predeterminado")
    val notificationSound: StateFlow<String> = _notificationSound.asStateFlow()

    private val _notificationVibration = MutableStateFlow(true)
    val notificationVibration: StateFlow<Boolean> = _notificationVibration.asStateFlow()

    private val notificationHelper = NotificationHelper(application.applicationContext)
    private val globalListeners = mutableMapOf<String, Job>()
    private val seenMessageIds = mutableSetOf<String>()

    private val _accountSelfDestructPeriod = MutableStateFlow("6 meses")
    val accountSelfDestructPeriod: StateFlow<String> = _accountSelfDestructPeriod.asStateFlow()

    // Real list of all users for the Admin Panel
    val allUsers: StateFlow<List<User>> = repository.observeAllUsers()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // Real-time In-App Updates
    val latestAppUpdate: StateFlow<AppUpdateInfo?> = repository.observeLatestAppUpdate()
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    private val _registeredCount = MutableStateFlow(0)
    val registeredCount: StateFlow<Int> = _registeredCount.asStateFlow()

    // Searchable Contact List & Firestore Queries
    private val _searchContactQuery = MutableStateFlow("")
    val searchContactQuery: StateFlow<String> = _searchContactQuery.asStateFlow()

    private val _searchContactResults = MutableStateFlow<List<User>>(emptyList())
    val searchContactResults: StateFlow<List<User>> = _searchContactResults.asStateFlow()

    private val _isSearchingContacts = MutableStateFlow(false)
    val isSearchingContacts: StateFlow<Boolean> = _isSearchingContacts.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val savedContacts: StateFlow<List<SavedContact>> = combine(
        repository.getLocalContactsFlow(),
        _currentUser.flatMapLatest { user ->
            if (user != null && user.userId.isNotBlank()) {
                repository.observeSavedContacts(user.userId, viewModelScope)
            } else {
                flowOf(emptyList())
            }
        }
    ) { local, cloud ->
        val cloudIds = cloud.map { it.contactUserId }.toSet()
        (local.filterNot { it.contactUserId in cloudIds } + cloud).sortedByDescending { it.addedAt }
    }.catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // WhatsApp-Style Estados & Reels
    val activeStatuses: StateFlow<List<StatusItem>> = repository.observeActiveStatuses()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val userStatusGroups: StateFlow<List<UserStatusGroup>> = combine(
        activeStatuses,
        _currentUser
    ) { statuses, current ->
        val currentUid = current?.userId ?: ""
        val grouped = statuses.groupBy { it.userId }
        val list = mutableListOf<UserStatusGroup>()

        // 1. Current user group if any
        if (current != null) {
            val myStatuses = grouped[currentUid] ?: emptyList()
            if (myStatuses.isNotEmpty()) {
                list.add(
                    UserStatusGroup(
                        userId = currentUid,
                        displayName = current.name,
                        username = current.username,
                        userAvatarStart = current.avatarGradientStart,
                        userAvatarEnd = current.avatarGradientEnd,
                        isCurrentUser = true,
                        statuses = myStatuses,
                        hasUnseen = false
                    )
                )
            }
        }

        // 2. Other users' groups
        val otherGroups = grouped.filterKeys { it != currentUid }.map { (uId, userStatuses) ->
            val first = userStatuses.first()
            val hasUnseen = userStatuses.any { s -> s.viewers.none { it.userId == currentUid } }
            UserStatusGroup(
                userId = uId,
                displayName = first.displayName,
                username = first.username,
                userAvatarStart = first.userAvatarStart,
                userAvatarEnd = first.userAvatarEnd,
                isCurrentUser = false,
                statuses = userStatuses,
                hasUnseen = hasUnseen
            )
        }.sortedWith(
            compareByDescending<UserStatusGroup> { it.hasUnseen }
                .thenByDescending { it.latestTimestamp }
        )

        list.addAll(otherGroups)
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    private val _selectedStatusGroup = MutableStateFlow<UserStatusGroup?>(null)
    val selectedStatusGroup: StateFlow<UserStatusGroup?> = _selectedStatusGroup.asStateFlow()

    private val _selectedStatusIndex = MutableStateFlow(0)
    val selectedStatusIndex: StateFlow<Int> = _selectedStatusIndex.asStateFlow()

    private val _defaultStatusPrivacy = MutableStateFlow(StatusPrivacy.MY_CONTACTS)
    val defaultStatusPrivacy: StateFlow<StatusPrivacy> = _defaultStatusPrivacy.asStateFlow()

    private var cloudChatsJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                _registeredCount.value = repository.getRegisteredUsersCount()
                _isFirebaseSignedIn.value = repository.isUserSignedIn()

                // 1. Observe local Room DB chats continuously
                launch {
                    repository.getLocalChatsFlow().collect { localChats ->
                        if (localChats.isNotEmpty()) {
                            _chats.update { currentList ->
                                val localMap = localChats.associateBy { it.id }
                                val merged = currentList.map { localMap[it.id] ?: it } +
                                        localChats.filterNot { lc -> currentList.any { c -> c.id == lc.id } }
                                merged
                            }
                        }
                    }
                }

                // 2. If user is signed in, load user and start cloud listener
                if (repository.isUserSignedIn()) {
                    val user = repository.currentUser
                    _currentUser.value = user
                    if (user != null) {
                        if (_chats.value.isEmpty()) {
                            _chats.value = listOf(SampleData.createSavedMessagesChat(user))
                        }
                        observeCloudUserChats(user)
                    }
                }
                startGlobalNotificationListener()
            } catch (e: Exception) {
                Log.w("PaulViewModel", "Safe init exception: ${e.message}")
            }
        }
    }

    private fun observeCloudUserChats(user: User) {
        cloudChatsJob?.cancel()
        cloudChatsJob = viewModelScope.launch {
            repository.observeUserChats(user.userId, user.username, viewModelScope).collect { cloudChats ->
                if (cloudChats.isNotEmpty()) {
                    _chats.update { currentList ->
                        val cloudMap = cloudChats.associateBy { it.id }
                        val merged = currentList.map { cloudMap[it.id] ?: it } +
                                cloudChats.filterNot { cc -> currentList.any { c -> c.id == cc.id } }
                        merged
                    }
                }
            }
        }
    }

    suspend fun getRegisteredUsersCount(): Int {
        return try {
            val count = repository.getRegisteredUsersCount()
            _registeredCount.value = count
            count
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Account Registration:
     * The first registered user is automatically ADMIN!
     */
    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        pass: String,
        phone: String,
        bio: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.registerUser(fullName, username, email, pass, phone, bio)
            res.onSuccess { user ->
                _currentUser.value = user
                _isFirebaseSignedIn.value = true
                _chats.value = listOf(SampleData.createSavedMessagesChat(user))
                _registeredCount.value = repository.getRegisteredUsersCount()
                observeCloudUserChats(user)
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al registrar cuenta")
            }
        }
    }

    /**
     * Account Login with Username/Email and Password
     */
    fun loginUser(
        identifier: String,
        pass: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.loginUser(identifier, pass)
            res.onSuccess { user ->
                _currentUser.value = user
                _isFirebaseSignedIn.value = true
                _chats.value = listOf(SampleData.createSavedMessagesChat(user))
                observeCloudUserChats(user)
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onComplete: () -> Unit, onError: (String) -> Unit) {
        repository.onGoogleSignInClicked(
            activity = activity,
            onAuthSuccess = { user ->
                _isFirebaseSignedIn.value = true
                _currentUser.value = user
                _chats.value = listOf(SampleData.createSavedMessagesChat(user))
                observeCloudUserChats(user)
                onComplete()
            },
            onAuthError = onError,
            scope = viewModelScope
        )
    }

    fun signOut(onComplete: () -> Unit) {
        messagesListenerJob?.cancel()
        cloudChatsJob?.cancel()
        repository.signOut(viewModelScope) {
            _isFirebaseSignedIn.value = false
            _currentUser.value = null
            _chats.value = emptyList()
            _messages.value = emptyMap()
            _selectedChat.value = null
            onComplete()
        }
    }

    // Admin Panel Methods
    fun updateUserRole(userId: String, newRole: String, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.updateUserRole(userId, newRole)
            res.onSuccess {
                if (_currentUser.value?.userId == userId) {
                    _currentUser.value = _currentUser.value?.copy(role = newRole)
                }
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al cambiar rol")
            }
        }
    }

    fun updateUserProfile(newDisplayName: String, newBio: String, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.updateUserProfile(newDisplayName, newBio)
            res.onSuccess {
                _currentUser.value = _currentUser.value?.copy(displayName = newDisplayName, bio = newBio)
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al actualizar perfil")
            }
        }
    }

    fun toggleUserBan(userId: String, isBanned: Boolean, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.toggleUserBan(userId, isBanned)
            res.onSuccess {
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al suspender/desbloquear usuario")
            }
        }
    }

    fun deleteUserAccount(userId: String, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.deleteUserAccount(userId)
            res.onSuccess {
                onComplete()
            }.onFailure {
                onError(it.localizedMessage ?: "Error al eliminar usuario")
            }
        }
    }

    // Admin Publish In-App Update
    fun publishAppUpdate(
        versionName: String,
        versionCode: Int,
        apkUrl: String,
        changelog: String,
        isMandatory: Boolean,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.publishAppUpdate(versionName, versionCode, apkUrl, changelog, isMandatory)
            res.onSuccess { onComplete() }.onFailure { onError(it.localizedMessage ?: "Error al publicar actualización") }
        }
    }

    fun getDeviceContacts(): List<DeviceContact> {
        return repository.contactsHelper.readDeviceContacts()
    }

    suspend fun searchUserInFirebase(query: String): User? {
        return repository.searchUserInFirebase(query)
    }

    fun setSearchContactQuery(q: String) {
        _searchContactQuery.value = q
        if (q.isBlank()) {
            _searchContactResults.value = emptyList()
        }
    }

    fun searchContactsInFirestore(query: String) {
        _searchContactQuery.value = query
        if (query.trim().isEmpty()) {
            _searchContactResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearchingContacts.value = true
            try {
                val results = repository.searchUsersInFirestore(query)
                _searchContactResults.value = results
            } catch (e: Exception) {
                Log.w("PaulViewModel", "Search error: ${e.message}")
            } finally {
                _isSearchingContacts.value = false
            }
        }
    }

    fun saveContact(user: User, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.addSavedContact(user)
            res.onSuccess { onComplete() }.onFailure { onError(it.localizedMessage ?: "Error al guardar contacto") }
        }
    }

    fun deleteSavedContact(contactUserId: String) {
        viewModelScope.launch {
            repository.removeSavedContact(contactUserId)
        }
    }

    fun openChatWithUser(user: User) {
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val targetUid = user.userId.ifEmpty { user.username }
        val deterministicId = repository.getDirectChatId(currentUid.ifEmpty { currentUsername }, targetUid)

        val existingChat = _chats.value.find { it.id == deterministicId || it.username.equals(user.username, ignoreCase = true) }
        if (existingChat != null) {
            selectChat(existingChat)
        } else {
            addContactAndOpenChat(
                title = user.name,
                username = user.username,
                phone = user.phoneNumber,
                isFirebase = true
            )
        }
    }

    // ==========================================
    // ADVANCED GROUPS & RBAC MANAGEMENT
    // ==========================================

    private val _activeGroupConfigs = MutableStateFlow<Map<String, GroupConfig>>(emptyMap())
    val activeGroupConfigs: StateFlow<Map<String, GroupConfig>> = _activeGroupConfigs.asStateFlow()

    fun createGroupChat(
        title: String,
        description: String,
        isPublic: Boolean,
        alias: String,
        initialMembers: List<User>,
        onComplete: (Chat) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.createGroupChat(title, description, isPublic, alias, initialMembers)
            res.onSuccess { newGroupChat ->
                _chats.update { current -> listOf(newGroupChat) + current.filterNot { it.id == newGroupChat.id } }
                newGroupChat.groupConfig?.let { cfg ->
                    _activeGroupConfigs.update { map -> map + (newGroupChat.id to cfg) }
                }
                selectChat(newGroupChat)
                onComplete(newGroupChat)
            }.onFailure { err ->
                onError(err.localizedMessage ?: "Error al crear grupo")
            }
        }
    }

    fun observeGroupConfig(groupId: String) {
        if (groupId.isBlank()) return
        viewModelScope.launch {
            repository.observeGroupConfig(groupId).collect { config ->
                if (config != null) {
                    _activeGroupConfigs.update { map -> map + (groupId to config) }
                    _chats.update { list ->
                        list.map { c ->
                            if (c.id == groupId) c.copy(
                                groupConfig = config,
                                subtitle = "${config.members.size} miembros • ${if (config.accessConfig.isPublic) "Público" else "Privado"}",
                                memberCount = config.members.size
                            ) else c
                        }
                    }
                }
            }
        }
    }

    fun updateGroupConfig(groupId: String, newConfig: GroupConfig) {
        _activeGroupConfigs.update { map -> map + (groupId to newConfig) }
        _chats.update { list ->
            list.map { c ->
                if (c.id == groupId) c.copy(
                    groupConfig = newConfig,
                    subtitle = "${newConfig.members.size} miembros • ${if (newConfig.accessConfig.isPublic) "Público" else "Privado"}",
                    memberCount = newConfig.members.size
                ) else c
            }
        }
        viewModelScope.launch {
            repository.saveGroupConfig(newConfig)
        }
    }

    fun banUserFromGroup(groupId: String, targetUserId: String, massDeleteMessages: Boolean) {
        viewModelScope.launch {
            repository.banUserFromGroup(groupId, targetUserId, massDeleteMessages)
        }
    }

    fun unbanUserFromGroup(groupId: String, targetUserId: String) {
        val currentConfig = _activeGroupConfigs.value[groupId] ?: return
        val updatedBanned = currentConfig.bannedUserIds.filterNot { it == targetUserId }
        val newConfig = currentConfig.copy(bannedUserIds = updatedBanned)
        updateGroupConfig(groupId, newConfig)
    }

    fun pinMessageInChat(chatId: String, messageId: String, pin: Boolean) {
        _messages.update { map ->
            val msgs = map[chatId] ?: emptyList()
            map + (chatId to msgs.map { m ->
                if (m.id == messageId) m.copy(isPinned = pin) else m
            })
        }
        viewModelScope.launch {
            repository.pinMessageInChat(chatId, messageId, pin)
        }
    }

    // ==========================================
    // ESTADOS Y REELS (WhatsApp style)
    // ==========================================

    fun setDefaultStatusPrivacy(privacy: StatusPrivacy) {
        _defaultStatusPrivacy.value = privacy
    }

    fun createStatus(
        type: StatusType,
        contentText: String = "",
        mediaUrl: String = "",
        backgroundColor: Long = 0xFF0D5C46,
        fontStyle: String = "NORMAL",
        caption: String = "",
        privacy: StatusPrivacy = _defaultStatusPrivacy.value,
        excludedIds: List<String> = emptyList(),
        includedIds: List<String> = emptyList(),
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _currentUser.value ?: run {
            onError("Debes iniciar sesión para publicar un estado")
            return
        }

        viewModelScope.launch {
            val status = StatusItem(
                id = "status_" + UUID.randomUUID().toString().take(10),
                userId = current.userId,
                displayName = current.name,
                username = current.username,
                userAvatarStart = current.avatarGradientStart,
                userAvatarEnd = current.avatarGradientEnd,
                type = type.name,
                contentText = contentText.trim(),
                mediaUrl = mediaUrl.trim(),
                backgroundColor = backgroundColor,
                fontStyle = fontStyle,
                caption = caption.trim(),
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
                privacy = privacy.name,
                excludedUserIds = excludedIds,
                includedUserIds = includedIds,
                viewers = emptyList(),
                reactions = emptyMap()
            )

            val res = repository.createStatus(status)
            res.onSuccess { onComplete() }.onFailure { onError(it.localizedMessage ?: "Error al publicar estado") }
        }
    }

    fun recordStatusView(statusId: String) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            val viewer = StatusViewer(
                userId = current.userId,
                displayName = current.name,
                username = current.username,
                avatarGradientStart = current.avatarGradientStart,
                avatarGradientEnd = current.avatarGradientEnd,
                viewedAt = System.currentTimeMillis()
            )
            repository.recordStatusView(statusId, viewer)
        }
    }

    fun reactToStatus(statusId: String, emoji: String) {
        viewModelScope.launch {
            repository.reactToStatus(statusId, emoji)
        }
    }

    fun deleteStatus(statusId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteStatus(statusId)
            onComplete()
        }
    }

    fun replyToStatus(status: StatusItem, replyText: String) {
        val summary = if (status.isText) status.contentText.take(40) else status.caption.ifBlank { "Estado multimedia" }
        val quotedStatus = Message(
            id = status.id,
            text = "📷 Estado: \"$summary\"",
            photoUrl = if (status.isPhoto || status.isReel) status.mediaUrl else null,
            senderName = status.displayName,
            senderUsername = status.username
        )

        val fullText = replyText.ifBlank { "💬 Respondió a tu estado" }

        val targetChat = _chats.value.find { it.username.equals(status.username, ignoreCase = true) || it.id == status.userId }
        val chatId = targetChat?.id ?: run {
            val newId = "contact_" + UUID.randomUUID().toString().take(6)
            val newChat = Chat(
                id = newId,
                title = status.displayName,
                subtitle = "en línea • Paul Cloud",
                username = status.username,
                type = ChatType.DIRECT,
                lastMessage = fullText,
                lastMessageTime = getCurrentTimeString(),
                unreadCount = 0,
                isOnline = true,
                isEncrypted = false,
                isCloudSynced = true,
                avatarGradientStart = status.userAvatarStart,
                avatarGradientEnd = status.userAvatarEnd
            )
            _chats.update { listOf(newChat) + it }
            newId
        }

        sendMessage(chatId, fullText, isViewOnce = false, replyingMessage = quotedStatus)
    }

    fun openStatusGroup(group: UserStatusGroup, startIndex: Int = 0) {
        _selectedStatusGroup.value = group
        _selectedStatusIndex.value = startIndex.coerceIn(0, (group.statuses.size - 1).coerceAtLeast(0))
    }

    fun closeStatusViewer() {
        _selectedStatusGroup.value = null
        _selectedStatusIndex.value = 0
    }

    fun nextStatus(): Boolean {
        val group = _selectedStatusGroup.value ?: return false
        val nextIdx = _selectedStatusIndex.value + 1
        if (nextIdx < group.statuses.size) {
            _selectedStatusIndex.value = nextIdx
            return true
        } else {
            // Move to next group if available
            val allGroups = userStatusGroups.value
            val currentGroupIdx = allGroups.indexOfFirst { it.userId == group.userId }
            if (currentGroupIdx != -1 && currentGroupIdx + 1 < allGroups.size) {
                val nextGroup = allGroups[currentGroupIdx + 1]
                _selectedStatusGroup.value = nextGroup
                _selectedStatusIndex.value = 0
                return true
            } else {
                closeStatusViewer()
                return false
            }
        }
    }

    fun previousStatus(): Boolean {
        val group = _selectedStatusGroup.value ?: return false
        val prevIdx = _selectedStatusIndex.value - 1
        if (prevIdx >= 0) {
            _selectedStatusIndex.value = prevIdx
            return true
        } else {
            // Move to previous group
            val allGroups = userStatusGroups.value
            val currentGroupIdx = allGroups.indexOfFirst { it.userId == group.userId }
            if (currentGroupIdx > 0) {
                val prevGroup = allGroups[currentGroupIdx - 1]
                _selectedStatusGroup.value = prevGroup
                _selectedStatusIndex.value = (prevGroup.statuses.size - 1).coerceAtLeast(0)
                return true
            } else {
                return false
            }
        }
    }

    fun addContactAndOpenChat(title: String, username: String, phone: String, isFirebase: Boolean) {
        val currentUid = _currentUser.value?.userId ?: "local_user"
        val currentUsername = _currentUser.value?.username ?: ""
        val partnerId = if (isFirebase) username.ifEmpty { phone } else "secret_user"
        val fingerprint = if (!isFirebase) "SHA-256 #${UUID.randomUUID().toString().take(8).uppercase()}" else null

        val newId = if (isFirebase) {
            repository.getDirectChatId(currentUid.ifEmpty { currentUsername }, partnerId)
        } else {
            "secret_" + UUID.randomUUID().toString().take(6)
        }

        val newChat = Chat(
            id = newId,
            title = title,
            subtitle = if (isFirebase) "en línea • Paul Cloud" else "🔒 Chat Secreto E2EE",
            username = if (username.startsWith("@")) username else if (username.isNotBlank()) "@$username" else "",
            type = if (isFirebase) ChatType.DIRECT else ChatType.SECRET,
            lastMessage = if (isFirebase) "Chat iniciado vía Paul Cloud" else "🔒 Chat Secreto iniciado con cifrado E2EE",
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
                "✨ Has iniciado una conversación con $title conectada a Paul Cloud Database."
            } else {
                "🔒 Chat Secreto activado con cifrado de extremo a extremo.\n• Las claves nunca salen de este dispositivo\n• Sin almacenamiento en la nube\n• Huella criptográfica: ${fingerprint ?: "#D4A8"}"
            },
            timestamp = getCurrentTimeString(),
            isOutgoing = false,
            senderId = partnerId,
            senderUsername = username,
            senderName = title,
            isEncrypted = !isFirebase,
            status = MessageStatus.READ
        )

        _chats.update { listOf(newChat) + it.filterNot { c -> c.id == newId } }
        _messages.update { it + (newId to listOf(initialWelcomeMessage)) }

        val contactUser = User(
            userId = partnerId,
            displayName = title,
            username = if (username.startsWith("@")) username else if (username.isNotBlank()) "@$username" else "",
            phoneNumber = phone,
            bio = if (isFirebase) "Contacto en Paul Cloud" else "Contacto Local Cifrado E2EE",
            isOnline = isFirebase
        )

        viewModelScope.launch {
            repository.addSavedContact(contactUser)
            if (isFirebase) {
                repository.syncChatToCloudAndLocal(newChat, listOf(currentUid, currentUsername, username, phone))
                repository.syncMessage(newId, initialWelcomeMessage, viewModelScope)
            }
        }

        selectChat(newChat)
    }

    fun selectChat(chat: Chat?) {
        _selectedChat.value = chat
        _replyingMessage.value = null
        messagesListenerJob?.cancel()

        if (chat != null) {
            val currentUid = _currentUser.value?.userId ?: ""
            val currentUsername = _currentUser.value?.username ?: ""

            if (chat.unreadCount > 0) {
                _chats.update { list ->
                    list.map { if (it.id == chat.id) it.copy(unreadCount = 0) else it }
                }
            }

            // 1. Observe local messages from Room DB immediately
            viewModelScope.launch {
                repository.getLocalMessagesFlow(chat.id).collect { localMsgs ->
                    if (localMsgs.isNotEmpty()) {
                        val mapped = localMsgs.map { m ->
                            m.copy(isOutgoing = m.isOutgoingFor(currentUid, currentUsername))
                        }
                        _messages.update { currentMap ->
                            val existing = currentMap[chat.id] ?: emptyList()
                            val localIds = mapped.map { it.id }.toSet()
                            val merged = existing.filterNot { it.id in localIds } + mapped
                            currentMap + (chat.id to merged.sortedBy { it.timestamp })
                        }
                    }
                }
            }

            // 2. Real-time Messages Observation for this chat from Cloud Firestore!
            messagesListenerJob = viewModelScope.launch {
                repository.observeRealtimeMessages(chat.id, viewModelScope).collect { cloudMsgs ->
                    if (cloudMsgs.isNotEmpty()) {
                        val mapped = cloudMsgs.map { m ->
                            m.copy(isOutgoing = m.isOutgoingFor(currentUid, currentUsername))
                        }
                        _messages.update { currentMap ->
                            val existing = currentMap[chat.id] ?: emptyList()
                            val cloudMsgIds = mapped.map { it.id }.toSet()
                            val merged = existing.filterNot { it.id in cloudMsgIds } + mapped
                            currentMap + (chat.id to merged.sortedBy { it.timestamp })
                        }
                    }
                }
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

    fun sendMessage(chatId: String, text: String, isViewOnce: Boolean = false, replyingMessage: Message? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val targetChat = _chats.value.find { it.id == chatId }
        if (targetChat?.isBlocked == true) return

        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"

        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()
        val replying = replyingMessage ?: _replyingMessage.value

        val newMessage = Message(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = trimmed,
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
            isEncrypted = isEncrypted,
            replyTo = replying,
            isViewOnce = isViewOnce,
            hasBeenViewed = false,
            status = MessageStatus.SENT
        )

        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (chatMessages + newMessage))
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(
                        lastMessage = if (isViewOnce) "🔒 Mensaje de lectura única" else trimmed,
                        lastMessageTime = time
                    )
                } else it
            }
        }

        _replyingMessage.value = null

        val updatedChat = targetChat?.copy(
            lastMessage = if (isViewOnce) "🔒 Mensaje de lectura única" else trimmed,
            lastMessageTime = time
        ) ?: Chat(
            id = chatId,
            title = targetChat?.title ?: "Chat",
            lastMessage = trimmed,
            lastMessageTime = time
        )

        // Sync Chat and Message to local Room DB and Cloud Firestore!
        viewModelScope.launch {
            repository.syncChatToCloudAndLocal(updatedChat, listOf(updatedChat.username, targetChat?.contactPhone ?: "", currentUid, currentUsername))
            repository.syncMessage(chatId, newMessage, viewModelScope)
        }

        viewModelScope.launch {
            delay(500)
            _messages.update { currentMap ->
                val chatMessages = currentMap[chatId] ?: emptyList()
                currentMap + (chatId to chatMessages.map {
                    if (it.id == newMessage.id) it.copy(status = MessageStatus.READ) else it
                })
            }
        }
    }

    fun markMessageAsViewed(chatId: String, messageId: String) {
        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to chatMessages.map { msg ->
                if (msg.id == messageId && msg.isViewOnce) {
                    msg.copy(hasBeenViewed = true)
                } else msg
            })
        }
    }

    fun startVoiceRecording() {
        if (_isRecordingVoice.value) return
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

    fun sendVoiceMessage(chatId: String, durationSeconds: Int) {
        val targetChat = _chats.value.find { it.id == chatId }
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()
        val durationFormatted = "%d:%02d".format(durationSeconds / 60, durationSeconds % 60)
        val newMessage = Message(
            id = "voice_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = "🎤 Mensaje de voz ($durationFormatted)",
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
            type = MessageType.VOICE,
            voiceDurationSeconds = durationSeconds,
            isEncrypted = isEncrypted,
            status = MessageStatus.SENT
        )

        _messages.update { currentMap ->
            val chatMessages = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (chatMessages + newMessage))
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(
                        lastMessage = "🎤 Mensaje de voz ($durationFormatted)",
                        lastMessageTime = time
                    )
                } else it
            }
        }

        repository.syncMessage(chatId, newMessage, viewModelScope)
    }

    fun sendGifMessage(chatId: String, gifTitle: String, gifUrl: String) {
        val targetChat = _chats.value.find { it.id == chatId }
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()
        val newMessage = Message(
            id = "gif_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = "GIF: $gifTitle",
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
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
                        lastMessage = "GIF",
                        lastMessageTime = time
                    )
                } else it
            }
        }

        repository.syncMessage(chatId, newMessage, viewModelScope)
    }

    fun sendLocationMessage(chatId: String, location: LocationData) {
        val targetChat = _chats.value.find { it.id == chatId }
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()
        val isLive = location.isLive
        val msgId = "loc_" + UUID.randomUUID().toString().take(8)

        val newMessage = Message(
            id = msgId,
            chatId = chatId,
            text = if (isLive) "📍 Ubicación en tiempo real" else "📍 ${location.title}",
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
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

        repository.syncMessage(chatId, newMessage, viewModelScope)

        if (isLive) {
            repository.locationHelper.startLiveLocationUpdates(location.liveDurationMinutes) { updatedLoc ->
                _messages.update { map ->
                    val msgs = map[chatId] ?: emptyList()
                    map + (chatId to msgs.map { m ->
                        if (m.id == msgId) m.copy(locationData = updatedLoc) else m
                    })
                }
            }
        }
    }

    fun sendPhotoMessage(chatId: String, photoUrl: String, caption: String = "") {
        val targetChat = _chats.value.find { it.id == chatId }
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()

        val newMessage = Message(
            id = "photo_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = caption.ifBlank { "📷 Foto" },
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
            type = MessageType.PHOTO,
            photoUrl = photoUrl,
            isEncrypted = isEncrypted,
            status = MessageStatus.READ
        )

        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to (list + newMessage))
        }

        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(lastMessage = "📷 Foto", lastMessageTime = time) else it }
        }

        repository.syncMessage(chatId, newMessage, viewModelScope)
    }

    fun sendVideoMessage(chatId: String, videoUrl: String, caption: String = "") {
        val targetChat = _chats.value.find { it.id == chatId }
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()

        val newMessage = Message(
            id = "video_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = caption.ifBlank { "🎬 Video / Reel" },
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
            type = MessageType.VIDEO,
            videoUrl = videoUrl,
            photoUrl = videoUrl,
            isEncrypted = isEncrypted,
            status = MessageStatus.READ
        )

        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to (list + newMessage))
        }

        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(lastMessage = "🎬 Video", lastMessageTime = time) else it }
        }

        repository.syncMessage(chatId, newMessage, viewModelScope)
    }

    fun sendFileMessage(chatId: String, fileUrl: String, fileName: String, fileSize: String) {
        val targetChat = _chats.value.find { it.id == chatId }
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()

        val newMessage = Message(
            id = "file_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = fileName,
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
            type = MessageType.FILE,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            isEncrypted = isEncrypted,
            status = MessageStatus.READ
        )

        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to (list + newMessage))
        }

        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(lastMessage = "📄 $fileName", lastMessageTime = time) else it }
        }

        repository.syncMessage(chatId, newMessage, viewModelScope)
    }

    fun sendContactMessage(chatId: String, contactName: String, contactPhone: String, contactUsername: String = "") {
        val targetChat = _chats.value.find { it.id == chatId }
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val isEncrypted = targetChat?.isEncrypted ?: (targetChat?.type == ChatType.SECRET)
        val time = getCurrentTimeString()

        val newMessage = Message(
            id = "contact_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = "👤 $contactName",
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
            type = MessageType.CONTACT,
            contactName = contactName,
            contactPhone = contactPhone,
            contactUsername = contactUsername,
            isEncrypted = isEncrypted,
            status = MessageStatus.READ
        )

        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to (list + newMessage))
        }

        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(lastMessage = "👤 Contacto: $contactName", lastMessageTime = time) else it }
        }

        repository.syncMessage(chatId, newMessage, viewModelScope)
    }

    fun sendPoll(chatId: String, question: String, optionTexts: List<String>) {
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
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
            id = "poll_msg_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            text = "📊 Encuesta: $question",
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
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

        repository.syncMessage(chatId, newMessage, viewModelScope)
    }

    fun createCloudBackup(onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.createCloudBackup()
            res.onSuccess { onComplete() }.onFailure { onError(it.localizedMessage ?: "Error al crear copia de seguridad") }
        }
    }

    fun restoreCloudBackup(onComplete: (Int) -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.restoreCloudBackup()
            res.onSuccess { count -> onComplete(count) }.onFailure { onError(it.localizedMessage ?: "Error al restaurar copia de seguridad") }
        }
    }

    fun votePoll(messageId: String, optionId: Int) {
        var updatedChatId: String? = null
        var updatedMessage: Message? = null

        _messages.update { currentMap ->
            currentMap.mapValues { (cid, messageList) ->
                messageList.map { message ->
                    if (message.id == messageId && message.pollData != null) {
                        updatedChatId = cid
                        val currentPoll = message.pollData
                        val previousVote = currentPoll.userVotedOptionId

                        val newOptions = currentPoll.options.map { opt ->
                            when {
                                opt.id == optionId && previousVote == optionId -> opt.copy(voteCount = (opt.voteCount - 1).coerceAtLeast(0))
                                opt.id == optionId -> opt.copy(voteCount = opt.voteCount + 1)
                                opt.id == previousVote -> opt.copy(voteCount = (opt.voteCount - 1).coerceAtLeast(0))
                                else -> opt
                            }
                        }

                        val newVotedOptionId = if (previousVote == optionId) null else optionId
                        val newTotalVotes = newOptions.sumOf { it.voteCount }

                        updatedMessage = message.copy(
                            pollData = currentPoll.copy(
                                options = newOptions,
                                totalVotes = newTotalVotes,
                                userVotedOptionId = newVotedOptionId
                            )
                        )
                        updatedMessage!!
                    } else {
                        message
                    }
                }
            }
        }

        updatedChatId?.let { cid ->
            updatedMessage?.let { msg ->
                repository.syncMessage(cid, msg, viewModelScope)
            }
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        var updatedChatId: String? = null
        var updatedMessage: Message? = null

        _messages.update { currentMap ->
            currentMap.mapValues { (cid, messageList) ->
                messageList.map { message ->
                    if (message.id == messageId) {
                        updatedChatId = cid
                        val currentReactions = message.reactions
                        val count = currentReactions[emoji] ?: 0
                        val isRemoving = count > 0 && message.userReaction == emoji
                        
                        val newReactions = if (isRemoving) {
                            currentReactions - emoji
                        } else {
                            currentReactions + (emoji to (count + 1))
                        }
                        
                        updatedMessage = message.copy(
                            reactions = newReactions,
                            userReaction = if (isRemoving) null else emoji
                        )
                        updatedMessage!!
                    } else {
                        message
                    }
                }
            }
        }

        updatedChatId?.let { cid ->
            updatedMessage?.let { msg ->
                repository.syncMessage(cid, msg, viewModelScope)
            }
        }
    }

    fun toggleMuteChat(chatId: String) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(isMuted = !it.isMuted)
                } else it
            }
        }
    }

    fun togglePinChat(chatId: String) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(isPinned = !it.isPinned)
                } else it
            }
        }
    }

    fun createNewChat(title: String, type: ChatType, description: String) {
        val newId = "chat_" + UUID.randomUUID().toString().take(6)
        val time = getCurrentTimeString()
        val newChat = Chat(
            id = newId,
            title = title,
            subtitle = if (type == ChatType.CHANNEL) "Canal público" else "Grupo • 1 miembro",
            username = "@" + title.lowercase().replace(" ", "_"),
            type = type,
            lastMessage = if (type == ChatType.CHANNEL) "Canal creado" else "Grupo creado",
            lastMessageTime = time,
            unreadCount = 0,
            isOnline = false,
            pinnedMessageText = if (description.isNotBlank()) description else null,
            avatarGradientStart = 0xFF7B1FA2L,
            avatarGradientEnd = 0xFF512DA8L
        )

        _chats.update { listOf(newChat) + it }
        _messages.update { it + (newId to emptyList()) }
        selectChat(newChat)
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
                    it.copy(isBlocked = !it.isBlocked)
                } else it
            }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update { it?.copy(isBlocked = !(it.isBlocked)) }
        }
    }

    fun unblockChat(chatId: String) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(isBlocked = false)
                } else it
            }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update { it?.copy(isBlocked = false) }
        }
    }

    fun pinMessageInChat(chatId: String, messageText: String) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(pinnedMessageText = messageText) else it
            }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update { it?.copy(pinnedMessageText = messageText) }
        }
    }

    fun unpinMessageInChat(chatId: String) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(pinnedMessageText = null) else it
            }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update { it?.copy(pinnedMessageText = null) }
        }
    }

    fun deleteSingleMessage(chatId: String, messageId: String) {
        deleteMessageForMe(chatId, messageId)
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isBlank()) return
        val editedTime = getCurrentTimeString()
        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to list.map {
                if (it.id == messageId) {
                    val updated = it.copy(text = trimmed, isEdited = true, editedTimestamp = editedTime)
                    viewModelScope.launch { repository.syncMessage(chatId, updated, viewModelScope) }
                    updated
                } else it
            })
        }
    }

    fun deleteMessageForMe(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to list.filterNot { it.id == messageId })
        }
        viewModelScope.launch { repository.deleteLocalMessage(messageId) }
    }

    fun deleteMessageForEveryone(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to list.map {
                if (it.id == messageId) {
                    val revoked = it.copy(text = "🚫 Este mensaje fue eliminado", isDeletedForEveryone = true)
                    viewModelScope.launch { repository.syncMessage(chatId, revoked, viewModelScope) }
                    revoked
                } else it
            })
        }
    }

    fun toggleStarMessage(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId] ?: emptyList()
            map + (chatId to list.map {
                if (it.id == messageId) {
                    val updated = it.copy(isStarred = !it.isStarred)
                    viewModelScope.launch { repository.syncMessage(chatId, updated, viewModelScope) }
                    updated
                } else it
            })
        }
    }

    fun forwardMessage(sourceMessage: Message, targetChatId: String, keepAuthor: Boolean) {
        val currentUid = _currentUser.value?.userId ?: ""
        val currentUsername = _currentUser.value?.username ?: ""
        val currentName = _currentUser.value?.displayName ?: "Usuario"
        val time = getCurrentTimeString()

        val fwdMsg = sourceMessage.copy(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            chatId = targetChatId,
            timestamp = time,
            isOutgoing = true,
            senderId = currentUid,
            senderUsername = currentUsername,
            senderName = currentName,
            forwardedFrom = if (keepAuthor) (sourceMessage.senderName ?: sourceMessage.senderUsername.ifEmpty { "Contacto" }) else null,
            status = MessageStatus.SENT
        )

        _messages.update { currentMap ->
            val chatMsgs = currentMap[targetChatId] ?: emptyList()
            currentMap + (targetChatId to (chatMsgs + fwdMsg))
        }

        viewModelScope.launch {
            repository.syncMessage(targetChatId, fwdMsg, viewModelScope)
        }
    }

    fun setDisappearingTimer(chatId: String, seconds: Int) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(disappearingTimerSeconds = seconds) else it }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update { it?.copy(disappearingTimerSeconds = seconds) }
        }
    }

    fun toggleChatLock(chatId: String, pin: String) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) {
                    val newLock = !it.isLocked
                    it.copy(isLocked = newLock, lockPin = if (newLock) pin else "")
                } else it
            }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update {
                val newLock = !(it?.isLocked ?: false)
                it?.copy(isLocked = newLock, lockPin = if (newLock) pin else "")
            }
        }
    }

    fun setChatMuteDuration(chatId: String, untilTimestamp: Long) {
        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(isMuted = untilTimestamp != 0L, mutedUntilTimestamp = untilTimestamp) else it
            }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update { it?.copy(isMuted = untilTimestamp != 0L, mutedUntilTimestamp = untilTimestamp) }
        }
    }

    fun toggleArchiveChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isArchived = !it.isArchived) else it }
        }
        if (_selectedChat.value?.id == chatId) {
            _selectedChat.update { it?.copy(isArchived = !(it.isArchived)) }
        }
    }

    fun toggleMarkUnread(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isMarkedUnread = !it.isMarkedUnread) else it }
        }
    }

    fun clearChatHistory(chatId: String) {
        _messages.update { currentMap ->
            currentMap + (chatId to emptyList())
        }
        viewModelScope.launch {
            repository.clearMessagesForChat(chatId)
        }
    }

    fun reportUser(chatId: String, reason: String) {
        viewModelScope.launch {
            repository.saveAuditLog(
                AuditLogEntry(
                    id = "report_" + UUID.randomUUID().toString().take(6),
                    adminUserId = _currentUser.value?.userId ?: "local_user",
                    adminName = _currentUser.value?.displayName ?: "Usuario",
                    actionType = "REPORT_USER",
                    description = "Reporte sobre $chatId: $reason",
                    targetUserId = chatId
                )
            )
        }
    }

    fun setPrivacyPhone(value: String) { _privacyPhone.value = value }
    fun setPrivacyLastSeen(value: String) { _privacyLastSeen.value = value }
    fun setPrivacyProfilePhoto(value: String) { _privacyProfilePhoto.value = value }
    fun setPrivacyForwardProtect(enabled: Boolean) { _privacyForwardProtect.value = enabled }
    fun setPrivacyPasscodeLock(enabled: Boolean) { _privacyPasscodeLock.value = enabled }
    fun setAccountSelfDestructPeriod(period: String) { _accountSelfDestructPeriod.value = period }

    // Notification preference updates
    fun setShowNotifications(enabled: Boolean) { _showNotifications.value = enabled }
    fun setNotificationSound(sound: String) { _notificationSound.value = sound }
    fun setNotificationVibration(enabled: Boolean) { _notificationVibration.value = enabled }

    /**
     * Start background observation for all active chats to show notifications
     */
    fun startGlobalNotificationListener() {
        viewModelScope.launch {
            _chats.collect { chatList ->
                chatList.forEach { chat ->
                    if (!globalListeners.containsKey(chat.id)) {
                        val job = viewModelScope.launch {
                            repository.observeRealtimeMessages(chat.id, viewModelScope).collect { messages ->
                                if (messages.isNotEmpty()) {
                                    messages.forEach { msg ->
                                        // If it's a new message, not from me, and not already seen
                                        if (!msg.isOutgoing && !seenMessageIds.contains(msg.id)) {
                                            // Only notify if notifications are enabled and it's not the currently active chat
                                            if (_showNotifications.value && _selectedChat.value?.id != chat.id) {
                                                notificationHelper.showMessageNotification(
                                                    senderName = chat.title,
                                                    messageText = msg.text,
                                                    chatId = chat.id,
                                                    soundName = _notificationSound.value
                                                )
                                            }
                                        }
                                        seenMessageIds.add(msg.id)
                                    }
                                }
                            }
                        }
                        globalListeners[chat.id] = job
                    }
                }
            }
        }
    }
}
