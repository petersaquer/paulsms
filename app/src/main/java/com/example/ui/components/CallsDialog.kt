package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Chat
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

data class CallLogEntry(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val timeAgo: String,
    val duration: String,
    val isMissed: Boolean = false,
    val isOutgoing: Boolean = false,
    val isVideo: Boolean = false
)

private val DEFAULT_CALL_LOGS = listOf(
    CallLogEntry("c1", "Soporte Paul", "+595981000111", "Hoy, 10:24", "2m 14s", isMissed = false, isOutgoing = false),
    CallLogEntry("c2", "Familia", "+595981222333", "Ayer, 18:45", "5m 30s", isMissed = false, isOutgoing = true),
    CallLogEntry("c3", "Contacto Desconocido", "+595981555666", "10 Oct, 14:12", "Perdida", isMissed = true, isOutgoing = false)
)

@Composable
fun CallsDialog(
    availableChats: List<Chat> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var dialNumber by remember { mutableStateOf("") }
    var activeCallContact by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isActiveCallVideo by remember { mutableStateOf(false) }

    val callLogs = remember(availableChats) {
        if (availableChats.isNotEmpty()) {
            availableChats.take(5).mapIndexed { idx, chat ->
                CallLogEntry(
                    id = "call_$idx",
                    contactName = chat.title,
                    phoneNumber = chat.contactPhone ?: "+595981" + (100000 + idx * 37),
                    timeAgo = if (idx == 0) "Hoy, 11:30" else if (idx == 1) "Ayer, 19:15" else "Hace ${idx + 1} días",
                    duration = if (idx == 2) "Perdida" else "${(idx + 1) * 2}m 15s",
                    isMissed = (idx == 2),
                    isOutgoing = (idx % 2 == 1),
                    isVideo = (idx == 1)
                )
            }
        } else {
            DEFAULT_CALL_LOGS
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with title and close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Llamadas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Historial vs Marcar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
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
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Recientes", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Dialpad, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Marcar número", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Call History Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(callLogs, key = { it.id }) { call ->
                            CallItemRow(
                                call = call,
                                onCall = {
                                    isActiveCallVideo = call.isVideo
                                    activeCallContact = Pair(call.contactName, call.phoneNumber)
                                }
                            )
                        }
                    }
                } else {
                    // Dialer Tab
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = dialNumber,
                            onValueChange = { dialNumber = it },
                            placeholder = { Text("Ej. +595 981 123456", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = TelegramBlue)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TelegramBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val numberToDial = dialNumber.ifBlank { "+595 981 123456" }
                                    isActiveCallVideo = false
                                    activeCallContact = Pair(numberToDial, numberToDial)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Llamada de voz")
                            }

                            Button(
                                onClick = {
                                    val numberToDial = dialNumber.ifBlank { "+595 981 123456" }
                                    isActiveCallVideo = true
                                    activeCallContact = Pair(numberToDial, numberToDial)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Videollamada")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    // Active Call Overlay inside CallsDialog
    activeCallContact?.let { contact ->
        ActiveCallScreen(
            contactName = contact.first,
            phoneNumber = contact.second,
            isVideoCall = isActiveCallVideo,
            onEndCall = { activeCallContact = null }
        )
    }
}

@Composable
private fun CallItemRow(
    call: CallLogEntry,
    onCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onCall)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    call.isMissed -> Icons.AutoMirrored.Filled.CallMissed
                    call.isOutgoing -> Icons.AutoMirrored.Filled.CallMade
                    else -> Icons.AutoMirrored.Filled.CallReceived
                },
                contentDescription = null,
                tint = if (call.isMissed) Color.Red else Color(0xFF00C853),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.contactName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (call.isMissed) Color.Red else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${call.timeAgo} • ${call.duration}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        IconButton(onClick = onCall) {
            Icon(
                imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Llamar",
                tint = Color(0xFF00C853)
            )
        }
    }
}

private fun startDialIntent(context: Context, number: String) {
    try {
        val clean = number.trim()
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo iniciar el marcador: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
