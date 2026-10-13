package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.LocationData
import com.example.data.model.Message
import com.example.ui.components.MessageBubble
import com.example.ui.theme.TelegramBlue
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    messages: List<Message>,
    replyingMessage: Message? = null,
    isRecordingVoice: Boolean = false,
    recordingSeconds: Int = 0,
    isDarkTheme: Boolean = false,
    onBack: () -> Unit,
    onSendMessage: (String, Boolean) -> Unit,
    onSendVoiceMessage: (Int) -> Unit = {},
    onSendGifMessage: (String, String) -> Unit = { _, _ -> },
    onSendLocationMessage: (LocationData) -> Unit = {},
    onSendPoll: (String, List<String>) -> Unit = { _, _ -> },
    onVotePoll: (String, String) -> Unit = { _, _ -> },
    onReaction: (Message, String) -> Unit = { _, _ -> },
    onReplyMessage: (Message) -> Unit = {},
    onCancelReply: () -> Unit = {},
    onToggleMute: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onToggleBlock: () -> Unit = {},
    onMarkMessageViewed: (Message) -> Unit = {},
    onClearChat: () -> Unit = {},
    onStartVoiceRecording: () -> Unit = {},
    onCancelVoiceRecording: () -> Unit = {},
    onStopVoiceRecordingAndSend: () -> Unit = {},
    onSendPhotoMessage: (String, String) -> Unit = { _, _ -> },
    onSendVideoMessage: (String, String) -> Unit = { _, _ -> },
    onSendFileMessage: (String, String, String) -> Unit = { _, _, _ -> },
    onSendContactMessage: (String, String) -> Unit = { _, _ -> },
    onOpenGroupManagement: () -> Unit = {},
    onEditMessage: (String, String) -> Unit = { _, _ -> },
    onDeleteForMe: (String) -> Unit = {},
    onDeleteForEveryone: (String) -> Unit = {},
    onToggleStar: (String) -> Unit = {},
    onForwardMessage: (Message, String, Boolean) -> Unit = { _, _, _ -> },
    onSetDisappearingTimer: (Int) -> Unit = {},
    onToggleChatLock: (String) -> Unit = {},
    onReportUser: (String) -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempUri(): Uri {
        val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(chat.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            text = if (chat.type == ChatType.GROUP) "Grupo" else "En línea",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (chat.type == ChatType.GROUP) {
                        IconButton(onClick = onOpenGroupManagement) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones del grupo")
                        }
                    } else {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Silenciar") }, onClick = { showMenu = false; onToggleMute() })
                            DropdownMenuItem(text = { Text("Vaciar chat") }, onClick = { showMenu = false; onClearChat() })
                            DropdownMenuItem(text = { Text("Bloquear") }, onClick = { showMenu = false; onToggleBlock() })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = false
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isDarkTheme = isDarkTheme,
                        onReaction = { emoji -> onReaction(msg, emoji) }
                    )
                }
            }

            // Reply bar preview if active
            if (replyingMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(replyingMessage.senderName, fontWeight = FontWeight.Bold, color = TelegramBlue, fontSize = 12.sp)
                        Text(replyingMessage.text, maxLines = 1, fontSize = 12.sp)
                    }
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar respuesta")
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val uri = createTempUri()
                            tempCameraUri = uri
                            cameraPhotoLauncher.launch(uri)
                        },
                        modifier = Modifier.testTag("camera_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cámara", tint = TelegramBlue)
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Escribe un mensaje...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (textInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSendMessage(textInput, false)
                                textInput = ""
                            },
                            modifier = Modifier.testTag("send_message_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = TelegramBlue)
                        }
                    } else if (isRecordingVoice) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${recordingSeconds}s", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = onStopVoiceRecordingAndSend, modifier = Modifier.testTag("stop_recording_button")) {
                                Icon(Icons.Default.Stop, contentDescription = "Detener", tint = Color.Red)
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onStartVoiceRecording,
                            modifier = Modifier.testTag("record_voice_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Grabar voz", tint = TelegramBlue)
                        }
                    }
                }
            }
        }
    }
}
