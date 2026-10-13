package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.UserStatusGroup
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactViewerDialog(
    name: String,
    phone: String,
    username: String = "",
    chat: Chat? = null,
    statusGroup: UserStatusGroup? = null,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit = {},
    onOpenStatusGroup: ((UserStatusGroup) -> Unit)? = null,
    onAudioCall: (() -> Unit)? = null,
    onVideoCall: (() -> Unit)? = null,
    onToggleMute: (() -> Unit)? = null,
    onToggleBlock: (() -> Unit)? = null,
    onReportUser: ((String) -> Unit)? = null,
    onSetDisappearingTimer: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var showDisappearingOptions by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil del Contacto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_profile_dialog")) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // Avatar & Name Banner Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val hasStatus = statusGroup != null && statusGroup.statuses.isNotEmpty()
                    val ringColor = if (hasStatus) {
                        if (statusGroup!!.hasUnseen) Color(0xFF00E676) else TelegramBlue
                    } else Color.Transparent

                    // Ring container
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(ringColor)
                            .padding(if (hasStatus) 3.5.dp else 0.dp)
                            .clickable(enabled = hasStatus) {
                                if (statusGroup != null && onOpenStatusGroup != null) {
                                    onDismiss()
                                    onOpenStatusGroup(statusGroup)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(85.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(chat?.avatarGradientStart ?: 0xFF2AABEE),
                                            Color(chat?.avatarGradientEnd ?: 0xFF1976D2)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Active Story Badge
                    if (hasStatus) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(if (statusGroup!!.hasUnseen) Color(0xFF00E676) else TelegramBlue)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Ver Estado",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val displayUsername = if (username.isNotBlank()) username else chat?.username ?: ""
                if (displayUsername.isNotBlank()) {
                    Text(
                        text = if (displayUsername.startsWith("@")) displayUsername else "@$displayUsername",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TelegramBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val statusText = if (chat?.isBlocked == true) "🚫 Bloqueado" else (chat?.subtitle ?: "en línea")
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    color = if (chat?.isBlocked == true) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Active Story Action Button (if user has active story)
                if (statusGroup != null && statusGroup.statuses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenStatusGroup?.invoke(statusGroup)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (statusGroup.hasUnseen) Color(0xFF00C853) else TelegramBlue
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("view_active_status_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (statusGroup.hasUnseen) "✨ Ver Nuevo Estado Activo" else "Ver Estado Publicado",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Message button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onSendMessage("Hola $name")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp), tint = TelegramBlue)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Mensaje", fontSize = 11.sp, color = TelegramBlue)
                    }
                }

                // Audio Call
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        if (onAudioCall != null) onAudioCall() else {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp), tint = TelegramBlue)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Llamar", fontSize = 11.sp, color = TelegramBlue)
                    }
                }

                // Video Call
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onVideoCall?.invoke()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(20.dp), tint = TelegramBlue)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Video", fontSize = 11.sp, color = TelegramBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información del contacto",
                        style = MaterialTheme.typography.labelMedium,
                        color = TelegramBlue,
                        fontWeight = FontWeight.Bold
                    )

                    if (phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = phone, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Móvil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    val bio = "Disponible en Paul App • Mensajería Cifrada E2EE"
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = bio, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Ajustes de Privacidad y Seguridad",
                        style = MaterialTheme.typography.labelMedium,
                        color = TelegramBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    // Mute Notifications Toggle
                    if (onToggleMute != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleMute() }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (chat?.isMuted == true) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = TelegramBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notificaciones",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (chat?.isMuted == true) "Silenciado" else "Activado",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = chat?.isMuted != true,
                                onCheckedChange = { onToggleMute() },
                                colors = SwitchDefaults.colors(checkedThumbColor = TelegramBlue)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }

                    // Disappearing Messages
                    if (onSetDisappearingTimer != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDisappearingOptions = !showDisappearingOptions }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = TelegramBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mensajes Temporales",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Configurar expiración de mensajes",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (showDisappearingOptions) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    0 to "Off",
                                    86400 to "24 Horas",
                                    604800 to "7 Días"
                                ).forEach { (secs, label) ->
                                    Button(
                                        onClick = {
                                            onSetDisappearingTimer(secs)
                                            showDisappearingOptions = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(label, fontSize = 11.sp, color = TelegramBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }

                    // Encryption info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cifrado de Extremo a Extremo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = chat?.encryptionKeyFingerprint ?: "Claves verificadas por protocolo seguro",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Block / Unblock Contact
                    if (onToggleBlock != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleBlock() }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = if (chat?.isBlocked == true) "Desbloquear Contacto" else "Bloquear Contacto",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                    }

                    // Report User
                    if (onReportUser != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onReportUser("Reportado por el usuario")
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Reportar Usuario",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
