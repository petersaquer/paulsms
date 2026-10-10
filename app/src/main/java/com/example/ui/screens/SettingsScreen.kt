package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.User
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    isDarkMode: Boolean,
    isFirebaseSignedIn: Boolean,
    blockedChats: List<Chat> = emptyList(),
    privacyPhone: String = "Mis contactos",
    privacyLastSeen: String = "Todos",
    privacyForwardProtect: Boolean = true,
    privacyPasscodeLock: Boolean = false,
    showNotifications: Boolean = true,
    notificationSound: String = "Predeterminado",
    notificationVibration: Boolean = true,
    onToggleDarkMode: () -> Unit,
    onSignInGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onUnblockChat: (String) -> Unit = {},
    onUpdatePrivacyPhone: (String) -> Unit = {},
    onUpdatePrivacyLastSeen: (String) -> Unit = {},
    onToggleForwardProtect: (Boolean) -> Unit = {},
    onTogglePasscodeLock: (Boolean) -> Unit = {},
    onToggleNotifications: (Boolean) -> Unit = {},
    onUpdateNotificationSound: (String) -> Unit = {},
    onToggleNotificationVibration: (Boolean) -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onUpdateProfile: (String, String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val user = currentUser
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showDataUsageDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Edit profile form state
    var editName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var editBio by remember(user) { mutableStateOf(user?.bio ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user?.displayName ?: user?.name ?: "Paul Usuario",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.size(24.dp).testTag("edit_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar perfil",
                                tint = TelegramBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = user?.username ?: "@paul_user",
                        style = MaterialTheme.typography.bodySmall,
                        color = TelegramBlue
                    )
                    Text(
                        text = if (!user?.email.isNullOrBlank()) user?.email!! else (user?.phoneNumber?.ifEmpty { "Sin teléfono" } ?: "Sin teléfono"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Role Badge
                    val roleLabel = when {
                        user?.isAdmin == true -> "👑 Administrador Principal"
                        user?.isModerator == true -> "🛡️ Moderador"
                        else -> "👤 Miembro de la Plataforma"
                    }
                    val roleColor = when {
                        user?.isAdmin == true -> Color(0xFFFF6F00)
                        user?.isModerator == true -> Color(0xFF7B1FA2)
                        else -> TelegramBlue
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(roleColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(roleLabel, color = roleColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ADMIN PANEL BANNER
            if (user?.isAdmin == true || user?.isModerator == true) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD84315)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onOpenAdminPanel() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Panel de Administración", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Control total de usuarios y roles", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Firebase Cloud Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(if (isFirebaseSignedIn) "Firebase Sincronizado ✅" else "Sincronización en la Nube", fontWeight = FontWeight.Bold)
                    if (!isFirebaseSignedIn) {
                        Button(onClick = onSignInGoogle, modifier = Modifier.fillMaxWidth()) {
                            Text("Iniciar sesión con Google")
                        }
                    } else {
                        TextButton(onClick = onSignOutGoogle) { Text("Cerrar Sesión", color = Color.Red) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsItem(
                title = "Privacidad y seguridad",
                subtitle = "Bloqueo, visibilidad y E2EE",
                icon = Icons.Default.Lock,
                onClick = { showPrivacyDialog = true }
            )

            SettingsItem(
                title = "Usuarios bloqueados",
                subtitle = "${blockedChats.size} usuarios",
                icon = Icons.Default.Block,
                onClick = { showBlockedUsersDialog = true }
            )

            SettingsItem(
                title = "Notificaciones",
                subtitle = if (showNotifications) "Activado" else "Desactivado",
                icon = Icons.Default.Notifications,
                onClick = { showNotificationsDialog = true }
            )

            SettingsItem(
                title = "Datos y almacenamiento",
                subtitle = "Uso de red",
                icon = Icons.Default.DataUsage,
                onClick = { showDataUsageDialog = true }
            )

            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggleDarkMode() }.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Modo nocturno", fontWeight = FontWeight.Medium)
                    Text(if (isDarkMode) "Activado" else "Desactivado", fontSize = 11.sp)
                }
                Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
            }
        }
    }

    if (showPrivacyDialog) {
        PrivacyDialog(
            phoneVisibility = privacyPhone,
            lastSeenVisibility = privacyLastSeen,
            forwardProtection = privacyForwardProtect,
            passcodeLock = privacyPasscodeLock,
            blockedCount = blockedChats.size,
            onOpenBlocked = { showPrivacyDialog = false; showBlockedUsersDialog = true },
            onUpdatePhone = onUpdatePrivacyPhone,
            onUpdateLastSeen = onUpdatePrivacyLastSeen,
            onToggleForward = onToggleForwardProtect,
            onTogglePasscode = onTogglePasscodeLock,
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showBlockedUsersDialog) {
        BlockedUsersDialog(
            blockedChats = blockedChats,
            onUnblock = onUnblockChat,
            onDismiss = { showBlockedUsersDialog = false }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            enabled = showNotifications,
            sound = notificationSound,
            vibration = notificationVibration,
            onToggleEnabled = onToggleNotifications,
            onUpdateSound = onUpdateNotificationSound,
            onToggleVibration = onToggleNotificationVibration,
            onDismiss = { showNotificationsDialog = false }
        )
    }

    if (showDataUsageDialog) {
        AlertDialog(
            onDismissRequest = { showDataUsageDialog = false },
            title = { Text("Datos") },
            text = { Text("Caché y uso de red optimizados.") },
            confirmButton = { TextButton(onClick = { showDataUsageDialog = false }) { Text("OK") } }
        )
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Editar Perfil") },
            text = {
                Column {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Nombre") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Bio") })
                }
            },
            confirmButton = {
                Button(onClick = { onUpdateProfile(editName, editBio); showEditProfileDialog = false }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun NotificationsDialog(
    enabled: Boolean,
    sound: String,
    vibration: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onUpdateSound: (String) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val soundOptions = listOf("Predeterminado", "Campana", "Nota", "WhatsApp", "Ninguno")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración de Notificaciones") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activar notificaciones", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(checked = enabled, onCheckedChange = onToggleEnabled)
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text("Sonido de mensaje", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                soundOptions.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onUpdateSound(opt) }.padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = sound == opt, onClick = { onUpdateSound(opt) }, enabled = enabled)
                        Text(opt, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vibración", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(checked = vibration, onCheckedChange = onToggleVibration, enabled = enabled)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Estas opciones aplican a todos los chats individuales. Puedes personalizar chats específicos tocando en su nombre.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Listo")
            }
        }
    )
}

@Composable
private fun PrivacyDialog(
    phoneVisibility: String,
    lastSeenVisibility: String,
    forwardProtection: Boolean,
    passcodeLock: Boolean,
    blockedCount: Int,
    onOpenBlocked: () -> Unit,
    onUpdatePhone: (String) -> Unit,
    onUpdateLastSeen: (String) -> Unit,
    onToggleForward: (Boolean) -> Unit,
    onTogglePasscode: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("Todos", "Mis contactos", "Nadie")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacidad") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("¿Quién puede ver mi número?", fontWeight = FontWeight.Bold)
                options.forEach { opt ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onUpdatePhone(opt) }) {
                        RadioButton(selected = phoneVisibility == opt, onClick = { onUpdatePhone(opt) })
                        Text(opt)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Bloqueo de aplicación", modifier = Modifier.weight(1f))
                    Switch(checked = passcodeLock, onCheckedChange = onTogglePasscode)
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun BlockedUsersDialog(
    blockedChats: List<Chat>,
    onUnblock: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bloqueados") },
        text = {
            if (blockedChats.isEmpty()) Text("No hay usuarios bloqueados")
            else {
                LazyColumn {
                    items(blockedChats) { chat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(chat.title, modifier = Modifier.weight(1f))
                            Button(onClick = { onUnblock(chat.id) }) { Text("Desbloquear") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun SettingsItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
