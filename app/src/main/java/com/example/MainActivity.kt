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
import androidx.compose.ui.unit.dp
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
import com.example.data.model.Chat
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PaulViewModel

enum class PaulScreen {
    AUTH,
    CHAT_LIST,
    CHAT_DETAIL,
    SETTINGS,
    ADMIN_PANEL
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
    val activity = context as? Activity

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
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

    val privacyPhone by viewModel.privacyPhone.collectAsStateWithLifecycle()
    val privacyLastSeen by viewModel.privacyLastSeen.collectAsStateWithLifecycle()
    val privacyForwardProtect by viewModel.privacyForwardProtect.collectAsStateWithLifecycle()
    val privacyPasscodeLock by viewModel.privacyPasscodeLock.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(if (currentUser == null) PaulScreen.AUTH else PaulScreen.CHAT_LIST) }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            currentScreen = PaulScreen.AUTH
        } else if (currentScreen == PaulScreen.AUTH) {
            currentScreen = PaulScreen.CHAT_LIST
        }
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
                            viewModel.startVoiceRecording()
                        },
                        onCancelVoiceRecording = {
                            viewModel.cancelVoiceRecording()
                        },
                        onStopVoiceRecordingAndSend = {
                            viewModel.stopVoiceRecordingAndSend(chat.id)
                        }
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
                    onOpenAdminPanel = { currentScreen = PaulScreen.ADMIN_PANEL },
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
        }
    }

    // In-App Update Prompt Dialog for All Users
    val update = latestAppUpdate
    if (update != null && update.versionCode > 1 && dismissedUpdateVersion != update.versionCode) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                if (!update.isMandatory) dismissedUpdateVersion = update.versionCode
            },
            title = {
                androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
