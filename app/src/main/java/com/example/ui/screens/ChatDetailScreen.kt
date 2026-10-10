package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.LocationData
import com.example.data.model.Message
import com.example.ui.components.AttachmentSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.ChatInputBar
import com.example.ui.components.GifPickerSheet
import com.example.ui.components.LocationPickerSheet
import com.example.ui.components.MessageBubble
import com.example.ui.components.ViewOnceDialog
import com.example.ui.theme.ChatPatternDark
import com.example.ui.theme.ChatPatternLight
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    messages: List<Message>,
    replyingMessage: Message?,
    isRecordingVoice: Boolean,
    recordingSeconds: Int,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onBack: () -> Unit,
    onSendMessage: (text: String, isViewOnce: Boolean) -> Unit,
    onSendVoiceMessage: (Int) -> Unit,
    onSendGifMessage: (title: String, url: String) -> Unit,
    onSendLocationMessage: (LocationData) -> Unit,
    onSendPoll: (question: String, options: List<String>) -> Unit,
    onVotePoll: (messageId: String, optionId: Int) -> Unit,
    onReaction: (Message, String) -> Unit,
    onReplyMessage: (Message) -> Unit,
    onCancelReply: () -> Unit,
    onToggleMute: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleBlock: () -> Unit = {},
    onMarkMessageViewed: (Message) -> Unit = {},
    onClearChat: () -> Unit,
    onStartVoiceRecording: () -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onStopVoiceRecordingAndSend: () -> Unit
) {
    var isSearchingInChat by remember { mutableStateOf(false) }
    var searchChatQuery by remember { mutableStateOf("") }
    var currentSearchMatchIndex by remember { mutableIntStateOf(0) }

    val matchingIndices = remember(messages, searchChatQuery) {
        if (searchChatQuery.isBlank()) emptyList()
        else messages.mapIndexedNotNull { index, msg ->
            if (msg.text.contains(searchChatQuery, ignoreCase = true)) index else null
        }
    }

    BackHandler {
        if (isSearchingInChat) {
            isSearchingInChat = false
            searchChatQuery = ""
        } else {
            onBack()
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showGifPickerSheet by remember { mutableStateOf(false) }
    var showLocationPickerSheet by remember { mutableStateOf(false) }
    var showCreatePollDialog by remember { mutableStateOf(false) }
    var showPinnedBanner by remember { mutableStateOf(chat.pinnedMessageText != null) }
    var viewingOnceMessage by remember { mutableStateOf<Message?>(null) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive (if not in search mode)
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !isSearchingInChat) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Scroll to match when search match changes
    LaunchedEffect(currentSearchMatchIndex, matchingIndices) {
        if (matchingIndices.isNotEmpty() && currentSearchMatchIndex in matchingIndices.indices) {
            val targetIndex = matchingIndices[currentSearchMatchIndex]
            listState.animateScrollToItem(targetIndex)
        }
    }

    val chatBg = if (isDarkTheme) ChatPatternDark else ChatPatternLight

    Scaffold(
        topBar = {
            if (isSearchingInChat) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchChatQuery,
                            onValueChange = {
                                searchChatQuery = it
                                currentSearchMatchIndex = 0
                            },
                            placeholder = { Text("Buscar en este chat...", fontSize = 15.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("in_chat_search_field"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchingInChat = false
                            searchChatQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cerrar búsqueda",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (searchChatQuery.isNotBlank()) {
                            val count = matchingIndices.size
                            Text(
                                text = if (count > 0) "${currentSearchMatchIndex + 1}/$count" else "0/0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (matchingIndices.isNotEmpty()) {
                                        currentSearchMatchIndex = if (currentSearchMatchIndex > 0) currentSearchMatchIndex - 1 else matchingIndices.size - 1
                                    }
                                },
                                enabled = matchingIndices.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Anterior",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (matchingIndices.isNotEmpty()) {
                                        currentSearchMatchIndex = (currentSearchMatchIndex + 1) % matchingIndices.size
                                    }
                                },
                                enabled = matchingIndices.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Siguiente",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvatarView(
                                title = chat.title,
                                type = chat.type,
                                gradientStart = chat.avatarGradientStart,
                                gradientEnd = chat.avatarGradientEnd,
                                size = 40.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (chat.isEncrypted || chat.type == com.example.data.model.ChatType.SECRET) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Cifrado E2EE",
                                            tint = Color(0xFF00C853),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    Text(
                                        text = chat.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (chat.isEncrypted || chat.type == com.example.data.model.ChatType.SECRET) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (chat.isCloudSynced) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = "Firebase Cloud",
                                            tint = TelegramAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    if (chat.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verificado",
                                            tint = TelegramAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                // Subtitle status
                                val statusText = if (chat.isBlocked) "🚫 Contacto bloqueado" else (chat.typingStatus ?: chat.subtitle)
                                val isTyping = chat.typingStatus != null
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (chat.isBlocked) Color.Red else if (isTyping) TelegramAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchingInChat = true }, modifier = Modifier.testTag("in_chat_search_button")) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar en el chat",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { /* Call */ }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Llamar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box {
                            IconButton(onClick = { showMenu = !showMenu }, modifier = Modifier.testTag("chat_menu_button")) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Más opciones",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Buscar en el chat") },
                                    onClick = {
                                        showMenu = false
                                        isSearchingInChat = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (chat.isBlocked) "Desbloquear usuario" else "Bloquear usuario") },
                                    onClick = {
                                        showMenu = false
                                        onToggleBlock()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (chat.isMuted) "Desilenciar" else "Silenciar") },
                                    onClick = {
                                        showMenu = false
                                        onToggleMute()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (chat.isPinned) "Desfijar chat" else "Fijar chat") },
                                    onClick = {
                                        showMenu = false
                                        onTogglePin()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Vaciar chat") },
                                    onClick = {
                                        showMenu = false
                                        onClearChat()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            ChatInputBar(
                replyingMessage = replyingMessage,
                isBlocked = chat.isBlocked,
                onCancelReply = onCancelReply,
                onSendMessage = onSendMessage,
                onOpenAttachments = { showAttachmentSheet = true },
                onUnblock = onToggleBlock,
                isRecordingVoice = isRecordingVoice,
                recordingSeconds = recordingSeconds,
                onStartRecording = onStartVoiceRecording,
                onCancelRecording = onCancelVoiceRecording,
                onStopRecordingAndSend = onStopVoiceRecordingAndSend,
                modifier = Modifier.imePadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(chatBg)
        ) {
            // E2EE or Cloud Indicator Banner
            if (chat.isEncrypted || chat.type == com.example.data.model.ChatType.SECRET) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9).copy(alpha = 0.95f))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chat Secreto • Cifrado de Extremo a Extremo (E2EE)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Huella de clave: ${chat.encryptionKeyFingerprint ?: "SHA-256 #D4A8-E19C"} • Sin rastros en servidores",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (chat.isCloudSynced) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD).copy(alpha = 0.95f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Base de datos Firebase Cloud activa • Sincronizado en tiempo real",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = TelegramBlue,
                        fontSize = 11.sp
                    )
                }
            }

            // Pinned message banner if present
            if (chat.pinnedMessageText != null && showPinnedBanner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Mensaje fijado",
                        tint = TelegramAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mensaje fijado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TelegramAccent,
                            fontSize = 11.sp
                        )
                        Text(
                            text = chat.pinnedMessageText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { showPinnedBanner = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar fijado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                // Date pill at top
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Hoy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isDarkTheme = isDarkTheme,
                        onReply = onReplyMessage,
                        onReaction = onReaction,
                        onVotePoll = onVotePoll,
                        onOpenViewOnce = { viewingOnceMessage = it }
                    )
                }
            }
        }
    }

    // View Once Dialog
    viewingOnceMessage?.let { msg ->
        ViewOnceDialog(
            message = msg,
            onDismissAndMarkRead = {
                onMarkMessageViewed(msg)
                viewingOnceMessage = null
            }
        )
    }

    // Attachment bottom sheet
    if (showAttachmentSheet) {
        AttachmentSheet(
            onDismiss = { showAttachmentSheet = false },
            onSelectGallery = {
                onSendMessage("📷 [Foto compartida desde la galería]", false)
            },
            onSelectGif = {
                showGifPickerSheet = true
            },
            onSelectLocation = {
                showLocationPickerSheet = true
            },
            onSelectPoll = {
                showCreatePollDialog = true
            },
            onSelectFile = {
                onSendMessage("📄 Documento_Paul_Spec.pdf (1.8 MB)", false)
            }
        )
    }

    if (showGifPickerSheet) {
        GifPickerSheet(
            onDismiss = { showGifPickerSheet = false },
            onSelectGif = onSendGifMessage
        )
    }

    if (showLocationPickerSheet) {
        LocationPickerSheet(
            onDismiss = { showLocationPickerSheet = false },
            onSendLocation = onSendLocationMessage
        )
    }

    // Create Poll Dialog
    if (showCreatePollDialog) {
        CreatePollDialog(
            onDismiss = { showCreatePollDialog = false },
            onConfirm = { question, options ->
                showCreatePollDialog = false
                onSendPoll(question, options)
            }
        )
    }
}

@Composable
private fun CreatePollDialog(
    onDismiss: () -> Unit,
    onConfirm: (question: String, options: List<String>) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Encuesta", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Pregunta") },
                    placeholder = { Text("¿Cuál es tu framework preferido?") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = opt1,
                    onValueChange = { opt1 = it },
                    label = { Text("Opción 1") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = opt2,
                    onValueChange = { opt2 = it },
                    label = { Text("Opción 2") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = opt3,
                    onValueChange = { opt3 = it },
                    label = { Text("Opción 3 (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validOptions = listOf(opt1, opt2, opt3).filter { it.isNotBlank() }
                    if (question.isNotBlank() && validOptions.size >= 2) {
                        onConfirm(question.trim(), validOptions)
                    }
                },
                enabled = question.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
