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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
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
import com.example.ui.components.AddContactDialog
import com.example.ui.components.ChatListItem
import com.example.ui.components.NewChatDialog
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
    onOpenSettings: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }

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
                    onOpenSavedMessages = {
                        scope.launch { drawerState.close() }
                        onOpenSavedMessages()
                    },
                    onNewGroup = {
                        scope.launch { drawerState.close() }
                        showNewChatDialog = true
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
                                Text(
                                    text = "Paul",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isFirebaseSignedIn) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Firebase Conectado",
                                        tint = TelegramAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
                FloatingActionButton(
                    onClick = { showAddContactDialog = true },
                    containerColor = TelegramBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("new_chat_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Añadir contacto o nuevo chat",
                        modifier = Modifier.size(24.dp)
                    )
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
                    }.sortedByDescending { it.isPinned }
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
                                onClick = { onChatClick(chat) }
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
