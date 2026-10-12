package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramDarkBlue

@Composable
fun SettingsDrawer(
    currentUser: User?,
    isDarkMode: Boolean,
    isFirebaseSignedIn: Boolean,
    onToggleDarkMode: () -> Unit,
    onSignInGoogle: () -> Unit,
    onAddContact: () -> Unit,
    onOpenContacts: () -> Unit = {},
    onCreateStatus: () -> Unit = {},
    onOpenSavedMessages: () -> Unit,
    onNewGroup: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdminPanel: () -> Unit = {},
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = currentUser

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with user info & dark mode toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TelegramBlue, TelegramDarkBlue)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Theme Mode Toggle
                        IconButton(
                            onClick = onToggleDarkMode,
                            modifier = Modifier.testTag("drawer_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Cambiar tema",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user?.name ?: "Paul Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )

                    Text(
                        text = user?.username ?: "@paul_user",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )

                    // Cloud status pill
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFirebaseSignedIn) Color(0xFF00C853).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.2f))
                            .clickable { if (!isFirebaseSignedIn) onSignInGoogle() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFirebaseSignedIn) Icons.Default.CloudDone else Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFirebaseSignedIn) "Firebase Cloud: Activo" else "Vincular Google Cloud",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contacts & User Search
            DrawerMenuItem(
                title = "Contactos y Usuarios",
                icon = Icons.Default.Contacts,
                badge = "Búsqueda Firestore",
                onClick = {
                    onCloseDrawer()
                    onOpenContacts()
                }
            )

            // WhatsApp-style Status & Reels
            DrawerMenuItem(
                title = "Crear Estado / Reel",
                icon = Icons.Default.Movie,
                badge = "24h • Privacidad",
                onClick = {
                    onCloseDrawer()
                    onCreateStatus()
                }
            )

            // Add Contact Action
            DrawerMenuItem(
                title = "Añadir por Teléfono / @",
                icon = Icons.Default.PersonAdd,
                badge = "E2EE / Cloud",
                onClick = {
                    onCloseDrawer()
                    onAddContact()
                }
            )

            // Navigation Menu Items
            DrawerMenuItem(
                title = "Nuevo Grupo",
                icon = Icons.Default.Group,
                onClick = {
                    onCloseDrawer()
                    onNewGroup()
                }
            )

            DrawerMenuItem(
                title = "Mensajes Guardados",
                icon = Icons.Default.Bookmark,
                onClick = {
                    onCloseDrawer()
                    onOpenSavedMessages()
                }
            )

            DrawerMenuItem(
                title = "Llamadas",
                icon = Icons.Default.Call,
                onClick = {
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                title = "Ajustes",
                icon = Icons.Default.Settings,
                onClick = {
                    onCloseDrawer()
                    onOpenSettings()
                }
            )

            if (user?.isAdmin == true || user?.isModerator == true) {
                DrawerMenuItem(
                    title = "Panel de Administración",
                    icon = Icons.Default.Security,
                    badge = if (user.isAdmin) "Admin" else "Mod",
                    onClick = {
                        onCloseDrawer()
                        onOpenAdminPanel()
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            DrawerMenuItem(
                title = "Seguridad y Cifrado E2EE",
                icon = Icons.Default.Lock,
                onClick = {
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                title = "Acerca de Paul",
                icon = Icons.Default.Info,
                onClick = {
                    onCloseDrawer()
                }
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    title: String,
    icon: ImageVector,
    badge: String? = null,
    onClick: () -> Unit
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
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(TelegramBlue.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    color = TelegramBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
