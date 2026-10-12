package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.SavedContact
import com.example.data.model.StatusPrivacy
import com.example.data.model.StatusType
import com.example.ui.theme.TelegramAccent

private val STATUS_BACKGROUND_PALETTES = listOf(
    0xFF005C4BL, // WhatsApp teal clásica
    0xFF128C7EL, // WhatsApp verde esmeralda
    0xFF1F6F8BL, // Azul petróleo
    0xFF4A148CL, // Púrpura profundo
    0xFF880E4FL, // Vino tinto
    0xFFBF360CL, // Terracota cálido
    0xFF1B5E20L, // Verde bosque
    0xFF263238L  // Grafito oscuro
)

private val FONT_STYLES = listOf(
    "NORMAL" to ("Sans" to FontFamily.Default),
    "SERIF" to ("Serif" to FontFamily.Serif),
    "MONO" to ("Mono" to FontFamily.Monospace),
    "CURSIVE" to ("Cursiva" to FontFamily.Cursive)
)

private val PRESET_MEDIA = listOf(
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80" to "Playa",
    "https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=800&q=80" to "Naturaleza",
    "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=800&q=80" to "Mascotas",
    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80" to "Ciudad"
)

@Composable
fun CreateStatusDialog(
    initialType: StatusType = StatusType.TEXT,
    availableContacts: List<SavedContact>,
    onDismiss: () -> Unit,
    onPublish: (
        type: StatusType,
        text: String,
        mediaUrl: String,
        bgGradient: Long,
        fontStyle: String,
        caption: String,
        privacy: StatusPrivacy,
        excludedIds: List<String>,
        includedIds: List<String>
    ) -> Unit
) {
    var isTextMode by remember { mutableStateOf(initialType == StatusType.TEXT) }

    // Text status state
    var textContent by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var selectedFontIndex by remember { mutableIntStateOf(0) }

    // Media status state
    var selectedMediaUrl by remember { mutableStateOf(PRESET_MEDIA[0].first) }
    var captionText by remember { mutableStateOf("") }

    val statusMediaLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
        }
    }

    // Privacy state
    var privacy by remember { mutableStateOf(StatusPrivacy.MY_CONTACTS) }
    var excludedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var includedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    var isPublishing by remember { mutableStateOf(false) }

    val currentBgColor = if (isTextMode) {
        Color(STATUS_BACKGROUND_PALETTES[selectedColorIndex])
    } else {
        Color(0xFF0B141B)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentBgColor)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 1. Top Bar: Clean WhatsApp design with no cluttered icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("close_create_status")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }

                    // Mode Switcher: Texto / Foto
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isTextMode) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { isTextMode = true }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Texto",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isTextMode) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!isTextMode) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { isTextMode = false }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Foto",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (!isTextMode) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Top Right Tools
                    if (isTextMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    selectedFontIndex = (selectedFontIndex + 1) % FONT_STYLES.size
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = "Fuente",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedColorIndex = (selectedColorIndex + 1) % STATUS_BACKGROUND_PALETTES.size
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = "Color",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = {
                                statusMediaLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Galería",
                                tint = Color.White
                            )
                        }
                    }
                }

                // 2. Middle Content Area (Clean, responsive, never overlapping)
                if (isTextMode) {
                    val currentFont = FONT_STYLES[selectedFontIndex].second.second
                    val fontSize = when {
                        textContent.length > 150 -> 20.sp
                        textContent.length > 70 -> 24.sp
                        else -> 30.sp
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { if (it.length <= 400) textContent = it },
                            placeholder = {
                                Text(
                                    text = "Escribe un estado...",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = fontSize,
                                    fontFamily = currentFont,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = fontSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = currentFont,
                                textAlign = TextAlign.Center,
                                lineHeight = if (fontSize > 24.sp) 38.sp else 28.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("status_text_input")
                        )
                    }

                    // Background color row at bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        STATUS_BACKGROUND_PALETTES.forEachIndexed { idx, colorLong ->
                            val isSelected = selectedColorIndex == idx
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.25f),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorIndex = idx }
                            )
                        }
                    }
                } else {
                    // Media Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272E)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = selectedMediaUrl,
                                    contentDescription = "Previsualización de estado",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Surface(
                                    onClick = {
                                        statusMediaLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Cambiar",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Curated templates row with clean text titles (NO emojis)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PRESET_MEDIA.forEach { (url, label) ->
                                val isSelected = selectedMediaUrl == url
                                Surface(
                                    onClick = { selectedMediaUrl = url },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) TelegramAccent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, TelegramAccent) else null
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Caption input
                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            placeholder = {
                                Text("Añade un comentario...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TelegramAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                cursorColor = TelegramAccent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("status_caption_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Bottom Row: Privacy Pill + Floating WhatsApp Send Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Privacy indicator pill (Bottom-left like WhatsApp)
                    Surface(
                        onClick = { showPrivacyDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("status_privacy_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TelegramAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (privacy) {
                                    StatusPrivacy.MY_CONTACTS -> "Estados: Mis contactos"
                                    StatusPrivacy.MY_CONTACTS_EXCEPT -> "Excluidos (${excludedIds.size})"
                                    StatusPrivacy.ONLY_SHARE_WITH -> "Compartir (${includedIds.size})"
                                    StatusPrivacy.PUBLIC -> "Público"
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Floating circular Send Button (Bottom-right like WhatsApp)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTextMode && textContent.isBlank()) TelegramAccent.copy(alpha = 0.4f)
                                else TelegramAccent
                            )
                            .clickable(
                                enabled = !isPublishing && (!isTextMode || textContent.isNotBlank())
                            ) {
                                isPublishing = true
                                val type = if (isTextMode) StatusType.TEXT else StatusType.PHOTO
                                onPublish(
                                    type,
                                    textContent,
                                    selectedMediaUrl,
                                    STATUS_BACKGROUND_PALETTES[selectedColorIndex],
                                    FONT_STYLES[selectedFontIndex].first,
                                    captionText,
                                    privacy,
                                    excludedIds,
                                    includedIds
                                )
                            }
                            .testTag("publish_status_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Publicar estado",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPrivacyDialog) {
        StatusPrivacyDialog(
            currentPrivacy = privacy,
            initialExcludedIds = excludedIds,
            initialIncludedIds = includedIds,
            availableContacts = availableContacts,
            onDismiss = { showPrivacyDialog = false },
            onSavePrivacy = { newPrivacy, newExcluded, newIncluded ->
                privacy = newPrivacy
                excludedIds = newExcluded
                includedIds = newIncluded
            }
        )
    }
}
