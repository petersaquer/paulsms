package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StatusItem
import com.example.data.model.StatusPrivacy
import com.example.data.model.StatusViewer
import com.example.data.model.UserStatusGroup
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QUICK_EMOJIS = listOf("❤️", "🔥", "😂", "😮", "😢", "👏", "🎉")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusViewerScreen(
    group: UserStatusGroup,
    currentIndex: Int,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRecordView: (statusId: String) -> Unit,
    onReact: (statusId: String, emoji: String) -> Unit,
    onDeleteStatus: (statusId: String) -> Unit,
    onReply: (status: StatusItem, replyText: String) -> Unit
) {
    val statuses = group.statuses
    if (statuses.isEmpty() || currentIndex !in statuses.indices) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val currentStatus = statuses[currentIndex]
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showViewersSheet by remember { mutableStateOf(false) }

    // Automatic progress bar
    val progress = remember(currentIndex, group.userId) { Animatable(0f) }

    LaunchedEffect(currentStatus.id) {
        onRecordView(currentStatus.id)
    }

    LaunchedEffect(currentIndex, group.userId, isPaused) {
        if (!isPaused) {
            val remaining = (1f - progress.value) * 5000f
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = remaining.toInt().coerceAtLeast(100),
                    easing = LinearEasing
                )
            )
            onNext()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(currentIndex, group.userId) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth * 0.35f) {
                            onPrevious()
                        } else {
                            onNext()
                        }
                    }
                )
            }
    ) {
        // CONTENT DISPLAY
        if (currentStatus.isText) {
            val bgColor = Color(currentStatus.backgroundColor)
            val font = when (currentStatus.fontStyle) {
                "SERIF" -> FontFamily.Serif
                "MONO" -> FontFamily.Monospace
                "CURSIVE" -> FontFamily.Cursive
                else -> FontFamily.Default
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentStatus.contentText,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )
            }
        } else {
            // Media or Reel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (currentStatus.mediaUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentStatus.mediaUrl,
                        contentDescription = "Estado",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (currentStatus.isReel) {
                    // Reel badge indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 90.dp, end = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REEL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Caption overlay
                if (currentStatus.caption.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 90.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = currentStatus.caption,
                            color = Color.White,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // TOP SEGMENTED PROGRESS BARS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 12.dp, end = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                statuses.forEachIndexed { idx, _ ->
                    val segmentProgress = when {
                        idx < currentIndex -> 1f
                        idx == currentIndex -> progress.value
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segmentProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TOP USER BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(group.userAvatarStart), Color(group.userAvatarEnd))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = group.displayName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = group.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val timeAgo = remember(currentStatus.createdAt) {
                                val diff = System.currentTimeMillis() - currentStatus.createdAt
                                val minutes = diff / (60 * 1000)
                                val hours = minutes / 60
                                when {
                                    minutes < 1 -> "Justo ahora"
                                    minutes < 60 -> "Hace ${minutes}m"
                                    hours < 24 -> "Hace ${hours}h"
                                    else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(currentStatus.createdAt))
                                }
                            }
                            Text(
                                text = timeAgo,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${currentIndex + 1}/${statuses.size}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (group.isCurrentUser) {
                        IconButton(
                            onClick = { onDeleteStatus(currentStatus.id) },
                            modifier = Modifier.testTag("delete_my_status_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar estado", tint = Color.White)
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_status_viewer")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }
            }
        }

        // BOTTOM ACTION BAR
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            if (group.isCurrentUser) {
                // OWNER VIEW: Viewers Counter & Sheet Trigger
                Surface(
                    onClick = { showViewersSheet = true },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("view_status_viewers_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${currentStatus.viewers.size} vistas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // VIEWER: Reactions & Reply Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick emoji reactions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        QUICK_EMOJIS.forEach { emoji ->
                            val myReaction = currentStatus.reactions[group.userId]
                            val isSelected = myReaction == emoji
                            Surface(
                                onClick = { onReact(currentStatus.id, emoji) },
                                shape = CircleShape,
                                color = if (isSelected) TelegramAccent.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    // Reply Text Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Responder a este estado...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                focusedBorderColor = TelegramAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = TelegramAccent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reply_status_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onReply(currentStatus, replyText)
                                    replyText = ""
                                }
                            },
                            enabled = replyText.isNotBlank(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (replyText.isNotBlank()) TelegramAccent else Color.White.copy(alpha = 0.2f))
                                .size(48.dp)
                                .testTag("send_status_reply_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar respuesta", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    // VIEWERS MODAL BOTTOM SHEET (WhatsApp style)
    if (showViewersSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showViewersSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visto por (${currentStatus.viewers.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showViewersSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (currentStatus.viewers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún nadie ha visto este estado.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(currentStatus.viewers, key = { it.userId + it.viewedAt }) { viewer ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(viewer.avatarGradientStart), Color(viewer.avatarGradientEnd))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = viewer.displayName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = viewer.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = viewer.username,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(viewer.viewedAt))
                                Text(
                                    text = timeStr,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
