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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import com.example.ui.components.PaulAppLogo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.User
import com.example.data.model.UserStatusGroup
import com.example.ui.components.AddContactDialog
import com.example.ui.components.ChatListItem
import com.example.ui.components.NewChatDialog
import com.example.ui.components.StoriesBar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import com.example.ui.components.AvatarView
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.ChatFolder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    currentUser: User?,
    chats: List<Chat>,
    selectedFolder: ChatFolder,
    searchQuery: String,
    isSearching: Boolean,
    isDarkMode: Boolean,
    isFirebaseSignedIn: Boolean,
    statusGroups: List<UserStatusGroup> = emptyList(),
    onSelectFolder: (ChatFolder) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onToggleDarkMode: () -> Unit,
    onSignInGoogle: () -> Unit,
    onSearchUserInFirebase: suspend (String) -> User?,
    onAddContactAndOpenChat: (title: String, username: String, phone: String, isFirebase: Boolean) -> Unit,
    onChatClick: (Chat) -> Unit,
    onCreateNewChat: (title: String, type: ChatType, description: String) -> Unit,
    onOpenSavedMessages: () -> Unit,
    onOpenAdminPanel: () -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenStatusGroup: (UserStatusGroup) -> Unit = {},
    onCreateTextStatus: () -> Unit = {},
    onCreateMediaStatus: () -> Unit = {},
    onDeleteChat: (String) -> Unit = {},
    onTogglePinChat: (String) -> Unit = {},
    onToggleMuteChat: (String) -> Unit = {},
    onOpenSettings: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var selectedChatForOptions by remember { mutableStateOf<Chat?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SettingsDrawer(
                    currentUser = currentUser,
                    isDarkMode = isDarkMode,
                    isFirebaseSignedIn = isFirebaseSignedIn,
                    onToggleDarkMode = onToggleDarkMode,
                    onSignInGoogle = onSignInGoogle,
                    onAddContact = { showAddContactDialog = true },
                    onOpenContacts = {
                        scope.launch { drawerState.close() }
                        onOpenContacts()
                    },
                    onCreateStatus = {
                        scope.launch { drawerState.close() }
                        onCreateTextStatus()
                    },
                    onOpenSavedMessages = {
                        scope.launch { drawerState.close() }
                        onOpenSavedMessages()
                    },
                    onNewGroup = {
                        scope.launch { drawerState.close() }
                        showNewChatDialog = true
                    },
                    onOpenAdminPanel = {
                        scope.launch { drawerState.close() }
                        onOpenAdminPanel()
                    },
                    onOpenSettings = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isSearching) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = {
                                    Text(
                                        "Buscar @usuario, teléfono o chats...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 15.sp
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_text_field")
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { onToggleSearch(false) },
                                modifier = Modifier.testTag("close_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar búsqueda"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PaulAppLogo(size = 30.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Paul",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_drawer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú principal",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            if (currentUser?.isAdmin == true || currentUser?.isModerator == true) {
                                IconButton(
                                    onClick = onOpenAdminPanel,
                                    modifier = Modifier.testTag("top_admin_panel_button")
                                ) {
                                    Text("👑", fontSize = 18.sp)
                                }
                            }

                            // Search and Add Contacts (Firestore Query)
                            IconButton(
                                onClick = onOpenContacts,
                                modifier = Modifier.testTag("top_contacts_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = "Contactos (@ / email)",
                                    tint = TelegramBlue
                                )
                            }

                            // Add Contact by @ or phone
                            IconButton(
                                onClick = { showAddContactDialog = true },
                                modifier = Modifier.testTag("top_add_contact_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Añadir Contacto (@ / 📱)",
                                    tint = TelegramBlue
                                )
                            }

                            IconButton(
                                onClick = { onToggleSearch(true) },
                                modifier = Modifier.testTag("open_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onToggleDarkMode,
                                modifier = Modifier.testTag("theme_quick_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Cambiar tema",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action: Write Text Status (WhatsApp style)
                    SmallFloatingActionButton(
                        onClick = onCreateTextStatus,
                        containerColor = TelegramAccent,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_create_text_status")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = "Crear Estado de Texto",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quick Action: Create Reel / Media Status
                    SmallFloatingActionButton(
                        onClick = onCreateMediaStatus,
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_create_reel_status")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Crear Reel o Foto",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Main FAB: Contacts Search & Chat
                    FloatingActionButton(
                        onClick = onOpenContacts,
                        containerColor = TelegramBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("new_chat_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Contactos y chats",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Folder Tabs Row
                if (!isSearching) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedFolder.ordinal,
                        edgePadding = 12.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TelegramBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFolder.ordinal]),
                                color = TelegramBlue,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        ChatFolder.entries.forEach { folder ->
                            val isSelected = selectedFolder == folder
                            val folderUnread = remember(chats, folder) {
                                chats.filter { chat ->
                                    when (folder) {
                                        ChatFolder.ALL -> true
                                        ChatFolder.DIRECT -> chat.type == ChatType.DIRECT || chat.type == ChatType.SECRET
                                        ChatFolder.GROUPS -> chat.type == ChatType.GROUP
                                        ChatFolder.CHANNELS -> chat.type == ChatType.CHANNEL
                                        ChatFolder.BOTS -> chat.type == ChatType.BOT
                                    }
                                }.sumOf { it.unreadCount }
                            }

                            Tab(
                                selected = isSelected,
                                onClick = { onSelectFolder(folder) },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = folder.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        )
                                        if (folderUnread > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) TelegramBlue else MaterialTheme.colorScheme.surfaceVariant)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = folderUnread.toString(),
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("tab_${folder.name}")
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Stories & Reels carousel (WhatsApp style)
                    if (selectedFolder == ChatFolder.ALL) {
                        StoriesBar(
                            currentUser = currentUser,
                            statusGroups = statusGroups,
                            onOpenStatusGroup = onOpenStatusGroup,
                            onCreateTextStatus = onCreateTextStatus,
                            onCreateMediaStatus = onCreateMediaStatus
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }

                // Filtered Chat List with memoization for maximum fluidity
                val displayedChats = remember(chats, selectedFolder, searchQuery) {
                    chats.filter { chat ->
                        val folderMatch = when (selectedFolder) {
                            ChatFolder.ALL -> true
                            ChatFolder.DIRECT -> chat.type == ChatType.DIRECT || chat.type == ChatType.SAVED_MESSAGES || chat.type == ChatType.SECRET
                            ChatFolder.GROUPS -> chat.type == ChatType.GROUP
                            ChatFolder.CHANNELS -> chat.type == ChatType.CHANNEL
                            ChatFolder.BOTS -> chat.type == ChatType.BOT
                        }
                        val searchMatch = if (searchQuery.isBlank()) true else {
                            chat.title.contains(searchQuery, ignoreCase = true) ||
                                    chat.lastMessage.contains(searchQuery, ignoreCase = true) ||
                                    chat.username.contains(searchQuery, ignoreCase = true) ||
                                    (chat.contactPhone?.contains(searchQuery, ignoreCase = true) ?: false)
                        }
                        folderMatch && searchMatch
                    }.sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.lastUpdatedTimestamp })
                }

                if (displayedChats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No se encontraron resultados para \"$searchQuery\""
                                else "No hay chats en esta sección",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_list_view")
                    ) {
                        items(displayedChats, key = { it.id }) { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = { onChatClick(chat) },
                                onLongClick = { selectedChatForOptions = chat }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 78.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            )
                        }
                    }
                }
            }
        }
    }

    selectedChatForOptions?.let { chat ->
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        WhatsAppChatOptionsDialog(
            chat = chat,
            onDismiss = { selectedChatForOptions = null },
            onOpenAndReply = {
                selectedChatForOptions = null
                onChatClick(chat)
            },
            onForward = {
                selectedChatForOptions = null
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, "Contacto: ${chat.title} (${chat.username.ifBlank { chat.contactPhone ?: "" }})")
                    type = "text/plain"
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Reenviar contacto"))
            },
            onCopy = {
                selectedChatForOptions = null
                val info = "${chat.title}\n${chat.username.ifBlank { chat.contactPhone ?: "" }}"
                clipboardManager.setText(AnnotatedString(info))
                Toast.makeText(context, "Información del chat copiada", Toast.LENGTH_SHORT).show()
            },
            onTogglePin = {
                selectedChatForOptions = null
                onTogglePinChat(chat.id)
                Toast.makeText(context, if (chat.isPinned) "Chat desfijado" else "Chat fijado", Toast.LENGTH_SHORT).show()
            },
            onToggleMute = {
                selectedChatForOptions = null
                onToggleMuteChat(chat.id)
                Toast.makeText(context, if (chat.isMuted) "Notificaciones activadas" else "Chat silenciado", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                selectedChatForOptions = null
                onDeleteChat(chat.id)
                Toast.makeText(context, "Chat eliminado", Toast.LENGTH_SHORT).show()
            },
            onReaction = { emoji ->
                selectedChatForOptions = null
                Toast.makeText(context, "Reacción $emoji enviada a ${chat.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onSearchUserInFirebase = onSearchUserInFirebase,
            onAddContactAndOpenChat = onAddContactAndOpenChat
        )
    }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onCreateChat = { title, type, desc ->
                showNewChatDialog = false
                onCreateNewChat(title, type, desc)
            }
        )
    }
}

@Composable
private fun WhatsAppChatOptionsDialog(
    chat: Chat,
    onDismiss: () -> Unit,
    onOpenAndReply: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onDelete: () -> Unit,
    onReaction: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. WhatsApp Floating Reaction Emojis
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val quickEmojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "🔥", "👏")
                    quickEmojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onReaction(emoji)
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }

            // 2. Action Menu Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    if (!showDeleteConfirm) {
                        // Header info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarView(
                                title = chat.title,
                                type = chat.type,
                                gradientStart = chat.avatarGradientStart,
                                gradientEnd = chat.avatarGradientEnd,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = chat.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (chat.username.isNotBlank()) chat.username else (chat.contactPhone ?: "Chat directo"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        ChatActionOptionRow(
                            icon = Icons.AutoMirrored.Filled.Reply,
                            title = "Responder chat",
                            onClick = onOpenAndReply
                        )

                        ChatActionOptionRow(
                            icon = Icons.AutoMirrored.Filled.Forward,
                            title = "Reenviar chat",
                            onClick = onForward
                        )

                        ChatActionOptionRow(
                            icon = Icons.Default.ContentCopy,
                            title = "Copiar información",
                            onClick = onCopy
                        )

                        ChatActionOptionRow(
                            icon = Icons.Default.PushPin,
                            title = if (chat.isPinned) "Desfijar chat" else "Fijar chat",
                            onClick = onTogglePin
                        )

                        ChatActionOptionRow(
                            icon = if (chat.isMuted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            title = if (chat.isMuted) "Activar notificaciones" else "Silenciar notificaciones",
                            onClick = onToggleMute
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        ChatActionOptionRow(
                            icon = Icons.Default.Delete,
                            title = "Eliminar chat",
                            iconTint = Color.Red,
                            titleColor = Color.Red,
                            onClick = { showDeleteConfirm = true }
                        )
                    } else {
                        // Delete Confirmation
                        Text(
                            text = "¿Deseas eliminar este chat?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                        Text(
                            text = "Se borrarán todos los mensajes de esta conversación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ChatActionOptionRow(
                            icon = Icons.Default.Delete,
                            title = "Confirmar y eliminar chat",
                            iconTint = Color.Red,
                            titleColor = Color.Red,
                            onClick = onDelete
                        )

                        ChatActionOptionRow(
                            icon = Icons.Default.Close,
                            title = "Cancelar",
                            onClick = { showDeleteConfirm = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatActionOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            fontSize = 15.sp
        )
    }
}
