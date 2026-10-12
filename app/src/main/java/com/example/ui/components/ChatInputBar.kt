package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.data.model.GroupMember
import com.example.data.model.Message
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import kotlin.math.roundToInt
import com.example.ui.components.EmojiPickerSheet

@Composable
fun ChatInputBar(
    replyingMessage: Message?,
    isBlocked: Boolean = false,
    isMutedInGroup: Boolean = false,
    membersForMentions: List<com.example.data.model.GroupMember> = emptyList(),
    onCancelReply: () -> Unit,
    onSendMessage: (text: String, isViewOnce: Boolean) -> Unit,
    onOpenAttachments: () -> Unit,
    onUnblock: () -> Unit = {},
    isRecordingVoice: Boolean,
    recordingSeconds: Int,
    onStartRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onStopRecordingAndSend: () -> Unit,
    onOpenEmojiPicker: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var isViewOnce by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isVoiceLocked by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    val mentionQuery = remember(text) {
        val lastWord = text.substringAfterLast(" ", text)
        if (lastWord.startsWith("@") && lastWord.length >= 1) lastWord.removePrefix("@") else null
    }

    if (showEmojiPicker) {
        EmojiPickerSheet(
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = { emoji ->
                text += emoji
            }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Muted Banner Mode
            if (isMutedInGroup) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "No puedes enviar mensajes en este grupo (Silenciado por un Administrador)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                }
                return@Column
            }

            // Mentions Popup Autocomplete
            if (mentionQuery != null && membersForMentions.isNotEmpty()) {
                val filteredMembers = remember(mentionQuery, membersForMentions) {
                    val special = listOf("@todos", "@admin")
                    val matchingSpecial = special.filter { it.removePrefix("@").contains(mentionQuery, ignoreCase = true) }
                    val matchingMembers = membersForMentions.filter {
                        it.username.contains(mentionQuery, ignoreCase = true) ||
                                it.displayName.contains(mentionQuery, ignoreCase = true)
                    }
                    matchingSpecial to matchingMembers
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        items(filteredMembers.first) { spec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val prefix = text.substringBeforeLast("@")
                                        text = "$prefix$spec "
                                    }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(spec, fontWeight = FontWeight.Bold, color = TelegramBlue, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mención masiva", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        items(filteredMembers.second) { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val prefix = text.substringBeforeLast("@")
                                        val handle = if (m.username.startsWith("@")) m.username else "@${m.username}"
                                        text = "$prefix$handle "
                                    }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(m.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(m.username, fontSize = 12.sp, color = TelegramBlue)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(m.roleLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            // Blocked Banner Mode
            if (isBlocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Has bloqueado a este usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onUnblock,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Desbloquear", fontSize = 12.sp)
                    }
                }
                return@Column
            }

            // Reply Header if active
            AnimatedVisibility(
                visible = replyingMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (replyingMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(TelegramAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Respondiendo a ${replyingMessage.senderName ?: if (replyingMessage.isOutgoing) "Tú" else "Contacto"}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TelegramAccent
                            )
                            Text(
                                text = replyingMessage.text.take(35),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = onCancelReply,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar respuesta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // View Once Active Banner
            if (isViewOnce) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mensaje de lectura única (se autodestruye al abrirse)",
                        fontSize = 12.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isViewOnce = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar lectura única",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Main Input Row or Voice Recording Row
            if (isRecordingVoice) {
                VoiceRecordingIndicatorBar(
                    seconds = recordingSeconds,
                    isLocked = isVoiceLocked,
                    onCancel = {
                        isVoiceLocked = false
                        dragOffsetY = 0f
                        dragOffsetX = 0f
                        onCancelRecording()
                    },
                    onSend = {
                        isVoiceLocked = false
                        dragOffsetY = 0f
                        dragOffsetX = 0f
                        onStopRecordingAndSend()
                    }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment Clip Button
                    IconButton(
                        onClick = onOpenAttachments,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("attach_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntar archivo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Text Field Container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                placeholder = {
                                    Text(
                                        text = if (isViewOnce) "Mensaje de lectura única..." else "Mensaje...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 15.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                maxLines = 4,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("message_input_field")
                            )

                            // View Once (1) Toggle Button
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isViewOnce) Color(0xFFFF9800) else Color.Transparent)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isViewOnce) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        shape = CircleShape
                                    )
                                    .clickable { isViewOnce = !isViewOnce }
                                    .testTag("view_once_toggle_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "1",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isViewOnce) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Emoji Picker Trigger
                            IconButton(
                                onClick = { showEmojiPicker = true },
                                modifier = Modifier.size(34.dp).testTag("emoji_picker_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfiedAlt,
                                    contentDescription = "Emoji",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right action: Voice (Hold/Slide WhatsApp style) or Send
                    if (text.isNotBlank()) {
                        // Send Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isViewOnce) Color(0xFFFF9800) else TelegramBlue)
                                .clickable {
                                    onSendMessage(text, isViewOnce)
                                    text = ""
                                    isViewOnce = false
                                }
                                .testTag("send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar mensaje",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // WhatsApp style: Press & Hold to record, Drag up to lock, Drag left to cancel, Click to tap-record
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .offset { IntOffset(0, dragOffsetY.roundToInt().coerceIn(-120, 0)) }
                                .clip(CircleShape)
                                .background(TelegramAccent)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            // Fallback: tap toggles recording directly
                                            isVoiceLocked = true
                                            onStartRecording()
                                        },
                                        onPress = {
                                            isVoiceLocked = false
                                            dragOffsetY = 0f
                                            dragOffsetX = 0f
                                            onStartRecording()
                                            val released = tryAwaitRelease()
                                            if (released) {
                                                // User released finger
                                                if (!isVoiceLocked) {
                                                    if (dragOffsetX < -100f) {
                                                        onCancelRecording()
                                                    } else {
                                                        onStopRecordingAndSend()
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y
                                            dragOffsetX += dragAmount.x

                                            // Slide up by 80px -> Lock audio for long recording hands-free
                                            if (dragOffsetY < -80f && !isVoiceLocked) {
                                                isVoiceLocked = true
                                            }
                                            // Slide left by 120px -> Cancel recording
                                            if (dragOffsetX < -120f) {
                                                isVoiceLocked = false
                                                onCancelRecording()
                                            }
                                        },
                                        onDragEnd = {
                                            if (!isVoiceLocked) {
                                                if (dragOffsetX < -100f) {
                                                    onCancelRecording()
                                                } else {
                                                    onStopRecordingAndSend()
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            if (!isVoiceLocked) {
                                                onCancelRecording()
                                            }
                                        }
                                    )
                                }
                                .testTag("mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Mantén presionado para hablar, desliza hacia arriba para bloquear",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceRecordingIndicatorBar(
    seconds: Int,
    isLocked: Boolean,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_dot_alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red Recording Dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = alpha))
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Timer
        Text(
            text = String.format("%02d:%02d", seconds / 60, seconds % 60),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Status text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TelegramAccent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Audio bloqueado (manos libres)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TelegramAccent
                )
            } else {
                Text(
                    text = "Desliza ⇦ cancelar | ⇧ fijar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cancel / Trash Button
        IconButton(
            onClick = onCancel,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Cancelar y descartar audio",
                tint = Color.Red
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Send Audio Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TelegramBlue)
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Enviar nota de voz",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
