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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRoles
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    currentUser: User,
    allUsers: List<User>,
    onUpdateRole: (userId: String, newRole: String) -> Unit,
    onToggleBan: (userId: String, isBanned: Boolean) -> Unit,
    onDeleteUser: (userId: String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") } // TODOS, ADMIN, MODERATOR, USER, BANNED

    // Dialog state for Role Change
    var userForRoleChange by remember { mutableStateOf<User?>(null) }
    var selectedNewRole by remember { mutableStateOf(UserRoles.USER) }

    // Dialog state for Delete User
    var userForDeletion by remember { mutableStateOf<User?>(null) }

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
            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatBadge(
                    title = "Usuarios",
                    count = totalUsers,
                    color = TelegramBlue,
                    modifier = Modifier.weight(1f)
                )
                AdminStatBadge(
                    title = "Admins",
                    count = totalAdmins,
                    color = Color(0xFFFF6F00),
                    modifier = Modifier.weight(1f)
                )
                AdminStatBadge(
                    title = "Mods",
                    count = totalMods,
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.weight(1f)
                )
                AdminStatBadge(
                    title = "Bloqueados",
                    count = totalBanned,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )
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
                    // Role Badge
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
