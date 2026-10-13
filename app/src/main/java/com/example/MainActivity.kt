package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ChatType
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.GroupManagementScreen
import com.example.ui.screens.SearchContactsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatusViewerScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PaulViewModel

enum class PaulScreen {
    AUTH,
    CHAT_LIST,
    CHAT_DETAIL,
    SETTINGS,
    ADMIN_PANEL,
    SEARCH_CONTACTS,
    GROUP_MANAGEMENT,
    STATUS_VIEWER,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: PaulViewModel = viewModel()
            val isDarkModePref by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val useDarkTheme = isDarkModePref ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PaulApp(
                        activity = this,
                        viewModel = viewModel,
                        isDarkTheme = useDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun PaulApp(
    activity: MainActivity,
    viewModel: PaulViewModel,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current

    // Request permissions on start
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isFirebaseSignedIn by viewModel.isFirebaseSignedIn.collectAsStateWithLifecycle()
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val selectedChat by viewModel.selectedChat.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val replyingMessage by viewModel.replyingMessage.collectAsStateWithLifecycle()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
    val userStatusGroups by viewModel.userStatusGroups.collectAsStateWithLifecycle()
    val selectedStatusGroup by viewModel.selectedStatusGroup.collectAsStateWithLifecycle()
    val selectedStatusIndex by viewModel.selectedStatusIndex.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val latestAppUpdate by viewModel.latestAppUpdate.collectAsStateWithLifecycle()
    val registeredCount by viewModel.registeredCount.collectAsStateWithLifecycle()
    val savedContacts by viewModel.savedContacts.collectAsStateWithLifecycle()
    val searchContactQuery by viewModel.searchContactQuery.collectAsStateWithLifecycle()
    val searchContactResults by viewModel.searchContactResults.collectAsStateWithLifecycle()
    val isSearchingContacts by viewModel.isSearchingContacts.collectAsStateWithLifecycle()
    val activeGroupConfigs by viewModel.activeGroupConfigs.collectAsStateWithLifecycle()

    val privacyPhone by viewModel.privacyPhone.collectAsStateWithLifecycle()
    val privacyLastSeen by viewModel.privacyLastSeen.collectAsStateWithLifecycle()
    val privacyForwardProtect by viewModel.privacyForwardProtect.collectAsStateWithLifecycle()
    val privacyPasscodeLock by viewModel.privacyPasscodeLock.collectAsStateWithLifecycle()
    val showNotifications by viewModel.showNotifications.collectAsStateWithLifecycle()
    val notificationSound by viewModel.notificationSound.collectAsStateWithLifecycle()
    val notificationVibration by viewModel.notificationVibration.collectAsStateWithLifecycle()

    var currentScreen by remember {
        mutableStateOf(if (currentUser != null) PaulScreen.CHAT_LIST else PaulScreen.AUTH)
    }

    // Keep screen in sync with login status
    LaunchedEffect(currentUser) {
        if (currentUser == null && currentScreen != PaulScreen.AUTH) {
            currentScreen = PaulScreen.AUTH
        } else if (currentUser != null && currentScreen == PaulScreen.AUTH) {
            currentScreen = PaulScreen.CHAT_LIST
        }
    }

    when (currentScreen) {
        PaulScreen.AUTH -> {
            AuthScreen(
                registeredUsersCount = registeredCount,
                onLogin = { identifier, pass, onError ->
                    viewModel.loginUser(
                        identifier = identifier,
                        pass = pass,
                        onComplete = {
                            currentScreen = PaulScreen.CHAT_LIST
                        },
                        onError = onError
                    )
                },
                onRegister = { name, username, email, pass, phone, bio, onError ->
                    viewModel.registerUser(
                        fullName = name,
                        username = username,
                        email = email,
                        pass = pass,
                        phone = phone,
                        bio = bio,
                        onComplete = {
                            currentScreen = PaulScreen.CHAT_LIST
                        },
                        onError = onError
                    )
                },
                onSignInWithGoogle = {
                    viewModel.signInWithGoogle(
                        activity = activity,
                        onComplete = {
                            currentScreen = PaulScreen.CHAT_LIST
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }

        PaulScreen.CHAT_LIST -> {
            ChatListScreen(
                currentUser = currentUser,
                chats = chats,
                selectedFolder = selectedFolder,
                searchQuery = searchQuery,
                isSearching = isSearching,
                isDarkMode = isDarkTheme,
                isFirebaseSignedIn = isFirebaseSignedIn,
                statusGroups = userStatusGroups,
                onSelectFolder = { viewModel.setFolder(it) },
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onToggleSearch = { viewModel.toggleSearch(it) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onSignInGoogle = {
                    viewModel.signInWithGoogle(
                        activity = activity,
                        onComplete = {},
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                onSearchUserInFirebase = { query -> viewModel.searchUserInFirebase(query) },
                onAddContactAndOpenChat = { title, username, phone, isFirebase ->
                    viewModel.addContactAndOpenChat(title, username, phone, isFirebase)
                    currentScreen = PaulScreen.CHAT_DETAIL
                },
                onChatClick = { chat ->
                    viewModel.selectChat(chat)
                    currentScreen = PaulScreen.CHAT_DETAIL
                },
                onCreateNewChat = { title, type, description ->
                    viewModel.createNewChat(title, type, description)
                },
                onOpenSavedMessages = {
                    val savedChat = chats.find { it.isSavedMessages }
                    if (savedChat != null) {
                        viewModel.selectChat(savedChat)
                        currentScreen = PaulScreen.CHAT_DETAIL
                    } else {
                        viewModel.createNewChat("Mensajes Guardados", ChatType.DIRECT, "Mis notas y archivos personales")
                        currentScreen = PaulScreen.CHAT_DETAIL
                    }
                },
                onOpenAdminPanel = { currentScreen = PaulScreen.ADMIN_PANEL },
                onOpenContacts = { currentScreen = PaulScreen.SEARCH_CONTACTS },
                onOpenStatusGroup = { group ->
                    viewModel.openStatusGroup(group)
                    currentScreen = PaulScreen.STATUS_VIEWER
                },
                onCreateTextStatus = { /* dialog inside ChatListScreen or component */ },
                onCreateMediaStatus = { /* media status picker */ },
                onDeleteChat = { viewModel.deleteChat(it) },
                onTogglePinChat = { viewModel.togglePinChat(it) },
                onToggleMuteChat = { viewModel.toggleMuteChat(it) },
                onOpenSettings = { currentScreen = PaulScreen.SETTINGS },
                onOpenProfile = { currentScreen = PaulScreen.PROFILE }
            )
        }

        PaulScreen.CHAT_DETAIL -> {
            val activeChat = selectedChat
            if (activeChat == null) {
                currentScreen = PaulScreen.CHAT_LIST
            } else {
                val chatMessages = messages[activeChat.id] ?: emptyList()
                val currentConfig = activeGroupConfigs[activeChat.id] ?: com.example.data.model.GroupConfig()

                ChatDetailScreen(
                    chat = activeChat,
                    messages = chatMessages,
                    replyingMessage = replyingMessage,
                    isRecordingVoice = isRecordingVoice,
                    recordingSeconds = recordingSeconds,
                    isDarkTheme = isDarkTheme,
                    onBack = {
                        viewModel.selectChat(null)
                        currentScreen = PaulScreen.CHAT_LIST
                    },
                    onSendMessage = { text, isViewOnce ->
                        viewModel.sendMessage(activeChat.id, text, isViewOnce)
                    },
                    onSendVoiceMessage = { duration ->
                        viewModel.sendVoiceMessage(activeChat.id, duration)
                    },
                    onSendGifMessage = { title, url ->
                        viewModel.sendGifMessage(activeChat.id, title, url)
                    },
                    onSendLocationMessage = { loc ->
                        viewModel.sendLocationMessage(activeChat.id, loc)
                    },
                    onSendPoll = { question, options ->
                        viewModel.sendPoll(activeChat.id, question, options)
                    },
                    onVotePoll = { messageId, optionId ->
                        viewModel.votePoll(messageId, optionId)
                    },
                    onReaction = { msg, emoji ->
                        viewModel.addReaction(msg.id, emoji)
                    },
                    onReplyMessage = { msg ->
                        viewModel.setReplyingMessage(msg)
                    },
                    onCancelReply = {
                        viewModel.setReplyingMessage(null)
                    },
                    onToggleMute = {
                        viewModel.toggleMuteChat(activeChat.id)
                    },
                    onTogglePin = {
                        viewModel.togglePinChat(activeChat.id)
                    },
                    onToggleBlock = {
                        viewModel.toggleBlockChat(activeChat.id)
                    },
                    onMarkMessageViewed = { msg ->
                        viewModel.markMessageAsViewed(activeChat.id, msg.id)
                    },
                    onClearChat = {
                        // clear chat
                    },
                    onStartVoiceRecording = {
                        viewModel.startVoiceRecording()
                    },
                    onCancelVoiceRecording = {
                        viewModel.cancelVoiceRecording()
                    },
                    onStopVoiceRecordingAndSend = {
                        viewModel.stopVoiceRecordingAndSend(activeChat.id)
                    },
                    onSendPhotoMessage = { url, caption ->
                        viewModel.sendPhotoMessage(activeChat.id, url, caption)
                    },
                    onSendVideoMessage = { url, caption ->
                        viewModel.sendVideoMessage(activeChat.id, url, caption)
                    },
                    onSendFileMessage = { url, fileName, fileSize ->
                        viewModel.sendFileMessage(activeChat.id, url, fileName, fileSize)
                    },
                    onSendContactMessage = { name, phone ->
                        viewModel.sendContactMessage(activeChat.id, name, phone)
                    },
                    onOpenGroupManagement = {
                        currentScreen = PaulScreen.GROUP_MANAGEMENT
                    },
                    onEditMessage = { msgId, newText ->
                        viewModel.editMessage(activeChat.id, msgId, newText)
                    },
                    onDeleteForMe = { msgId ->
                        viewModel.deleteMessageForMe(activeChat.id, msgId)
                    },
                    onDeleteForEveryone = { msgId ->
                        viewModel.deleteMessageForEveryone(activeChat.id, msgId)
                    },
                    onToggleStar = { msgId ->
                        viewModel.toggleStarMessage(activeChat.id, msgId)
                    },
                    onForwardMessage = { msg, targetChatId, keepAuthor ->
                        viewModel.forwardMessage(msg, targetChatId, keepAuthor)
                    },
                    onSetDisappearingTimer = { seconds ->
                        viewModel.setDisappearingTimer(activeChat.id, seconds)
                    },
                    onToggleChatLock = { pin ->
                        viewModel.toggleChatLock(activeChat.id, pin)
                    },
                    onReportUser = { reason ->
                        viewModel.reportUser(activeChat.id, reason)
                    }
                )
            }
        }

        PaulScreen.SETTINGS -> {
            val blockedList = chats.filter { it.isBlocked }

            SettingsScreen(
                currentUser = currentUser,
                isDarkMode = isDarkTheme,
                isFirebaseSignedIn = isFirebaseSignedIn,
                blockedChats = blockedList,
                privacyPhone = privacyPhone,
                privacyLastSeen = privacyLastSeen,
                privacyForwardProtect = privacyForwardProtect,
                privacyPasscodeLock = privacyPasscodeLock,
                showNotifications = showNotifications,
                notificationSound = notificationSound,
                notificationVibration = notificationVibration,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onSignInGoogle = {
                    viewModel.signInWithGoogle(
                        activity = activity,
                        onComplete = {},
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                onSignOutGoogle = {
                    viewModel.signOut {
                        currentScreen = PaulScreen.AUTH
                    }
                },
                onUnblockChat = { viewModel.unblockChat(it) },
                onUpdatePrivacyPhone = { viewModel.setPrivacyPhone(it) },
                onUpdatePrivacyLastSeen = { viewModel.setPrivacyLastSeen(it) },
                onToggleForwardProtect = { viewModel.setPrivacyForwardProtect(it) },
                onTogglePasscodeLock = { viewModel.setPrivacyPasscodeLock(it) },
                onToggleNotifications = { viewModel.setShowNotifications(it) },
                onUpdateNotificationSound = { viewModel.setNotificationSound(it) },
                onToggleNotificationVibration = { viewModel.setNotificationVibration(it) },
                onOpenAdminPanel = { currentScreen = PaulScreen.ADMIN_PANEL },
                onOpenProfile = { currentScreen = PaulScreen.PROFILE },
                onUpdateProfile = { name, bio ->
                    viewModel.updateUserProfile(name, bio)
                },
                onCreateCloudBackup = {
                    viewModel.createCloudBackup(
                        onComplete = { Toast.makeText(context, "Copia de seguridad creada en la nube", Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                onRestoreCloudBackup = {
                    viewModel.restoreCloudBackup(
                        onComplete = { count -> Toast.makeText(context, "Restaurados $count chats exitosamente", Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                onBack = { currentScreen = PaulScreen.CHAT_LIST }
            )
        }

        PaulScreen.PROFILE -> {
            UserProfileScreen(
                currentUser = currentUser,
                onUpdateProfile = { name, bio, photoUrl ->
                    viewModel.updateUserProfile(
                        newDisplayName = name,
                        newBio = bio,
                        newProfilePictureUrl = photoUrl,
                        onComplete = {
                            Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onBack = { currentScreen = PaulScreen.SETTINGS }
            )
        }

        PaulScreen.ADMIN_PANEL -> {
            val user = currentUser
            if (user == null) {
                currentScreen = PaulScreen.AUTH
            } else {
                AdminPanelScreen(
                    currentUser = user,
                    allUsers = allUsers,
                    latestAppUpdate = latestAppUpdate,
                    onUpdateRole = { userId, newRole ->
                        viewModel.updateUserRole(
                            userId = userId,
                            newRole = newRole,
                            onComplete = { Toast.makeText(context, "Rol actualizado a $newRole", Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    },
                    onBack = { currentScreen = PaulScreen.SETTINGS }
                )
            }
        }

        PaulScreen.SEARCH_CONTACTS -> {
            SearchContactsScreen(
                searchQuery = searchContactQuery,
                searchResults = searchContactResults,
                savedContacts = savedContacts,
                isSearching = isSearchingContacts,
                onQueryChange = { query ->
                    // viewModel search contacts
                },
                onSelectUser = { user ->
                    viewModel.addContactAndOpenChat(
                        title = user.displayName.ifBlank { user.name },
                        username = user.username,
                        phone = user.phoneNumber,
                        isFirebase = true
                    )
                    currentScreen = PaulScreen.CHAT_DETAIL
                },
                onSelectSavedContact = { contact ->
                    viewModel.addContactAndOpenChat(
                        title = contact.name,
                        username = contact.username,
                        phone = contact.phone,
                        isFirebase = true
                    )
                    currentScreen = PaulScreen.CHAT_DETAIL
                },
                onBack = { currentScreen = PaulScreen.CHAT_LIST }
            )
        }

        PaulScreen.GROUP_MANAGEMENT -> {
            val activeChat = selectedChat
            if (activeChat == null) {
                currentScreen = PaulScreen.CHAT_LIST
            } else {
                val currentConfig = activeGroupConfigs[activeChat.id] ?: com.example.data.model.GroupConfig()
                GroupManagementScreen(
                    chat = activeChat,
                    currentUserId = currentUser?.userId ?: "",
                    groupConfig = currentConfig,
                    onUpdateGroupConfig = { cfg ->
                        viewModel.updateGroupConfig(activeChat.id, cfg)
                    },
                    onBanUser = { targetId, massDelete ->
                        viewModel.banUserFromGroup(activeChat.id, targetId, massDelete)
                    },
                    onUnbanUser = { targetId ->
                        viewModel.unbanUserFromGroup(activeChat.id, targetId)
                    },
                    onBack = { currentScreen = PaulScreen.CHAT_DETAIL }
                )
            }
        }

        PaulScreen.STATUS_VIEWER -> {
            val statusGroup = selectedStatusGroup
            if (statusGroup == null) {
                currentScreen = PaulScreen.CHAT_LIST
            } else {
                StatusViewerScreen(
                    statusGroup = statusGroup,
                    initialIndex = selectedStatusIndex,
                    onClose = { currentScreen = PaulScreen.CHAT_LIST }
                )
            }
        }
    }
}
