package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntry
import com.example.data.model.Chat
import com.example.data.model.GroupAdminPermissions
import com.example.data.model.GroupConfig
import com.example.data.model.GroupGlobalPermissions
import com.example.data.model.GroupMember
import com.example.data.model.GroupRole
import com.example.data.model.JoinRequest
import com.example.data.model.Topic
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    chat: Chat,
    currentUserId: String,
    groupConfig: GroupConfig,
    onUpdateGroupConfig: (GroupConfig) -> Unit,
    onBanUser: (targetUserId: String, massDelete: Boolean) -> Unit,
    onUnbanUser: (targetUserId: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Members/RBAC, 1: Global Permissions, 2: Access & Links, 3: Audit & Banned, 4: Topics/Subtemas

    val currentUserMember = groupConfig.members.find { it.userId == currentUserId }
    val isOwner = groupConfig.ownerId == currentUserId || currentUserMember?.role == GroupRole.OWNER
    val isAdmin = isOwner || currentUserMember?.role == GroupRole.ADMIN

    var showPromoteDialog by remember { mutableStateOf<GroupMember?>(null) }
    var showMemberPermissionsDialog by remember { mutableStateOf<GroupMember?>(null) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showTransferOwnershipDialog by remember { mutableStateOf<GroupMember?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chat.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Ajustes de Grupo & Administración RBAC",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_group_mgmt")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Group Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF536DFE)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.title.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chat.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = groupConfig.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TelegramBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (groupConfig.accessConfig.isPublic) "Público (${groupConfig.accessConfig.publicAlias})" else "Privado",
                                    fontSize = 11.sp,
                                    color = TelegramBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${groupConfig.members.size} miembros",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TelegramBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TelegramBlue,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Miembros (${groupConfig.members.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_group_members")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Permisos", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_group_permissions")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Enlaces / Acceso", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_group_access")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Auditoría (${groupConfig.auditLog.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_group_audit")
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Subtemas (${groupConfig.topics.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_group_topics")
                )
            }

            // TAB CONTENT
            when (selectedTab) {
                0 -> MembersTab(
                    members = groupConfig.members,
                    currentUserId = currentUserId,
                    isAdmin = isAdmin,
                    isOwner = isOwner,
                    onPromote = { showPromoteDialog = it },
                    onManagePermissions = { showMemberPermissionsDialog = it },
                    onBan = { m -> onBanUser(m.userId, true) },
                    onTransferOwnership = { showTransferOwnershipDialog = it }
                )
                1 -> GlobalPermissionsTab(
                    config = groupConfig,
                    isAdmin = isAdmin,
                    onUpdateConfig = onUpdateGroupConfig
                )
                2 -> AccessAndLinksTab(
                    config = groupConfig,
                    isAdmin = isAdmin,
                    onUpdateConfig = onUpdateGroupConfig
                )
                3 -> AuditAndBannedTab(
                    auditLog = groupConfig.auditLog,
                    bannedIds = groupConfig.bannedUserIds,
                    onUnban = onUnbanUser
                )
                4 -> TopicsTab(
                    topics = groupConfig.topics,
                    isAdmin = isAdmin,
                    onAddTopic = { showAddTopicDialog = true }
                )
            }
        }
    }

    // PROMOTE ADMIN DIALOG
    showPromoteDialog?.let { member ->
        PromoteAdminDialog(
            member = member,
            onDismiss = { showPromoteDialog = null },
            onConfirm = { customBadge, perms ->
                val updatedMembers = groupConfig.members.map {
                    if (it.userId == member.userId) {
                        it.copy(
                            role = GroupRole.ADMIN,
                            customTitle = customBadge,
                            adminPermissions = perms
                        )
                    } else it
                }
                val newAudit = groupConfig.auditLog + AuditLogEntry(
                    id = "log_" + UUID.randomUUID().toString().take(6),
                    adminUserId = currentUserId,
                    adminName = currentUserMember?.displayName ?: "Admin",
                    actionType = "PROMOTE_ADMIN",
                    description = "Promovió a ${member.displayName} como Administrador ($customBadge)",
                    targetUserId = member.userId
                )
                onUpdateGroupConfig(groupConfig.copy(members = updatedMembers, auditLog = newAudit))
                showPromoteDialog = null
                Toast.makeText(context, "${member.displayName} promovido a Administrador", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // MEMBER INDIVIDUAL RESTRICTIONS DIALOG
    showMemberPermissionsDialog?.let { member ->
        MemberRestrictionsDialog(
            member = member,
            onDismiss = { showMemberPermissionsDialog = null },
            onSave = { updatedMember ->
                val updatedMembers = groupConfig.members.map {
                    if (it.userId == member.userId) updatedMember else it
                }
                val newAudit = groupConfig.auditLog + AuditLogEntry(
                    id = "log_" + UUID.randomUUID().toString().take(6),
                    adminUserId = currentUserId,
                    adminName = currentUserMember?.displayName ?: "Admin",
                    actionType = "EDIT_RESTRICTIONS",
                    description = "Modificó restricciones para ${member.displayName}",
                    targetUserId = member.userId
                )
                onUpdateGroupConfig(groupConfig.copy(members = updatedMembers, auditLog = newAudit))
                showMemberPermissionsDialog = null
                Toast.makeText(context, "Restricciones guardadas", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ADD TOPIC DIALOG
    if (showAddTopicDialog) {
        AddTopicDialog(
            onDismiss = { showAddTopicDialog = false },
            onConfirm = { title, emoji ->
                val newTopic = Topic(
                    id = "topic_" + UUID.randomUUID().toString().take(6),
                    title = title,
                    iconEmoji = emoji,
                    createdBy = currentUserId
                )
                val newTopics = groupConfig.topics + newTopic
                onUpdateGroupConfig(groupConfig.copy(topics = newTopics))
                showAddTopicDialog = false
                Toast.makeText(context, "Subtema creado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // TRANSFER OWNERSHIP DIALOG
    showTransferOwnershipDialog?.let { target ->
        AlertDialog(
            onDismissRequest = { showTransferOwnershipDialog = null },
            title = { Text("Transferir Propiedad del Grupo", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de transferir la propiedad del grupo a ${target.displayName}? Esta acción es IRREVERSIBLE y perderás el control absoluto del grupo.") },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedMembers = groupConfig.members.map {
                            when (it.userId) {
                                currentUserId -> it.copy(role = GroupRole.ADMIN, customTitle = "Ex-Propietario")
                                target.userId -> it.copy(role = GroupRole.OWNER, customTitle = "Propietario")
                                else -> it
                            }
                        }
                        onUpdateGroupConfig(groupConfig.copy(ownerId = target.userId, members = updatedMembers))
                        showTransferOwnershipDialog = null
                        Toast.makeText(context, "Propiedad transferida a ${target.displayName}", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Transferir Propiedad")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferOwnershipDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

// MEMBERS TAB
@Composable
private fun MembersTab(
    members: List<GroupMember>,
    currentUserId: String,
    isAdmin: Boolean,
    isOwner: Boolean,
    onPromote: (GroupMember) -> Unit,
    onManagePermissions: (GroupMember) -> Unit,
    onBan: (GroupMember) -> Unit,
    onTransferOwnership: (GroupMember) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(members, key = { it.userId }) { member ->
            var expandedMenu by remember { mutableStateOf(false) }
            val isTargetOwner = member.role == GroupRole.OWNER
            val isSelf = member.userId == currentUserId

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isTargetOwner) Color(0xFFFF9800) else if (member.role == GroupRole.ADMIN) TelegramBlue else Color(0xFF78909C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.displayName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (isSelf) {
                                Text(" (Tú)", fontSize = 12.sp, color = TelegramBlue)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.username,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (member.role) {
                                            GroupRole.OWNER -> Color(0xFFFFF3E0)
                                            GroupRole.ADMIN -> TelegramBlue.copy(alpha = 0.15f)
                                            GroupRole.RESTRICTED -> Color(0xFFFFEBEE)
                                            GroupRole.MEMBER -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = member.roleLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (member.role) {
                                        GroupRole.OWNER -> Color(0xFFE65100)
                                        GroupRole.ADMIN -> TelegramBlue
                                        GroupRole.RESTRICTED -> Color(0xFFC62828)
                                        GroupRole.MEMBER -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    if (isAdmin && !isSelf && !isTargetOwner) {
                        Box {
                            IconButton(onClick = { expandedMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones de miembro")
                            }
                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Promover a Admin") },
                                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = TelegramBlue) },
                                    onClick = { expandedMenu = false; onPromote(member) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Restricciones y Silencio") },
                                    leadingIcon = { Icon(Icons.Default.VolumeOff, contentDescription = null) },
                                    onClick = { expandedMenu = false; onManagePermissions(member) }
                                )
                                if (isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("Transferir Propiedad") },
                                        leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = Color(0xFFFF9800)) },
                                        onClick = { expandedMenu = false; onTransferOwnership(member) }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Banear / Expulsar", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { expandedMenu = false; onBan(member) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// GLOBAL PERMISSIONS TAB (Chat público / Modo anuncio / Modo lento)
@Composable
private fun GlobalPermissionsTab(
    config: GroupConfig,
    isAdmin: Boolean,
    onUpdateConfig: (GroupConfig) -> Unit
) {
    val perms = config.globalPermissions

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Permisos Globales de los Miembros", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TelegramBlue)
            Text("Desactiva opciones para convertir el grupo en Modo Anuncio o restringir ruido.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            PermissionToggleItem(
                title = "Enviar mensajes de texto",
                subtitle = "Permite a miembros comunes enviar mensajes normales",
                checked = perms.allowText,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(globalPermissions = perms.copy(allowText = it))) }
            )
        }

        item {
            PermissionToggleItem(
                title = "Enviar contenido multimedia",
                subtitle = "Fotos, videos, notas de voz y documentos",
                checked = perms.allowMedia,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(globalPermissions = perms.copy(allowMedia = it))) }
            )
        }

        item {
            PermissionToggleItem(
                title = "Enviar Stickers & GIFs",
                subtitle = "Efectos animados y reacciones gráficas",
                checked = perms.allowStickersGifs,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(globalPermissions = perms.copy(allowStickersGifs = it))) }
            )
        }

        item {
            PermissionToggleItem(
                title = "Previsualización de Enlaces",
                subtitle = "Vista previa automática de URLs (evita phishing)",
                checked = perms.allowLinkPreviews,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(globalPermissions = perms.copy(allowLinkPreviews = it))) }
            )
        }

        item {
            PermissionToggleItem(
                title = "Crear Encuestas",
                subtitle = "Permite a miembros iniciar votaciones",
                checked = perms.allowPolls,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(globalPermissions = perms.copy(allowPolls = it))) }
            )
        }

        item {
            PermissionToggleItem(
                title = "Permitir menciones masivas (@todos, @admin)",
                subtitle = "Notifica a todo el grupo a la vez",
                checked = perms.allowMassMentions,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(globalPermissions = perms.copy(allowMassMentions = it))) }
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = TelegramBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Modo Lento (Slow Mode)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Tiempo de espera obligatorio entre mensajes por usuario", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0, 5, 10, 30, 60, 300).forEach { sec ->
                    val isSelected = perms.slowModeSeconds == sec
                    OutlinedButton(
                        onClick = { if (isAdmin) onUpdateConfig(config.copy(globalPermissions = perms.copy(slowModeSeconds = sec))) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) TelegramBlue else Color.Transparent,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (sec == 0) "Off" else if (sec < 60) "${sec}s" else "${sec / 60}m", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ACCESS & LINKS TAB
@Composable
private fun AccessAndLinksTab(
    config: GroupConfig,
    isAdmin: Boolean,
    onUpdateConfig: (GroupConfig) -> Unit
) {
    val context = LocalContext.current
    val access = config.accessConfig

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Privacidad y Tipo de Grupo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TelegramBlue)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Grupo Público", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Indexable en la búsqueda global de Paul Messenger por @alias", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = access.isPublic,
                            enabled = isAdmin,
                            onCheckedChange = { isPub ->
                                onUpdateConfig(config.copy(accessConfig = access.copy(isPublic = isPub)))
                            }
                        )
                    }

                    if (access.isPublic) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = access.publicAlias,
                            onValueChange = { newAlias ->
                                onUpdateConfig(config.copy(accessConfig = access.copy(publicAlias = newAlias)))
                            },
                            label = { Text("@Alias público del grupo") },
                            placeholder = { Text("ej: @comunidad_android") },
                            singleLine = true,
                            enabled = isAdmin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            Text("Enlace de Invitación Avanzado", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TelegramBlue)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = access.inviteLink, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TelegramBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Paul Invite Link", access.inviteLink)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Enlace", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (isAdmin) {
                                    val newLink = "https://paul.chat/join/group_" + UUID.randomUUID().toString().take(8)
                                    onUpdateConfig(config.copy(accessConfig = access.copy(inviteLink = newLink)))
                                    Toast.makeText(context, "Enlace revocado y regenerado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Regenerar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            PermissionToggleItem(
                title = "Aprobación Previa de Ingreso (Cola de Espera)",
                subtitle = "Los usuarios deben ser aprobados por un admin antes de entrar",
                checked = access.requireAdminApproval,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(accessConfig = access.copy(requireAdminApproval = it))) }
            )
        }

        item {
            PermissionToggleItem(
                title = "Historial visible para nuevos miembros",
                subtitle = "Los nuevos participantes ven mensajes anteriores a su ingreso",
                checked = access.newMemberHistoryVisible,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(accessConfig = access.copy(newMemberHistoryVisible = it))) }
            )
        }

        item {
            PermissionToggleItem(
                title = "Protección de Contenido (DRM)",
                subtitle = "Bloquea reenvío de mensajes, copiado de texto y descargas",
                checked = access.protectedContent,
                enabled = isAdmin,
                onCheckedChange = { onUpdateConfig(config.copy(accessConfig = access.copy(protectedContent = it))) }
            )
        }
    }
}

// AUDIT & BANNED TAB
@Composable
private fun AuditAndBannedTab(
    auditLog: List<AuditLogEntry>,
    bannedIds: List<String>,
    onUnban: (String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Audit Log, 1: Blacklist

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTab) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Audit Log (${auditLog.size})") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Blacklist (${bannedIds.size})") })
        }

        if (subTab == 0) {
            if (auditLog.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No hay acciones de administración registradas aún.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(auditLog.reversed()) { log ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(10.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(log.actionType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TelegramBlue)
                                    Text(SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(log.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(log.description, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        } else {
            if (bannedIds.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No hay usuarios bloqueados en la lista negra.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bannedIds) { id ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ID: $id", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Button(onClick = { onUnban(id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                                    Text("Desbanear", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TOPICS TAB
@Composable
private fun TopicsTab(
    topics: List<Topic>,
    isAdmin: Boolean,
    onAddTopic: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Subtemas & Hilos de Foro", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TelegramBlue)
            if (isAdmin) {
                Button(onClick = onAddTopic, colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Subtema")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (topics.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Forum, contentDescription = null, tint = TelegramBlue.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sin subtemas aún", fontWeight = FontWeight.Bold)
                    Text("Crea temas independientes para organizar las conversaciones del grupo.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topics) { topic ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(topic.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}

// PROMOTE ADMIN DIALOG WITH GRANULAR PERMISSIONS
@Composable
private fun PromoteAdminDialog(
    member: GroupMember,
    onDismiss: () -> Unit,
    onConfirm: (customTitle: String, permissions: GroupAdminPermissions) -> Unit
) {
    var customBadge by remember { mutableStateOf(member.customTitle.ifBlank { "Administrador" }) }
    var pEditInfo by remember { mutableStateOf(member.adminPermissions.canEditInfo) }
    var pDeleteMsg by remember { mutableStateOf(member.adminPermissions.canDeleteMessages) }
    var pBanUsers by remember { mutableStateOf(member.adminPermissions.canBanUsers) }
    var pAddAdmins by remember { mutableStateOf(member.adminPermissions.canAddAdmins) }
    var pPinMsgs by remember { mutableStateOf(member.adminPermissions.canPinMessages) }
    var pManageLinks by remember { mutableStateOf(member.adminPermissions.canManageLinks) }
    var pApprove by remember { mutableStateOf(member.adminPermissions.canApproveMembers) }
    var pAnon by remember { mutableStateOf(member.adminPermissions.canPostAnonymously) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Promover a ${member.displayName} a Admin", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = customBadge,
                    onValueChange = { customBadge = it },
                    label = { Text("Título de administrador personalizado") },
                    placeholder = { Text("Ej: Moderador, Soporte, Creador") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Permisos del Administrador:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pEditInfo, onCheckedChange = { pEditInfo = it })
                        Text("Modificar información del grupo", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pDeleteMsg, onCheckedChange = { pDeleteMsg = it })
                        Text("Eliminar mensajes de otros usuarios", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pBanUsers, onCheckedChange = { pBanUsers = it })
                        Text("Banear / expulsar miembros", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pPinMsgs, onCheckedChange = { pPinMsgs = it })
                        Text("Fijar / desfijar mensajes", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pManageLinks, onCheckedChange = { pManageLinks = it })
                        Text("Gestionar enlaces de invitación", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pAddAdmins, onCheckedChange = { pAddAdmins = it })
                        Text("Añadir nuevos administradores", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pAnon, onCheckedChange = { pAnon = it })
                        Text("Publicar en modo anónimo (nombre del grupo)", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val perms = GroupAdminPermissions(
                        canEditInfo = pEditInfo,
                        canDeleteMessages = pDeleteMsg,
                        canBanUsers = pBanUsers,
                        canAddAdmins = pAddAdmins,
                        canPinMessages = pPinMsgs,
                        canManageLinks = pManageLinks,
                        canApproveMembers = pApprove,
                        canPostAnonymously = pAnon
                    )
                    onConfirm(customBadge, perms)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
            ) {
                Text("Guardar Admin")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// MEMBER INDIVIDUAL RESTRICTIONS DIALOG
@Composable
private fun MemberRestrictionsDialog(
    member: GroupMember,
    onDismiss: () -> Unit,
    onSave: (GroupMember) -> Unit
) {
    var isMuted by remember { mutableStateOf(member.isMuted) }
    var canText by remember { mutableStateOf(member.canSendText) }
    var canMedia by remember { mutableStateOf(member.canSendMedia) }
    var canStickers by remember { mutableStateOf(member.canSendStickersGifs) }
    var canLinks by remember { mutableStateOf(member.canEmbedLinks) }
    var canPolls by remember { mutableStateOf(member.canCreatePolls) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restricciones para ${member.displayName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Silenciar totalmente (Mudo)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Switch(checked = isMuted, onCheckedChange = { isMuted = it })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Permisos Selectivos:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = canText, onCheckedChange = { canText = it }); Text("Enviar texto", fontSize = 12.sp) }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = canMedia, onCheckedChange = { canMedia = it }); Text("Enviar fotos/videos/audios", fontSize = 12.sp) }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = canStickers, onCheckedChange = { canStickers = it }); Text("Enviar Stickers & GIFs", fontSize = 12.sp) }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = canLinks, onCheckedChange = { canLinks = it }); Text("Enviar previsualización de enlaces", fontSize = 12.sp) }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = canPolls, onCheckedChange = { canPolls = it }); Text("Crear encuestas", fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = member.copy(
                        isMuted = isMuted,
                        canSendText = canText,
                        canSendMedia = canMedia,
                        canSendStickersGifs = canStickers,
                        canEmbedLinks = canLinks,
                        canCreatePolls = canPolls,
                        role = if (isMuted || !canText) GroupRole.RESTRICTED else member.role
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
            ) { Text("Guardar Restricciones") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ADD TOPIC DIALOG
@Composable
private fun AddTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, emoji: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💬") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Subtema / Foro", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it.take(2) },
                    label = { Text("Icono Emoji") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Subtema") },
                    placeholder = { Text("ej: Anuncios, General, Soporte") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), emoji.ifBlank { "💬" }) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
            ) { Text("Crear Subtema") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
