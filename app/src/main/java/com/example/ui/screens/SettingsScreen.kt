package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Chat
import com.example.data.model.User
import com.example.ui.components.AboutPaulDialog
import com.example.ui.components.DataStorageDialog
import com.example.ui.components.E2EESecurityDialog
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
    onOpenProfile: () -> Unit = {},
    onUpdateProfile: (String, String) -> Unit = { _, _ -> },
    onCreateCloudBackup: () -> Unit = {},
    onRestoreCloudBackup: () -> Unit = {},
    onBack: () -> Unit
) {
    val user = currentUser
    var showDataUsageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

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
            // Profile Card Header (Clickable to open profile)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onOpenProfile() }
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
                    if (!user?.profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user?.profilePictureUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                            onClick = onOpenProfile,
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
                        text = if (!user?.email.isNullOrBlank()) user.email else "Sin correo registrado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            SettingsHeader(title = "AJUSTES DE LA APLICACIÓN")

            SettingsItemWithToggle(
                title = "Modo Oscuro",
                subtitle = "Tema nocturno relajante",
                icon = Icons.Default.DarkMode,
                checked = isDarkMode,
                onCheckedChange = { onToggleDarkMode() }
            )

            SettingsItemWithToggle(
                title = "Notificaciones",
                subtitle = "Alertas de nuevos mensajes",
                icon = Icons.Default.Notifications,
                checked = showNotifications,
                onCheckedChange = onToggleNotifications
            )

            SettingsItem(
                title = "Privacidad y Seguridad",
                subtitle = "Controles de perfil y mensajes",
                icon = Icons.Default.Lock,
                onClick = { showSecurityDialog = true }
            )

            SettingsItem(
                title = "Datos y Almacenamiento",
                subtitle = "Uso de red y archivos en caché",
                icon = Icons.Default.DataUsage,
                onClick = { showDataUsageDialog = true }
            )

            if (user?.isAdmin == true || user?.isModerator == true) {
                SettingsItem(
                    title = "Panel de Administración",
                    subtitle = "Gestión de roles y usuarios",
                    icon = Icons.Default.Security,
                    onClick = onOpenAdminPanel
                )
            }

            SettingsItem(
                title = "Copia de Seguridad en la Nube",
                subtitle = "Guardar chats y contactos",
                icon = Icons.Default.Security,
                onClick = onCreateCloudBackup
            )

            SettingsItem(
                title = "Acerca de Paul",
                subtitle = "Versión 1.0.0 Oficial",
                icon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign out button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSignOutGoogle() }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAboutDialog) AboutPaulDialog(onDismiss = { showAboutDialog = false })
    if (showSecurityDialog) E2EESecurityDialog(onDismiss = { showSecurityDialog = false })
    if (showDataUsageDialog) DataStorageDialog(onDismiss = { showDataUsageDialog = false })
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TelegramBlue,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
fun SettingsItemWithToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
