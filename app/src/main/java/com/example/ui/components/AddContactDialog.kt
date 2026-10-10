package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatType
import com.example.data.model.User
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.launch

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onSearchUserInFirebase: suspend (String) -> User?,
    onAddContactAndOpenChat: (title: String, username: String, phone: String, isFirebase: Boolean) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchExecuted by remember { mutableStateOf(false) }
    var foundFirebaseUser by remember { mutableStateOf<User?>(null) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val isUsername = query.trim().startsWith("@")
    val isPhone = query.trim().replace("+", "").replace("-", "").replace(" ", "").all { it.isDigit() } && query.trim().length >= 6

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = TelegramBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Añadir Contacto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Busca por @usuario o ingresa un número de teléfono. Si el usuario existe en Firebase se conectará a la nube, los demás se protegerán con cifrado extremo a extremo (E2EE).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        searchExecuted = false
                        foundFirebaseUser = null
                    },
                    label = { Text("@usuario o teléfono") },
                    placeholder = { Text("Ej: @maria_dev o +34 600...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = when {
                                isUsername -> Icons.Default.AlternateEmail
                                isPhone -> Icons.Default.Phone
                                else -> Icons.Default.Search
                            },
                            contentDescription = null,
                            tint = if (isUsername || isPhone) TelegramAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (query.isNotBlank()) {
                                    isSearching = true
                                    searchExecuted = true
                                    coroutineScope.launch {
                                        foundFirebaseUser = onSearchUserInFirebase(query)
                                        isSearching = false
                                    }
                                }
                            },
                            enabled = query.isNotBlank() && !isSearching,
                            modifier = Modifier.testTag("execute_search_contact_button")
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar en Firebase",
                                    tint = TelegramBlue
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_search_input")
                )

                // Detection badges
                if (query.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isUsername) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TelegramAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Formato: Nombre de usuario @",
                                    fontSize = 11.sp,
                                    color = TelegramBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else if (isPhone) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Formato: Número de teléfono 📱",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Result Box
                if (searchExecuted && !isSearching) {
                    val firebaseUser = foundFirebaseUser
                    if (firebaseUser != null) {
                        // User in Firebase
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Usuario de Firebase Cloud",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = firebaseUser.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "${firebaseUser.username} • ${firebaseUser.phoneNumber.ifEmpty { "Sin teléfono público" }}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = firebaseUser.bio,
                                    fontSize = 11.sp,
                                    color = Color(0xFF388E3C),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    } else {
                        // Not in Firebase -> E2EE Encrypted Chat
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cifrado de Extremo a Extremo (E2EE)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Este usuario no está en la base de datos de Firebase. Se creará un Chat Secreto protegido con claves criptográficas exclusivas en tu dispositivo.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF5D4037),
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Huella de clave E2EE: SHA-256 #D4A8-E19C-33F2",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        val isFirebase = foundFirebaseUser != null
                        val title = foundFirebaseUser?.displayName ?: if (isUsername) query.trim() else "Contacto ${query.trim()}"
                        val userHandle = foundFirebaseUser?.username ?: if (isUsername) query.trim() else ""
                        val phone = foundFirebaseUser?.phoneNumber ?: if (isPhone) query.trim() else ""
                        onAddContactAndOpenChat(title, userHandle, phone, isFirebase)
                        onDismiss()
                    }
                },
                enabled = query.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (foundFirebaseUser != null) Color(0xFF2E7D32) else TelegramBlue
                ),
                modifier = Modifier.testTag("confirm_add_contact_button")
            ) {
                if (foundFirebaseUser != null) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Chat Cloud")
                } else {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Chat E2EE")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
