package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.User
import com.example.data.model.UserStatusGroup
import com.example.ui.components.ChatListItem
import com.example.ui.components.StoriesBar
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
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                    onAddContact = { onOpenContacts() },
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
                        onCreateNewChat("Nuevo Grupo", ChatType.GROUP, "Grupo de chat")
                    },
                    onOpenAdminPanel = {
                        scope.launch { drawerState.close() }
                        onOpenAdminPanel()
                    },
                    onOpenSettings = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    onOpenProfile = {
                        scope.launch { drawerState.close() }
                        onOpenProfile()
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
                                placeholder = { Text("Buscar chats o usuarios...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("search_chats_input")
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { onToggleSearch(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda")
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("Paul", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_drawer_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { onToggleSearch(true) },
                                modifier = Modifier.testTag("search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onCreateNewChat("Nuevo Chat", ChatType.DIRECT, "Conversación") },
                    containerColor = TelegramBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("new_chat_fab")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Nuevo chat")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Folder Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedFolder.ordinal,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    ChatFolder.values().forEach { folder ->
                        Tab(
                            selected = selectedFolder == folder,
                            onClick = { onSelectFolder(folder) },
                            text = { Text(folder.label) },
                            modifier = Modifier.testTag("folder_tab_${folder.name.lowercase()}")
                        )
                    }
                }

                // Stories Bar
                StoriesBar(
                    currentUser = currentUser,
                    statusGroups = statusGroups,
                    onOpenStatusGroup = onOpenStatusGroup,
                    onCreateStatus = onCreateTextStatus
                )

                HorizontalDivider()

                val filteredChats = chats.filter {
                    when (selectedFolder) {
                        ChatFolder.ALL -> true
                        ChatFolder.DIRECT -> it.type == ChatType.DIRECT
                        ChatFolder.GROUPS -> it.type == ChatType.GROUP
                        ChatFolder.CHANNELS -> it.type == ChatType.CHANNEL
                        ChatFolder.BOTS -> it.type == ChatType.BOT
                    } && (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.lastMessage.contains(searchQuery, ignoreCase = true))
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredChats, key = { it.id }) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onChatClick(chat) },
                            onLongClick = { onTogglePinChat(chat.id) }
                        )
                    }
                }
            }
        }
    }
}
