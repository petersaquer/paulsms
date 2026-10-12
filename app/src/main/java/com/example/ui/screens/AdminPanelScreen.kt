package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdateInfo
import com.example.data.model.User
import com.example.data.model.UserRoles
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    currentUser: User,
    allUsers: List<User>,
    latestAppUpdate: AppUpdateInfo? = null,
    onUpdateRole: (userId: String, newRole: String) -> Unit,
    onToggleBan: (userId: String, isBanned: Boolean) -> Unit,
    onDeleteUser: (userId: String) -> Unit,
    onPublishUpdate: (versionName: String, versionCode: Int, apkUrl: String, changelog: String, isMandatory: Boolean) -> Unit = { _, _, _, _, _ -> },
    onBack: () -> Unit
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Gestión de Usuarios, 1: Publicar Actualización
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") }

    // Dialog state for Role Change
    var userForRoleChange by remember { mutableStateOf<User?>(null) }
    var selectedNewRole by remember { mutableStateOf(UserRoles.USER) }

    // Dialog state for Delete User
    var userForDeletion by remember { mutableStateOf<User?>(null) }

    // Publish update form state
    var newVersionName by remember { mutableStateOf("1.1.0") }
    var newVersionCode by remember { mutableStateOf("2") }
    var newApkUrl by remember { mutableStateOf("") }
    var newChangelog by remember { mutableStateOf("• Nuevas funciones de mensajería fluida en tiempo real\n• Icono renovado P con un pajarito\n• Panel de administración avanzado con control de versiones") }
    var isMandatoryUpdate by remember { mutableStateOf(false) }

    val filteredUsers = remember(allUsers, searchQuery, selectedFilter) {
        allUsers.filter { u ->
            val matchesQuery = searchQuery.isBlank() ||
                u.username.contains(searchQuery, ignoreCase = true) ||
                u.displayName.contains(searchQuery, ignoreCase = true) ||
                u.email.contains(searchQuery, ignoreCase = true) ||
                u.phoneNumber.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "ADMIN" -> u.isAdmin
                "MODERATOR" -> u.role.equals(UserRoles.MODERATOR, ignoreCase = true)
                "USER" -> u.role.equals(UserRoles.USER, ignoreCase = true)
                "BANNED" -> u.isBanned
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    val totalUsers = allUsers.size
    val totalAdmins = allUsers.count { it.isAdmin }
    val totalMods = allUsers.count { it.role.equals(UserRoles.MODERATOR, ignoreCase = true) }
    val totalBanned = allUsers.count { it.isBanned }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.example.ui.components.PaulAppLogo(size = 22.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("👑 Panel de Administración", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Text(
                            text = if (currentUser.isAdmin) "Sesión: Administrador General" else "Sesión: Moderador",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD84315),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Section Tabs
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    text = { Text("Usuarios ($totalUsers)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    text = { Text("Publicar APK", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedMainTab == 0) {
                // USER MANAGEMENT TAB
                // Stats Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatBadge(title = "Usuarios", count = totalUsers, color = TelegramBlue, modifier = Modifier.weight(1f))
                    AdminStatBadge(title = "Admins", count = totalAdmins, color = Color(0xFFFF6F00), modifier = Modifier.weight(1f))
                    AdminStatBadge(title = "Mods", count = totalMods, color = Color(0xFF7B1FA2), modifier = Modifier.weight(1f))
                    AdminStatBadge(title = "Bloqueados", count = totalBanned, color = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar usuario por @usuario, nombre, email...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("admin_user_search"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "TODOS" to "Todos ($totalUsers)",
                        "ADMIN" to "Admins ($totalAdmins)",
                        "MODERATOR" to "Mods ($totalMods)",
                        "BANNED" to "Bloqueados ($totalBanned)"
                    ).forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD84315),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Users List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (filteredUsers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron usuarios con ese filtro.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredUsers, key = { it.userId }) { user ->
                            AdminUserCard(
                                user = user,
                                isSelf = user.userId == currentUser.userId,
                                canManageRoles = currentUser.isAdmin,
                                canBan = currentUser.isModerator,
                                canDelete = currentUser.isAdmin,
                                onChangeRole = {
                                    userForRoleChange = user
                                    selectedNewRole = user.role
                                },
                                onToggleBan = {
                                    onToggleBan(user.userId, !user.isBanned)
                                },
                                onDelete = {
                                    userForDeletion = user
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } else {
                // PUBLISH APP UPDATE (APK) TAB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFFE65100))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gestor de Actualizaciones OTA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Al publicar una nueva compilación, todos los dispositivos recibirán la notificación de actualización en tiempo real basada en la fecha y número de versión.",
                                fontSize = 12.sp,
                                color = Color(0xFFBF360C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (latestAppUpdate != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Versión actualmente publicada:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TelegramBlue)
                                Text("v${latestAppUpdate.versionName} (Build ${latestAppUpdate.versionCode})", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                if (latestAppUpdate.releaseDateFormatted.isNotBlank()) {
                                    Text("Fecha de lanzamiento: ${latestAppUpdate.releaseDateFormatted}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notas: ${latestAppUpdate.changelog}", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text("Publicar nueva versión:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newVersionName,
                        onValueChange = { newVersionName = it },
                        label = { Text("Nombre de Versión (ej: 1.1.0)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newVersionCode,
                        onValueChange = { newVersionCode = it.filter { char -> char.isDigit() } },
                        label = { Text("Código de Compilación / Build (ej: 2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newApkUrl,
                        onValueChange = { newApkUrl = it },
                        label = { Text("URL de Descarga del APK (Direct Link o Web)") },
                        placeholder = { Text("https://servidor.com/Paul-Messenger.apk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newChangelog,
                        onValueChange = { newChangelog = it },
                        label = { Text("Notas de la versión / Changelog") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Actualización Obligatoria", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Los usuarios deberán actualizar para continuar usando la app", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isMandatoryUpdate,
                            onCheckedChange = { isMandatoryUpdate = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFD84315))
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val code = newVersionCode.toIntOrNull() ?: 2
                            onPublishUpdate(newVersionName, code, newApkUrl, newChangelog, isMandatoryUpdate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar Actualización Ahora", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Role Change Dialog
    if (userForRoleChange != null) {
        val target = userForRoleChange!!
        AlertDialog(
            onDismissRequest = { userForRoleChange = null },
            title = {
                Text("Cambiar Rol de @${target.username.removePrefix("@")}")
            },
            text = {
                Column {
                    Text(
                        text = "Selecciona el nuevo nivel de acceso para este usuario:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        UserRoles.ADMIN to "👑 Administrador (Acceso Total y Panel)",
                        UserRoles.MODERATOR to "🛡️ Moderador (Silenciar, bloquear y expulsar)",
                        UserRoles.USER to "👤 Miembro (Usuario estándar)"
                    ).forEach { (roleValue, roleLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNewRole = roleValue }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedNewRole == roleValue,
                                onClick = { selectedNewRole = roleValue }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(roleLabel, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateRole(target.userId, selectedNewRole)
                        userForRoleChange = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315))
                ) {
                    Text("Guardar Rol")
                }
            },
            dismissButton = {
                TextButton(onClick = { userForRoleChange = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete User Confirmation Dialog
    if (userForDeletion != null) {
        val target = userForDeletion!!
        AlertDialog(
            onDismissRequest = { userForDeletion = null },
            title = {
                Text("¿Eliminar usuario definitivamente?")
            },
            text = {
                Text("Estás a punto de eliminar a @${target.username.removePrefix("@")} (${target.displayName}) de la plataforma. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(target.userId)
                        userForDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar Cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { userForDeletion = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AdminStatBadge(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminUserCard(
    user: User,
    isSelf: Boolean,
    canManageRoles: Boolean,
    canBan: Boolean,
    canDelete: Boolean,
    onChangeRole: () -> Unit,
    onToggleBan: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isBanned) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(user.avatarGradientStart.toInt()),
                                Color(user.avatarGradientEnd.toInt())
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSelf) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(Tú)",
                            color = TelegramBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "${user.username} • ${if (user.email.isNotBlank()) user.email else user.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (roleText, roleColor) = when {
                        user.isAdmin -> "👑 Administrador" to Color(0xFFFF6F00)
                        user.isModerator -> "🛡️ Moderador" to Color(0xFF7B1FA2)
                        else -> "👤 Miembro" to TelegramBlue
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(roleColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(roleText, color = roleColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (user.isBanned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFD32F2F).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🚫 Suspendido", color = Color(0xFFD32F2F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Actions Menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones de usuario",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (canManageRoles && !isSelf) {
                        DropdownMenuItem(
                            text = { Text("Cambiar Rol...") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onChangeRole()
                            }
                        )
                    }

                    if (canBan && !isSelf) {
                        DropdownMenuItem(
                            text = { Text(if (user.isBanned) "Desbloquear Usuario" else "Bloquear / Suspender") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (user.isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                                    contentDescription = null,
                                    tint = if (user.isBanned) Color(0xFF00C853) else Color(0xFFD32F2F)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleBan()
                            }
                        )
                    }

                    if (canDelete && !isSelf) {
                        DropdownMenuItem(
                            text = { Text("Eliminar Cuenta", color = Color(0xFFD32F2F)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
