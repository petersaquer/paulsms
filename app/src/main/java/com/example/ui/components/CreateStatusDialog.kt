package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SavedContact
import com.example.data.model.StatusPrivacy
import com.example.data.model.StatusType
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

private val STATUS_BACKGROUND_PALETTES = listOf(
    0xFF0D5C46L, // WhatsApp classic teal
    0xFF1E88E5L, // Paul messenger blue
    0xFF6A1B9AL, // Purple royal
    0xFFC2185BL, // Magenta
    0xFFD84315L, // Coral orange
    0xFF2E7D32L, // Emerald green
    0xFF263238L, // Midnight slate
    0xFF880E4FL  // Deep ruby
)

private val FONT_STYLES = listOf(
    "NORMAL" to ("Sans" to FontFamily.Default),
    "SERIF" to ("Serif" to FontFamily.Serif),
    "MONO" to ("Mono" to FontFamily.Monospace),
    "CURSIVE" to ("Cursiva" to FontFamily.Cursive)
)

private val PRESET_REELS = listOf(
    "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=800&q=80" to "Gatito curioso 🐱 (Reel)",
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80" to "Atardecer en la playa 🌅",
    "https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=800&q=80" to "Aventura al aire libre 🐕",
    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80" to "Tecnología y Espacio 🚀"
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
    var selectedTab by remember { mutableIntStateOf(if (initialType == StatusType.TEXT) 0 else 1) }

    // Text status state
    var textContent by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var selectedFontIndex by remember { mutableIntStateOf(0) }

    // Media / Reel status state
    var selectedMediaUrl by remember { mutableStateOf(PRESET_REELS[0].first) }
    var captionText by remember { mutableStateOf("") }
    var isReelMode by remember { mutableStateOf(initialType == StatusType.REEL) }

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // Top header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_create_status")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }

                    // Mode Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = TelegramAccent,
                                height = 3.dp
                            )
                        },
                        divider = {},
                        modifier = Modifier.width(220.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Texto ✍️", color = Color.White, fontSize = 14.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Reel / Foto 🎬", color = Color.White, fontSize = 14.sp) }
                        )
                    }

                    // Privacy Pill Button
                    Surface(
                        onClick = { showPrivacyDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.testTag("status_privacy_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TelegramAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (privacy) {
                                    StatusPrivacy.MY_CONTACTS -> "Contactos"
                                    StatusPrivacy.MY_CONTACTS_EXCEPT -> "Excluidos (${excludedIds.size})"
                                    StatusPrivacy.ONLY_SHARE_WITH -> "Compartir (${includedIds.size})"
                                    StatusPrivacy.PUBLIC -> "Público"
                                },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CONTENT AREA
                if (selectedTab == 0) {
                    // TEXT STATUS (WhatsApp style)
                    val bgColor = Color(STATUS_BACKGROUND_PALETTES[selectedColorIndex])
                    val currentFont = FONT_STYLES[selectedFontIndex].second.second

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgColor)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { if (it.length <= 400) textContent = it },
                            placeholder = {
                                Text(
                                    "Escribe un estado...",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 26.sp,
                                    fontFamily = currentFont,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = currentFont,
                                textAlign = TextAlign.Center
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text Formatting Controls (Color & Font picker)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color Palette Swatches
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            STATUS_BACKGROUND_PALETTES.forEachIndexed { idx, colorLong ->
                                val isSelected = selectedColorIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorLong))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorIndex = idx }
                                )
                            }
                        }

                        // Font Style Switcher Button
                        Button(
                            onClick = {
                                selectedFontIndex = (selectedFontIndex + 1) % FONT_STYLES.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FontDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(FONT_STYLES[selectedFontIndex].second.first, color = Color.White, fontSize = 12.sp)
                        }
                    }

                } else {
                    // REEL / MEDIA STATUS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Reel / Photo Selection
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    onClick = { isReelMode = false },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (!isReelMode) TelegramBlue else Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Foto", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    onClick = { isReelMode = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isReelMode) TelegramAccent else Color.White.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reel / Video 🎬", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Gallery Picker Button
                            Button(
                                onClick = {
                                    statusMediaLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            if (isReelMode) androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
                                            else androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .testTag("pick_device_media_for_status")
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📱 Subir desde la Galería de mi Dispositivo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "O selecciona una de las plantillas destacadas:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 6.dp)
                            ) {
                                PRESET_REELS.forEach { (url, label) ->
                                    val isSelected = selectedMediaUrl == url
                                    Card(
                                        onClick = { selectedMediaUrl = url },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) TelegramBlue.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TelegramAccent) else null,
                                        modifier = Modifier.width(140.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Color(0xFF2AABEE), Color(0xFF00C853))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isReelMode) Icons.Default.Movie else Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Caption input
                            OutlinedTextField(
                                value = captionText,
                                onValueChange = { captionText = it },
                                placeholder = { Text("Añade un comentario o pie de estado...", color = Color.White.copy(alpha = 0.5f)) },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TelegramAccent,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = TelegramAccent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("status_caption_input")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Bar: Publish Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isPublishing = true
                            val type = if (selectedTab == 0) StatusType.TEXT else if (isReelMode) StatusType.REEL else StatusType.PHOTO
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
                        },
                        enabled = !isPublishing && (selectedTab == 1 || textContent.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramAccent),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("publish_status_button")
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedTab == 0) "Publicar Estado" else if (isReelMode) "Publicar Reel" else "Publicar Foto",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
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
