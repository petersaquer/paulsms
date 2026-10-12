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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.model.Chat
import com.example.data.model.LocationData
import com.example.data.model.Message
import com.example.ui.components.AttachmentSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ContactViewerDialog
import com.example.ui.components.GifPickerSheet
import com.example.ui.components.LocationPickerSheet
import com.example.ui.components.MapViewerDialog
import com.example.ui.components.MediaViewerDialog
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
    onStopVoiceRecordingAndSend: () -> Unit,
    onSendPhotoMessage: (photoUrl: String, caption: String) -> Unit = { _, _ -> },
    onSendVideoMessage: (videoUrl: String, caption: String) -> Unit = { _, _ -> },
    onSendFileMessage: (fileUrl: String, fileName: String, fileSize: String) -> Unit = { _, _, _ -> },
    onSendContactMessage: (contactName: String, contactPhone: String) -> Unit = { _, _ -> },
    onOpenGroupManagement: () -> Unit = {},
    onEditMessage: (messageId: String, newText: String) -> Unit = { _, _ -> },
    onDeleteForMe: (messageId: String) -> Unit = {},
    onDeleteForEveryone: (messageId: String) -> Unit = {},
    onToggleStar: (messageId: String) -> Unit = {},
    onForwardMessage: (sourceMsg: Message, targetChatId: String, keepAuthor: Boolean) -> Unit = { _, _, _ -> },
    onSetDisappearingTimer: (seconds: Int) -> Unit = {},
    onToggleChatLock: (pin: String) -> Unit = {},
    onReportUser: (reason: String) -> Unit = {},
    onToggleArchive: () -> Unit = {}
) {
    var isSearchingInChat by remember { mutableStateOf(false) }
    var searchChatQuery by remember { mutableStateOf("") }
    var currentSearchMatchIndex by remember { mutableIntStateOf(0) }

    // Media & Map Viewer Dialog states
    var viewingLocationInMap by remember { mutableStateOf<LocationData?>(null) }
    var viewingMediaUrl by remember { mutableStateOf<String?>(null) }
    var viewingIsVideo by remember { mutableStateOf(false) }
    var viewingMediaCaption by remember { mutableStateOf<String?>(null) }
    var viewingContact by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Dual Chat 1-on-1 Dialog states
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var forwardingMessage by remember { mutableStateOf<Message?>(null) }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var showChatLockDialog by remember { mutableStateOf(false) }
    var showStarredSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    fun createTempUri(): android.net.Uri {
        val file = java.io.File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { onSendPhotoMessage(it.toString(), "📷 Foto de cámara") }
        }
    }

    val cameraVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            tempCameraUri?.let { onSendVideoMessage(it.toString(), "🎬 Video de cámara") }
        }
    }

    // Media pickers launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val uriStr = uri.toString()
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType?.startsWith("video") == true) {
                onSendVideoMessage(uriStr, "🎬 Video compartido")
            } else {
                onSendPhotoMessage(uriStr, "📷 Foto compartida")
            }
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "Documento_adjunto"
            onSendFileMessage(uri.toString(), name, "Archivo adjunto")
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "Audio_compartido.mp3"
            onSendFileMessage(uri.toString(), "🎵 $name", "Audio MP3")
        }
    }

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

                        IconButton(onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${chat.contactPhone ?: "+595981123456"}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback or permission check
                            }
                        }) {
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
                                if (chat.type == com.example.data.model.ChatType.GROUP) {
                                    DropdownMenuItem(
                                        text = { Text("Ajustes del Grupo / RBAC", fontWeight = FontWeight.Bold, color = TelegramBlue) },
                                        onClick = {
                                            showMenu = false
                                            onOpenGroupManagement()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Buscar en el chat") },
                                    onClick = {
                                        showMenu = false
                                        isSearchingInChat = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("⏳ Mensajes temporales (Autodestrucción)") },
                                    onClick = {
                                        showMenu = false
                                        showDisappearingDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (chat.isLocked) "🔓 Desbloquear chat (PIN)" else "🔒 Bloquear chat (PIN)") },
                                    onClick = {
                                        showMenu = false
                                        showChatLockDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("⭐ Mensajes destacados") },
                                    onClick = {
                                        showMenu = false
                                        showStarredSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (chat.isArchived) "📂 Desarchivar chat" else "📂 Archivar chat") },
                                    onClick = {
                                        showMenu = false
                                        onToggleArchive()
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
                                DropdownMenuItem(
                                    text = { Text("🚩 Reportar usuario", color = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        showReportDialog = true
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
                        onOpenViewOnce = { viewingOnceMessage = it },
                        onOpenMap = { loc -> viewingLocationInMap = loc },
                        onOpenMedia = { url, isVid, cap ->
                            viewingMediaUrl = url
                            viewingIsVideo = isVid
                            viewingMediaCaption = cap
                        },
                        onOpenContact = { name, phone ->
                            viewingContact = name to phone
                        },
                        onEditMessage = { msg -> editingMessage = msg },
                        onDeleteForMe = { msg -> onDeleteForMe(msg.id) },
                        onDeleteForEveryone = { msg -> onDeleteForEveryone(msg.id) },
                        onToggleStar = { msg -> onToggleStar(msg.id) },
                        onForwardMessage = { msg -> forwardingMessage = msg },
                        onReportMessage = { _ -> showReportDialog = true }
                    )
                }
            }
        }
    }

    // Interactive Map Viewer Dialog
    viewingLocationInMap?.let { loc ->
        MapViewerDialog(
            location = loc,
            onDismiss = { viewingLocationInMap = null }
        )
    }

    // Media Viewer Dialog
    viewingMediaUrl?.let { url ->
        MediaViewerDialog(
            mediaUrl = url,
            isVideo = viewingIsVideo,
            caption = viewingMediaCaption,
            onDismiss = { viewingMediaUrl = null }
        )
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

    // Contact Viewer Dialog
    viewingContact?.let { (name, phone) ->
        ContactViewerDialog(
            name = name,
            phone = phone,
            onDismiss = { viewingContact = null },
            onSendMessage = { text ->
                onSendMessage(text, false)
                viewingContact = null
            }
        )
    }

    // Attachment bottom sheet
    if (showAttachmentSheet) {
        AttachmentSheet(
            onDismiss = { showAttachmentSheet = false },
            onSelectCamera = {
                val uri = createTempUri()
                tempCameraUri = uri
                cameraPhotoLauncher.launch(uri)
            },
            onSelectGallery = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
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
                fileLauncher.launch("*/*")
            },
            onSelectContact = {
                // Better mock contact sharing
                onSendContactMessage("Alex Dev", "+595 981 123 456")
            },
            onSelectAudio = {
                audioLauncher.launch("audio/*")
            },
            onSelectVideoCamera = {
                val uri = createTempUri()
                tempCameraUri = uri
                cameraVideoLauncher.launch(uri)
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

    // Edit Message Dialog
    editingMessage?.let { msg ->
        var editTxt by remember { mutableStateOf(msg.text) }
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("Editar mensaje", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editTxt,
                    onValueChange = { editTxt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Texto corregido") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTxt.isNotBlank()) {
                            onEditMessage(msg.id, editTxt.trim())
                            editingMessage = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Guardar cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) { Text("Cancelar") }
            }
        )
    }

    // Disappearing Messages Timer Dialog
    if (showDisappearingDialog) {
        var selectedSecs by remember { mutableIntStateOf(chat.disappearingTimerSeconds) }
        AlertDialog(
            onDismissRequest = { showDisappearingDialog = false },
            title = { Text("Mensajes temporales", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Los nuevos mensajes desaparecerán automáticamente del chat después del tiempo configurado.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        0 to "Desactivado",
                        86400 to "24 horas",
                        604800 to "7 días",
                        7776000 to "90 días"
                    ).forEach { (secs, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSecs = secs }
                                .padding(vertical = 8.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedSecs == secs,
                                onClick = { selectedSecs = secs }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontWeight = if (selectedSecs == secs) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetDisappearingTimer(selectedSecs)
                        showDisappearingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = { showDisappearingDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Chat Lock Dialog
    if (showChatLockDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChatLockDialog = false },
            title = { Text(if (chat.isLocked) "Desbloquear Chat" else "Bloquear Chat con PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(if (chat.isLocked) "Ingresa el PIN para quitar la protección de este chat:" else "Ingresa un PIN de 4 dígitos para proteger este chat:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("PIN de seguridad (4 dígitos)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4 || chat.isLocked) {
                            onToggleChatLock(pinInput)
                            showChatLockDialog = false
                        }
                    },
                    enabled = pinInput.length == 4 || chat.isLocked,
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) { Text(if (chat.isLocked) "Desbloquear" else "Bloquear Chat") }
            },
            dismissButton = {
                TextButton(onClick = { showChatLockDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Starred Messages Sheet
    if (showStarredSheet) {
        val starredMsgs = messages.filter { it.isStarred }
        AlertDialog(
            onDismissRequest = { showStarredSheet = false },
            title = { Text("⭐ Mensajes Destacados (${starredMsgs.size})", fontWeight = FontWeight.Bold) },
            text = {
                if (starredMsgs.isEmpty()) {
                    Text("No tienes mensajes destacados en esta conversación. Manten presionado un mensaje y selecciona 'Destacar'.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        items(starredMsgs) { sm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sm.text.take(60), modifier = Modifier.weight(1f), fontSize = 13.sp)
                                IconButton(onClick = { onToggleStar(sm.id) }) {
                                    Text("⭐")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStarredSheet = false }) { Text("Cerrar") }
            }
        )
    }

    // Report User Dialog
    if (showReportDialog) {
        var reason by remember { mutableStateOf("Spam o mensajes no deseados") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("🚩 Reportar Usuario / Abuso", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Selecciona el motivo del reporte. Se enviar la información a la consola de seguridad de Paul Messenger:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(
                        "Spam o mensajes no deseados",
                        "Acoso o conducta inapropiada",
                        "Suplantación de identidad",
                        "Contenido ilegal o peligroso"
                    ).forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reason = r }
                                .padding(vertical = 6.dp)
                        ) {
                            androidx.compose.material3.RadioButton(selected = reason == r, onClick = { reason = r })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(r, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportUser(reason)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Enviar reporte") }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Forward Message Dialog
    forwardingMessage?.let { srcMsg ->
        var targetChatId by remember { mutableStateOf("chat_1") }
        var keepAuthor by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { forwardingMessage = null },
            title = { Text("🔄 Reenviar mensaje", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Reenviar mensaje: \"${srcMsg.text.take(30)}...\"", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = keepAuthor,
                            onCheckedChange = { keepAuthor = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Conservar autor original (${srcMsg.senderName ?: "Contacto"})", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onForwardMessage(srcMsg, chat.id, keepAuthor)
                        forwardingMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) { Text("Reenviar") }
            },
            dismissButton = {
                TextButton(onClick = { forwardingMessage = null }) { Text("Cancelar") }
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
