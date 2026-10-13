package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Chat
import com.example.data.model.StatusType
import com.example.ui.components.CreateStatusDialog
import com.example.data.model.GroupConfig
import com.example.ui.screens.GroupManagementScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.SearchContactsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatusViewerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PaulViewModel

enum class PaulScreen {
    AUTH,
    CHAT_LIST,
    CHAT_DETAIL,
    SETTINGS,
    ADMIN_PANEL,
    SEARCH_CONTACTS,
    GROUP_MANAGEMENT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PaulViewModel = viewModel()
            val isDarkModeOverride by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val effectiveDark = isDarkModeOverride ?: systemDark

            MyApplicationTheme(darkTheme = effectiveDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PaulApp(viewModel = viewModel, isDarkMode = effectiveDark)
                }
            }
        }
    }
}

@Composable
fun PaulApp(
    viewModel: PaulViewModel,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val activity = context as? Activity

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentUser) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START,
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    if (currentUser != null) {
                        viewModel.setUserOnlineStatus(true)
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_STOP,
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    if (currentUser != null) {
                        viewModel.setUserOnlineStatus(false)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (currentUser != null) {
                viewModel.setUserOnlineStatus(false)
            }
        }
    }
    val isFirebaseSignedIn by viewModel.isFirebaseSignedIn.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val registeredCount by viewModel.registeredCount.collectAsStateWithLifecycle()
    val latestAppUpdate by viewModel.latestAppUpdate.collectAsStateWithLifecycle()

    var dismissedUpdateVersion by remember { mutableStateOf<Int?>(null) }

    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val messagesMap by viewModel.messages.collectAsStateWithLifecycle()
    val selectedChat by viewModel.selectedChat.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val replyingMessage by viewModel.replyingMessage.collectAsStateWithLifecycle()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
    val activeGroupConfigs by viewModel.activeGroupConfigs.collectAsStateWithLifecycle()

    val privacyPhone by viewModel.privacyPhone.collectAsStateWithLifecycle()
    val privacyLastSeen by viewModel.privacyLastSeen.collectAsStateWithLifecycle()
    val privacyForwardProtect by viewModel.privacyForwardProtect.collectAsStateWithLifecycle()
    val privacyPasscodeLock by viewModel.privacyPasscodeLock.collectAsStateWithLifecycle()

    val showNotifications by viewModel.showNotifications.collectAsStateWithLifecycle()
    val notificationSound by viewModel.notificationSound.collectAsStateWithLifecycle()
    val notificationVibration by viewModel.notificationVibration.collectAsStateWithLifecycle()

    val userStatusGroups by viewModel.userStatusGroups.collectAsStateWithLifecycle()
    val selectedStatusGroup by viewModel.selectedStatusGroup.collectAsStateWithLifecycle()
    val selectedStatusIndex by viewModel.selectedStatusIndex.collectAsStateWithLifecycle()

    val searchContactQuery by viewModel.searchContactQuery.collectAsStateWithLifecycle()
    val searchContactResults by viewModel.searchContactResults.collectAsStateWithLifecycle()
    val isSearchingContacts by viewModel.isSearchingContacts.collectAsStateWithLifecycle()
    val savedContacts by viewModel.savedContacts.collectAsStateWithLifecycle()

    // Notification permission request
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio recording permission request (Permiso de micrófono en tiempo de ejecución)
    val recordAudioPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permiso de micrófono concedido", Toast.LENGTH_SHORT).show()
            viewModel.startVoiceRecording()
        } else {
            Toast.makeText(context, "Se necesita permiso de micrófono para grabar notas de voz", Toast.LENGTH_LONG).show()
        }
    }

    var createStatusInitialType by remember { mutableStateOf<StatusType?>(null) }

    var currentScreen by remember { mutableStateOf(if (currentUser == null) PaulScreen.AUTH else PaulScreen.CHAT_LIST) }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Handle notification click intent
        activity?.intent?.getStringExtra("chat_id")?.let { cid ->
            val chat = chats.find { it.id == cid }
            if (chat != null) {
                viewModel.selectChat(chat)
                currentScreen = PaulScreen.CHAT_DETAIL
                // Clear the extra
                activity.intent.removeExtra("chat_id")
            }
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            currentScreen = PaulScreen.AUTH
        } else if (currentScreen == PaulScreen.AUTH) {
            currentScreen = PaulScreen.CHAT_LIST
        }
    }

    LaunchedEffect(currentScreen) {
        focusManager.clearFocus()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            PaulScreen.AUTH -> {
                AuthScreen(
                    registeredUsersCount = registeredCount,
                    onLogin = { id, pass, onError ->
                        viewModel.loginUser(
                            identifier = id,
                            pass = pass,
                            onComplete = {
                                Toast.makeText(context, "Bienvenido a Paul", Toast.LENGTH_SHORT).show()
                                currentScreen = PaulScreen.CHAT_LIST
                            },
                            onError = onError
                        )
                    },
                    onRegister = { name, userHandle, email, pass, phone, bio, onError ->
                        viewModel.registerUser(
                            fullName = name,
                            username = userHandle,
                            email = email,
                            pass = pass,
                            phone = phone,
                            bio = bio,
                            onComplete = {
                                Toast.makeText(context, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show()
                                currentScreen = PaulScreen.CHAT_LIST
                            },
                            onError = onError
                        )
                    },
                    onSignInWithGoogle = {
                        activity?.let { act ->
                            viewModel.signInWithGoogle(
                                activity = act,
                                onComplete = {
                                    Toast.makeText(context, "Sesión iniciada con Google", Toast.LENGTH_SHORT).show()
                                    currentScreen = PaulScreen.CHAT_LIST
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
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
                    isDarkMode = isDarkMode,
                    isFirebaseSignedIn = isFirebaseSignedIn,
                    onSelectFolder = { viewModel.setFolder(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleSearch = { viewModel.toggleSearch(it) },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onSignInGoogle = {
                        activity?.let { act ->
                            viewModel.signInWithGoogle(
                                activity = act,
                                onComplete = {
                                    Toast.makeText(context, "Sesión iniciada con Google", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    onSearchUserInFirebase = { query ->
                        viewModel.searchUserInFirebase(query)
                    },
                    onAddContactAndOpenChat = { title, username, phone, isFirebase ->
                        viewModel.addContactAndOpenChat(title, username, phone, isFirebase)
                        currentScreen = PaulScreen.CHAT_DETAIL
                    },
                    onChatClick = { chat ->
                        viewModel.selectChat(chat)
                        currentScreen = PaulScreen.CHAT_DETAIL
                    },
                    onCreateNewChat = { title, type, desc ->
                        viewModel.createNewChat(title, type, desc)
                        currentScreen = PaulScreen.CHAT_DETAIL
                    },
                    onOpenSavedMessages = {
                        val savedChat = chats.find { it.type == com.example.data.model.ChatType.SAVED_MESSAGES }
                        if (savedChat != null) {
                            viewModel.selectChat(savedChat)
                            currentScreen = PaulScreen.CHAT_DETAIL
                        }
                    },
                    onOpenAdminPanel = {
                        currentScreen = PaulScreen.ADMIN_PANEL
                    },
                    onOpenContacts = {
                        currentScreen = PaulScreen.SEARCH_CONTACTS
                    },
                    statusGroups = userStatusGroups,
                    onOpenStatusGroup = { group ->
                        viewModel.openStatusGroup(group)
                    },
                    onCreateTextStatus = {
                        createStatusInitialType = StatusType.TEXT
                    },
                    onCreateMediaStatus = {
                        createStatusInitialType = StatusType.REEL
                    },
                    onDeleteChat = { chatId ->
                        viewModel.deleteChat(chatId)
                    },
                    onTogglePinChat = { chatId ->
                        viewModel.togglePinChat(chatId)
                    },
                    onToggleMuteChat = { chatId ->
                        viewModel.toggleMuteChat(chatId)
                    },
                    onOpenSettings = {
                        currentScreen = PaulScreen.SETTINGS
                    }
                )
            }

            PaulScreen.CHAT_DETAIL -> {
                val chat = selectedChat
                if (chat != null) {
                    val currentMessages = messagesMap[chat.id] ?: emptyList()

                    ChatDetailScreen(
                        chat = chat,
                        messages = currentMessages,
                        replyingMessage = replyingMessage,
                        isRecordingVoice = isRecordingVoice,
                        recordingSeconds = recordingSeconds,
                        isDarkTheme = isDarkMode,
                        onBack = {
                            viewModel.selectChat(null)
                            currentScreen = PaulScreen.CHAT_LIST
                        },
                        onSendMessage = { text, isViewOnce ->
                            viewModel.sendMessage(chat.id, text, isViewOnce)
                        },
                        onSendVoiceMessage = { duration ->
                            viewModel.sendVoiceMessage(chat.id, duration)
                        },
                        onSendGifMessage = { title, url ->
                            viewModel.sendGifMessage(chat.id, title, url)
                        },
                        onSendLocationMessage = { loc ->
                            viewModel.sendLocationMessage(chat.id, loc)
                        },
                        onSendPoll = { question, options ->
                            viewModel.sendPoll(chat.id, question, options)
                        },
                        onVotePoll = { msgId, optId ->
                            viewModel.votePoll(msgId, optId)
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
                            viewModel.toggleMuteChat(chat.id)
                        },
                        onTogglePin = {
                            viewModel.togglePinChat(chat.id)
                        },
                        onToggleBlock = {
                            viewModel.toggleBlockChat(chat.id)
                        },
                        onMarkMessageViewed = { msg ->
                            viewModel.markMessageAsViewed(chat.id, msg.id)
                        },
                        onClearChat = {
                            viewModel.deleteChat(chat.id)
                            currentScreen = PaulScreen.CHAT_LIST
                        },
                        onStartVoiceRecording = {
                            val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (hasMicPermission) {
                                viewModel.startVoiceRecording()
                            } else {
                                recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onCancelVoiceRecording = {
                            viewModel.cancelVoiceRecording()
                        },
                        onStopVoiceRecordingAndSend = {
                            viewModel.stopVoiceRecordingAndSend(chat.id)
                        },
                        onSendPhotoMessage = { url, cap ->
                            viewModel.sendPhotoMessage(chat.id, url, cap)
                        },
                        onSendVideoMessage = { url, cap ->
                            viewModel.sendVideoMessage(chat.id, url, cap)
                        },
                        onSendFileMessage = { url, name, size ->
                            viewModel.sendFileMessage(chat.id, url, name, size)
                        },
                        onSendContactMessage = { name, phone ->
                            viewModel.sendContactMessage(chat.id, name, phone)
                        },
                        onOpenGroupManagement = {
                            viewModel.observeGroupConfig(chat.id)
                            currentScreen = PaulScreen.GROUP_MANAGEMENT
                        },
                        onEditMessage = { msgId, txt -> viewModel.editMessage(chat.id, msgId, txt) },
                        onDeleteForMe = { msgId -> viewModel.deleteMessageForMe(chat.id, msgId) },
                        onDeleteForEveryone = { msgId -> viewModel.deleteMessageForEveryone(chat.id, msgId) },
                        onToggleStar = { msgId -> viewModel.toggleStarMessage(chat.id, msgId) },
                        onForwardMessage = { srcMsg, targetId, keepAuthor -> viewModel.forwardMessage(srcMsg, targetId, keepAuthor) },
                        onSetDisappearingTimer = { secs -> viewModel.setDisappearingTimer(chat.id, secs) },
                        onToggleChatLock = { pin -> viewModel.toggleChatLock(chat.id, pin) },
                        onReportUser = { reason -> viewModel.reportUser(chat.id, reason) },
                        onToggleArchive = { viewModel.toggleArchiveChat(chat.id) },
                        userStatusGroups = userStatusGroups,
                        onOpenStatusGroup = { group ->
                            viewModel.openStatusGroup(group)
                        }
                    )
                } else {
                    currentScreen = PaulScreen.CHAT_LIST
                }
            }

            PaulScreen.GROUP_MANAGEMENT -> {
                val chat = selectedChat
                val activeConfig = chat?.id?.let { activeGroupConfigs[it] } ?: chat?.groupConfig ?: GroupConfig(groupId = chat?.id ?: "")
                if (chat != null) {
                    BackHandler { currentScreen = PaulScreen.CHAT_DETAIL }
                    GroupManagementScreen(
                        chat = chat,
                        currentUserId = currentUser?.userId ?: "",
                        groupConfig = activeConfig,
                        onUpdateGroupConfig = { updatedConfig ->
                            viewModel.updateGroupConfig(chat.id, updatedConfig)
                        },
                        onBanUser = { targetId, massDelete ->
                            viewModel.banUserFromGroup(chat.id, targetId, massDelete)
                        },
                        onUnbanUser = { targetId ->
                            viewModel.unbanUserFromGroup(chat.id, targetId)
                        },
                        onBack = { currentScreen = PaulScreen.CHAT_DETAIL }
                    )
                } else {
                    currentScreen = PaulScreen.CHAT_LIST
                }
            }

            PaulScreen.SETTINGS -> {
                BackHandler { currentScreen = PaulScreen.CHAT_LIST }
                SettingsScreen(
                    currentUser = currentUser,
                    isDarkMode = isDarkMode,
                    isFirebaseSignedIn = isFirebaseSignedIn,
                    blockedChats = chats.filter { it.isBlocked },
                    privacyPhone = privacyPhone,
                    privacyLastSeen = privacyLastSeen,
                    privacyForwardProtect = privacyForwardProtect,
                    privacyPasscodeLock = privacyPasscodeLock,
                    showNotifications = showNotifications,
                    notificationSound = notificationSound,
                    notificationVibration = notificationVibration,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onSignInGoogle = {
                        activity?.let { act ->
                            viewModel.signInWithGoogle(
                                activity = act,
                                onComplete = {
                                    Toast.makeText(context, "Sesión iniciada con Google", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    onSignOutGoogle = {
                        viewModel.signOut {
                            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                            currentScreen = PaulScreen.AUTH
                        }
                    },
                    onUnblockChat = { chatId ->
                        viewModel.unblockChat(chatId)
                    },
                    onUpdatePrivacyPhone = { viewModel.setPrivacyPhone(it) },
                    onUpdatePrivacyLastSeen = { viewModel.setPrivacyLastSeen(it) },
                    onToggleForwardProtect = { viewModel.setPrivacyForwardProtect(it) },
                    onTogglePasscodeLock = { viewModel.setPrivacyPasscodeLock(it) },
                    onToggleNotifications = { viewModel.setShowNotifications(it) },
                    onUpdateNotificationSound = { viewModel.setNotificationSound(it) },
                    onToggleNotificationVibration = { viewModel.setNotificationVibration(it) },
                    onOpenAdminPanel = { currentScreen = PaulScreen.ADMIN_PANEL },
                    onUpdateProfile = { name, bio ->
                        viewModel.updateUserProfile(
                            newDisplayName = name,
                            newBio = bio,
                            onComplete = { Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    onCreateCloudBackup = {
                        viewModel.createCloudBackup(
                            onComplete = { Toast.makeText(context, "☁️ Copia de seguridad guardada en la nube", Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    },
                    onRestoreCloudBackup = {
                        viewModel.restoreCloudBackup(
                            onComplete = { count -> Toast.makeText(context, "🔄 Copia restaurada ($count chats recuperados)", Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    },
                    onBack = { currentScreen = PaulScreen.CHAT_LIST }
                )
            }

            PaulScreen.ADMIN_PANEL -> {
                BackHandler { currentScreen = PaulScreen.CHAT_LIST }
                val curr = currentUser
                if (curr != null && (curr.isAdmin || curr.isModerator)) {
                    AdminPanelScreen(
                        currentUser = curr,
                        allUsers = allUsers,
                        latestAppUpdate = latestAppUpdate,
                        onUpdateRole = { userId, newRole ->
                            viewModel.updateUserRole(
                                userId = userId,
                                newRole = newRole,
                                onComplete = {
                                    Toast.makeText(context, "Rol actualizado con éxito", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onToggleBan = { userId, isBanned ->
                            viewModel.toggleUserBan(
                                userId = userId,
                                isBanned = isBanned,
                                onComplete = {
                                    val statusMsg = if (isBanned) "Usuario bloqueado/suspendido" else "Usuario desbloqueado"
                                    Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onDeleteUser = { userId ->
                            viewModel.deleteUserAccount(
                                userId = userId,
                                onComplete = {
                                    Toast.makeText(context, "Cuenta de usuario eliminada de la base de datos", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onPublishUpdate = { versionName, versionCode, apkUrl, changelog, isMandatory ->
                            viewModel.publishAppUpdate(
                                versionName = versionName,
                                versionCode = versionCode,
                                apkUrl = apkUrl,
                                changelog = changelog,
                                isMandatory = isMandatory,
                                onComplete = {
                                    Toast.makeText(context, "¡Actualización publicada exitosamente!", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onBack = { currentScreen = PaulScreen.CHAT_LIST }
                    )
                } else {
                    currentScreen = PaulScreen.CHAT_LIST
                }
            }

            PaulScreen.SEARCH_CONTACTS -> {
                BackHandler { currentScreen = PaulScreen.CHAT_LIST }
                SearchContactsScreen(
                    currentUserId = currentUser?.userId ?: "",
                    searchQuery = searchContactQuery,
                    searchResults = searchContactResults,
                    isSearching = isSearchingContacts,
                    savedContacts = savedContacts,
                    deviceContacts = viewModel.getDeviceContacts(),
                    onQueryChange = { q -> viewModel.setSearchContactQuery(q) },
                    onExecuteSearch = { q -> viewModel.searchContactsInFirestore(q) },
                    onSaveContact = { u ->
                        viewModel.saveContact(
                            user = u,
                            onComplete = { Toast.makeText(context, "Añadido a tus contactos", Toast.LENGTH_SHORT).show() },
                            onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    onDeleteSavedContact = { id -> viewModel.deleteSavedContact(id) },
                    onOpenChat = { u ->
                        viewModel.openChatWithUser(u)
                        currentScreen = PaulScreen.CHAT_DETAIL
                    },
                    onOpenChatWithContact = { title, username, phone, isFirebase ->
                        viewModel.addContactAndOpenChat(title, username, phone, isFirebase)
                        currentScreen = PaulScreen.CHAT_DETAIL
                    },
                    onBack = { currentScreen = PaulScreen.CHAT_LIST }
                )
            }
        }
    }

    // Status / Reel Fullscreen Viewer Modal (WhatsApp style)
    val activeGroup = selectedStatusGroup
    if (activeGroup != null) {
        BackHandler { viewModel.closeStatusViewer() }
        StatusViewerScreen(
            group = activeGroup,
            currentIndex = selectedStatusIndex,
            onClose = { viewModel.closeStatusViewer() },
            onNext = { viewModel.nextStatus() },
            onPrevious = { viewModel.previousStatus() },
            onRecordView = { id -> viewModel.recordStatusView(id) },
            onReact = { id, emoji -> viewModel.reactToStatus(id, emoji) },
            onDeleteStatus = { id ->
                viewModel.deleteStatus(id) {
                    Toast.makeText(context, "Estado eliminado", Toast.LENGTH_SHORT).show()
                    viewModel.closeStatusViewer()
                }
            },
            onReply = { status, text ->
                viewModel.replyToStatus(status, text)
                Toast.makeText(context, "Respuesta enviada al chat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Create Status Dialog Modal (WhatsApp style)
    val statusTypeToCreate = createStatusInitialType
    if (statusTypeToCreate != null) {
        CreateStatusDialog(
            initialType = statusTypeToCreate,
            availableContacts = savedContacts,
            onDismiss = { createStatusInitialType = null },
            onPublish = { type, text, mediaUrl, bgGradient, fontStyle, caption, privacy, excludedIds, includedIds ->
                viewModel.createStatus(
                    type = type,
                    contentText = text,
                    mediaUrl = mediaUrl,
                    backgroundColor = bgGradient,
                    fontStyle = fontStyle,
                    caption = caption,
                    privacy = privacy,
                    excludedIds = excludedIds,
                    includedIds = includedIds,
                    onComplete = {
                        Toast.makeText(context, "¡Estado publicado!", Toast.LENGTH_SHORT).show()
                        createStatusInitialType = null
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // In-App Update Prompt Dialog for All Users
    val update = latestAppUpdate
    val currentVersionCode = try { com.example.BuildConfig.VERSION_CODE } catch (e: Exception) { 1 }
    
    if (update != null && update.versionCode > currentVersionCode && dismissedUpdateVersion != update.versionCode) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                if (!update.isMandatory) dismissedUpdateVersion = update.versionCode
            },
            title = {
                androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    com.example.ui.components.PaulAppLogo(size = 32.dp)
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(10.dp))
                    androidx.compose.material3.Text("🚀 ¡Nueva Versión v${update.versionName}!")
                }
            },
            text = {
                androidx.compose.foundation.layout.Column {
                    if (update.releaseDateFormatted.isNotBlank()) {
                        androidx.compose.material3.Text(
                            text = "Lanzada el: ${update.releaseDateFormatted}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                    }
                    androidx.compose.material3.Text(
                        text = "Novedades:",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                    androidx.compose.material3.Text(
                        text = update.changelog,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (update.apkDownloadUrl.isNotBlank()) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(update.apkDownloadUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Descargando actualización...", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Descargando actualización del servidor...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.TelegramBlue)
                ) {
                    androidx.compose.material3.Text("Actualizar Ahora")
                }
            },
            dismissButton = {
                if (!update.isMandatory) {
                    androidx.compose.material3.TextButton(
                        onClick = { dismissedUpdateVersion = update.versionCode }
                    ) {
                        androidx.compose.material3.Text("Más tarde")
                    }
                }
            }
        )
    }
}
