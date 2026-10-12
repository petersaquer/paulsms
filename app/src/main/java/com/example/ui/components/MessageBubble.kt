package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.theme.DarkBubbleIncoming
import com.example.ui.theme.DarkBubbleOutgoing
import com.example.ui.theme.LightBubbleIncoming
import com.example.ui.theme.LightBubbleOutgoing
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onReply: (Message) -> Unit,
    onReaction: (Message, String) -> Unit,
    onVotePoll: (String, Int) -> Unit,
    onOpenViewOnce: (Message) -> Unit = {},
    onOpenMap: (com.example.data.model.LocationData) -> Unit = {},
    onOpenMedia: (url: String, isVideo: Boolean, caption: String?) -> Unit = { _, _, _ -> },
    onOpenContact: (name: String, phone: String) -> Unit = { _, _ -> },
    onEditMessage: (Message) -> Unit = {},
    onDeleteForMe: (Message) -> Unit = {},
    onDeleteForEveryone: (Message) -> Unit = {},
    onToggleStar: (Message) -> Unit = {},
    onForwardMessage: (Message) -> Unit = {},
    onReportMessage: (Message) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showReactionsPicker by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }

    val bubbleColor = if (message.isOutgoing) {
        if (isDarkTheme) DarkBubbleOutgoing else LightBubbleOutgoing
    } else {
        if (isDarkTheme) DarkBubbleIncoming else LightBubbleIncoming
    }

    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val alignment = if (message.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start
        ) {
            // Context Menu Dropdown
            androidx.compose.material3.DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("💬 Responder") },
                    onClick = {
                        showContextMenu = false
                        onReply(message)
                    }
                )
                if (message.isOutgoing && !message.isDeletedForEveryone && message.type == MessageType.TEXT) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("✏️ Editar mensaje") },
                        onClick = {
                            showContextMenu = false
                            onEditMessage(message)
                        }
                    )
                }
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(if (message.isStarred) "⭐ Quitar destacado" else "⭐ Destacar (Favorito)") },
                    onClick = {
                        showContextMenu = false
                        onToggleStar(message)
                    }
                )
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("🔄 Reenviar") },
                    onClick = {
                        showContextMenu = false
                        onForwardMessage(message)
                    }
                )
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("🗑️ Eliminar para mí") },
                    onClick = {
                        showContextMenu = false
                        onDeleteForMe(message)
                    }
                )
                if (message.isOutgoing && !message.isDeletedForEveryone) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("🚫 Eliminar para todos", color = Color.Red) },
                        onClick = {
                            showContextMenu = false
                            onDeleteForEveryone(message)
                        }
                    )
                }
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("🚩 Reportar abuso") },
                    onClick = {
                        showContextMenu = false
                        onReportMessage(message)
                    }
                )
            }

            // Quick reactions floating bar if tapped
            AnimatedVisibility(
                visible = showReactionsPicker,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ReactionsPickerRow(
                    onSelect = { emoji ->
                        onReaction(message, emoji)
                        showReactionsPicker = false
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Main Bubble Card
            Card(
                shape = bubbleShape,
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .widthIn(min = 64.dp, max = 320.dp)
                    .combinedClickable(
                        onClick = {
                            if (message.isViewOnce && !message.hasBeenViewed && !message.isOutgoing) {
                                onOpenViewOnce(message)
                            } else {
                                showReactionsPicker = !showReactionsPicker
                            }
                        },
                        onDoubleClick = {
                            onReaction(message, "❤️")
                        },
                        onLongClick = {
                            showContextMenu = true
                        }
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    // Sender name for group chats
                    if (!message.isOutgoing && !message.senderName.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 3.dp)
                        ) {
                            Text(
                                text = if (message.isAnonymous) "Grupo (Anónimo)" else message.senderName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (message.senderAvatarGradient != null) Color(message.senderAvatarGradient) else TelegramAccent,
                                fontSize = 12.sp
                            )
                            if (!message.userBadge.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TelegramBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = message.userBadge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TelegramBlue
                                    )
                                }
                            }
                        }
                    }

                    // Replied-to Quote Preview
                    if (message.replyTo != null) {
                        ReplyQuoteBox(
                            replyTo = message.replyTo,
                            isOutgoing = message.isOutgoing,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Content depending on message type or View Once state
                    if (message.isViewOnce) {
                        if (message.hasBeenViewed) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockClock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Mensaje autodestruido",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Lectura única expirada",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF3E0))
                                    .clickable {
                                        if (!message.isOutgoing) {
                                            onOpenViewOnce(message)
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9800)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "1",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Mensaje de lectura única",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = if (message.isOutgoing) "El destinatario solo podrá abrirlo una vez" else "Toca para abrir • Se autodestruirá",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFBF360C),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        when (message.type) {
                            MessageType.PHOTO -> {
                                val url = message.photoUrl ?: ""
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { if (url.isNotBlank()) onOpenMedia(url, false, message.text) }
                                ) {
                                    coil.compose.AsyncImage(
                                        model = url,
                                        contentDescription = "Foto",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    if (message.text.isNotBlank()) {
                                        Text(
                                            text = message.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                        )
                                    }
                                }
                            }
                            MessageType.VIDEO -> {
                                val url = message.videoUrl ?: message.photoUrl ?: ""
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { if (url.isNotBlank()) onOpenMedia(url, true, message.text) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = url,
                                            contentDescription = "Video preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("▶", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (message.text.isNotBlank()) {
                                        Text(
                                            text = message.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                        )
                                    }
                                }
                            }
                            MessageType.FILE -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(TelegramBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📄", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = message.fileName ?: message.text.ifBlank { "Archivo adjunto" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = message.fileSize ?: "Documento",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            MessageType.CONTACT -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            onOpenContact(
                                                message.contactName ?: "Contacto",
                                                message.contactPhone ?: ""
                                            )
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(TelegramAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("👤", fontSize = 22.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = message.contactName ?: "Contacto compartido",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (!message.contactPhone.isNullOrBlank()) {
                                                Text(
                                                    text = message.contactPhone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            MessageType.VOICE -> {
                                VoicePlayer(
                                    durationSeconds = message.voiceDurationSeconds ?: 12,
                                    waveform = message.voiceWaveform,
                                    isOutgoing = message.isOutgoing
                                )
                            }
                            MessageType.POLL -> {
                                message.pollData?.let { poll ->
                                    PollWidget(
                                        poll = poll,
                                        onVote = { optId -> onVotePoll(message.id, optId) }
                                    )
                                }
                            }
                            MessageType.STICKER -> {
                                Text(
                                    text = message.stickerEmoji ?: "🌟",
                                    fontSize = 48.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            MessageType.GIF -> {
                                message.gifUrl?.let { url ->
                                    GifMessageWidget(
                                        gifUrl = url,
                                        title = message.text
                                    )
                                }
                            }
                            MessageType.LOCATION, MessageType.LIVE_LOCATION -> {
                                message.locationData?.let { loc ->
                                    LocationMessageWidget(
                                        location = loc,
                                        isOutgoing = message.isOutgoing,
                                        onClick = { onOpenMap(loc) }
                                    )
                                }
                            }
                            else -> {
                                if (message.isDeletedForEveryone) {
                                    Text(
                                        text = "🚫 Este mensaje fue eliminado",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Footer: Starred indicator, Edited tag, Timestamp & status checkmarks
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (message.isStarred) {
                            Text("⭐", fontSize = 10.sp)
                        }

                        if (message.isEdited && !message.isDeletedForEveryone) {
                            Text(
                                text = "editado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }

                        if (message.isEncrypted) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Cifrado E2EE",
                                tint = if (message.isOutgoing) TelegramAccent else Color(0xFF00C853),
                                modifier = Modifier.size(11.dp)
                            )
                        }

                        Text(
                            text = message.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )

                        if (message.isOutgoing && !message.isDeletedForEveryone) {
                            when (message.status) {
                                MessageStatus.READ -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Leído (Doble check azul)",
                                        tint = TelegramAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                MessageStatus.DELIVERED -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Entregado (Doble check)",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                MessageStatus.SENT -> {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Enviado (Un check)",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                MessageStatus.SENDING -> {
                                    Text("...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Reactions Badges Row
            if (message.reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.reactions.forEach { (emoji, count) ->
                        val isUserReacted = message.userReaction == emoji
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isUserReacted) TelegramAccent.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isUserReacted) TelegramAccent else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onReaction(message, emoji) }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$emoji $count",
                                fontSize = 12.sp,
                                fontWeight = if (isUserReacted) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyQuoteBox(
    replyTo: Message,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor = if (isOutgoing) TelegramBlue else TelegramAccent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = replyTo.senderName ?: if (replyTo.isOutgoing) "Tú" else "Contacto",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = barColor,
                fontSize = 11.sp
            )
            Text(
                text = replyTo.text.take(40),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ReactionsPickerRow(
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("❤️", "👍", "👎", "🔥", "😂", "👏", "🎉", "😮", "😢", "💯", "🚀", "😍", "🤔")

    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.widthIn(max = 300.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            emojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onSelect(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}
