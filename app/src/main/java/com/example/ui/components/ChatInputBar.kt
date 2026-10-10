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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.ui.components.EmojiPickerSheet
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@Composable
fun ChatInputBar(
    replyingMessage: Message?,
    isBlocked: Boolean = false,
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

    if (showEmojiPicker) {
        EmojiPickerSheet(
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = { emoji ->
                text += emoji
                // showEmojiPicker = false // Don't close on selection (WhatsApp style)
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

            // Main Input Row
            if (isRecordingVoice) {
                VoiceRecordingIndicatorBar(
                    seconds = recordingSeconds,
                    onCancel = onCancelRecording,
                    onSend = onStopRecordingAndSend
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

                    // Right action: Voice or Send
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
                        // Voice Record Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TelegramAccent)
                                .clickable { onStartRecording() }
                                .testTag("mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Grabar nota de voz",
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

        Text(
            text = "Grabando audio...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        // Cancel Button
        IconButton(
            onClick = onCancel,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancelar grabación",
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
