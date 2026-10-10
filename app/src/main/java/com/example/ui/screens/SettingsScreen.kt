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
    onToggleDarkMode: () -> Unit,
    onSignInGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onUnblockChat: (String) -> Unit = {},
    onUpdatePrivacyPhone: (String) -> Unit = {},
    onUpdatePrivacyLastSeen: (String) -> Unit = {},
    onToggleForwardProtect: (Boolean) -> Unit = {},
    onTogglePasscodeLock: (Boolean) -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onBack: () -> Unit
) {
    val user = currentUser
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showDataUsageDialog by remember { mutableStateOf(false) }

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
                            text = user?.name ?: "Paul Usuario",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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

            // ADMIN PANEL BANNER if user is admin or moderator
            if (user?.isAdmin == true || user?.isModerator == true) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD84315)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onOpenAdminPanel() }
                        .testTag("settings_open_admin_panel")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👑 Panel de Administración",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Control total: lista de usuarios, roles, bloquear y eliminar cuentas",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                        Text("Abrir ›", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Firebase Cloud Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFirebaseSignedIn) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFirebaseSignedIn) Icons.Default.CloudDone else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isFirebaseSignedIn) Color(0xFF2E7D32) else TelegramBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isFirebaseSignedIn) "Firebase Cloud Sincronizado" else "Iniciar Sesión en Firebase Cloud",
                                fontWeight = FontWeight.Bold,
                                color = if (isFirebaseSignedIn) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isFirebaseSignedIn)
                                    "Base de datos Firestore activa. Mensajes y contactos sincronizados."
                                else
                                    "Inicia sesión con Google para sincronizar tus contactos y chats en la nube.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isFirebaseSignedIn) {
                        OutlinedButton(
                            onClick = onSignOutGoogle,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cerrar Sesión")
                        }
                    } else {
                        Button(
                            onClick = onSignInGoogle,
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_signin_button")
                        ) {
                            Text("Iniciar sesión con Google")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Category: AJUSTES
            Text(
                text = "AJUSTES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TelegramBlue,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )

            SettingsItem(
                title = "Privacidad y seguridad",
                subtitle = "Bloqueo, autodestrucción, E2EE y visibilidad",
                icon = Icons.Default.Lock,
                onClick = { showPrivacyDialog = true }
            )

            SettingsItem(
                title = "Usuarios bloqueados",
                subtitle = if (blockedChats.isEmpty()) "Ninguno" else "${blockedChats.size} usuario(s) bloqueado(s)",
                icon = Icons.Default.Block,
                onClick = { showBlockedUsersDialog = true }
            )

            SettingsItem(
                title = "Notificaciones y sonidos",
                subtitle = "Activado para todos los chats",
                icon = Icons.Default.Notifications,
                onClick = { showNotificationsDialog = true }
            )

            SettingsItem(
                title = "Datos y almacenamiento",
                subtitle = "Uso de red, caché y base de datos local",
                icon = Icons.Default.DataUsage,
                onClick = { showDataUsageDialog = true }
            )

            SettingsItem(
                title = "Ajustes de chats",
                subtitle = "Burbujas tipo Telegram, stickers y temas",
                icon = Icons.Default.Chat
            )

            // Dark Mode Switch Item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDarkMode() }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Modo nocturno",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modo nocturno",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) "Tema oscuro Telegram activado" else "Tema claro clásico",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TelegramBlue
                    )
                )
            }

            SettingsItem(
                title = "Idioma",
                subtitle = "Español",
                icon = Icons.Default.Language
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // App details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Paul Messenger para Android",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Firebase Cloud & Cifrado Extremo a Extremo (E2EE)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TelegramBlue
                )
            }
        }
    }

    // Privacy & Security Dialog
    if (showPrivacyDialog) {
        PrivacyDialog(
            phoneVisibility = privacyPhone,
            lastSeenVisibility = privacyLastSeen,
            forwardProtection = privacyForwardProtect,
            passcodeLock = privacyPasscodeLock,
            blockedCount = blockedChats.size,
            onOpenBlocked = {
                showPrivacyDialog = false
                showBlockedUsersDialog = true
            },
            onUpdatePhone = onUpdatePrivacyPhone,
            onUpdateLastSeen = onUpdatePrivacyLastSeen,
            onToggleForward = onToggleForwardProtect,
            onTogglePasscode = onTogglePasscodeLock,
            onDismiss = { showPrivacyDialog = false }
        )
    }

    // Blocked Users Dialog
    if (showBlockedUsersDialog) {
        BlockedUsersDialog(
            blockedChats = blockedChats,
            onUnblock = onUnblockChat,
            onDismiss = { showBlockedUsersDialog = false }
        )
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Notificaciones y sonidos", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("• Notificaciones de chats privados: Activadas\n• Notificaciones de grupos: Activadas\n• Sonido de mensaje: Tono Telegram\n• Vibración: Predeterminada")
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Data Usage Dialog
    if (showDataUsageDialog) {
        AlertDialog(
            onDismissRequest = { showDataUsageDialog = false },
            title = { Text("Datos y almacenamiento", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("• Uso de red móvil: Optimizado\n• Descarga automática: Solo con Wi-Fi\n• Cifrado local E2EE: Almacenado en sandbox seguro\n• Base de datos Firestore: Cache local activa")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataUsageDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TelegramBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Privacidad y Seguridad", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // E2EE Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Cifrado Extremo a Extremo Activo",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Los chats fuera de Firebase están blindados en este dispositivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Blocked contacts shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenBlocked() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Usuarios bloqueados", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            text = if (blockedCount == 0) "Ningún usuario bloqueado" else "$blockedCount usuario(s)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Text("Ver", color = TelegramBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Passcode lock switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bloqueo con PIN o Biometría", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Bloquea Paul al salir de la aplicación", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = passcodeLock,
                        onCheckedChange = onTogglePasscode,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TelegramBlue)
                    )
                }

                // Forward protection switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Proteger autoría al reenviar", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Oculta tu enlace de perfil al reenviar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = forwardProtection,
                        onCheckedChange = onToggleForward,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TelegramBlue)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Phone visibility
                Text("¿Quién puede ver mi número de teléfono?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                options.forEach { opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdatePhone(opt) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = phoneVisibility == opt,
                            onClick = { onUpdatePhone(opt) },
                            colors = RadioButtonDefaults.colors(selectedColor = TelegramBlue)
                        )
                        Text(text = opt, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Last seen visibility
                Text("¿Quién puede ver mi última vez y en línea?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                options.forEach { opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateLastSeen(opt) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = lastSeenVisibility == opt,
                            onClick = { onUpdateLastSeen(opt) },
                            colors = RadioButtonDefaults.colors(selectedColor = TelegramBlue)
                        )
                        Text(text = opt, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
            ) {
                Text("Guardar")
            }
        }
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usuarios Bloqueados", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (blockedChats.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tienes ningún usuario bloqueado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(blockedChats, key = { it.id }) { chat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chat.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = chat.username.ifEmpty { chat.contactPhone ?: "Sin usuario" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onUnblock(chat.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Desbloquear", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
