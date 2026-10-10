package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentSheet(
    onDismiss: () -> Unit,
    onSelectCamera: () -> Unit = {},
    onSelectGallery: () -> Unit,
    onSelectGif: () -> Unit,
    onSelectLocation: () -> Unit,
    onSelectPoll: () -> Unit,
    onSelectFile: () -> Unit,
    onSelectContact: () -> Unit = {},
    onSelectAudio: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Compartir en el chat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // Row 1: Camera, Gallery, File, Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttachmentItem(
                    title = "Cámara",
                    icon = Icons.Default.PhotoCamera,
                    color = Color(0xFFE91E63),
                    testTag = "attach_camera",
                    onClick = {
                        onDismiss()
                        onSelectCamera()
                    }
                )

                AttachmentItem(
                    title = "Galería",
                    icon = Icons.Default.Image,
                    color = Color(0xFF29B6F6),
                    testTag = "attach_gallery",
                    onClick = {
                        onDismiss()
                        onSelectGallery()
                    }
                )

                AttachmentItem(
                    title = "Archivo",
                    icon = Icons.Default.Description,
                    color = Color(0xFF5C6BC0),
                    testTag = "attach_file",
                    onClick = {
                        onDismiss()
                        onSelectFile()
                    }
                )

                AttachmentItem(
                    title = "Ubicación",
                    icon = Icons.Default.LocationOn,
                    color = Color(0xFF43A047),
                    testTag = "attach_location",
                    onClick = {
                        onDismiss()
                        onSelectLocation()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Poll, Contact, Audio, GIF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttachmentItem(
                    title = "Encuesta",
                    icon = Icons.Default.Poll,
                    color = Color(0xFFFFB300),
                    testTag = "attach_poll",
                    onClick = {
                        onDismiss()
                        onSelectPoll()
                    }
                )

                AttachmentItem(
                    title = "Contacto",
                    icon = Icons.Default.Person,
                    color = Color(0xFF00ACC1),
                    testTag = "attach_contact",
                    onClick = {
                        onDismiss()
                        onSelectContact()
                    }
                )

                AttachmentItem(
                    title = "Música",
                    icon = Icons.Default.Headphones,
                    color = Color(0xFFFF7043),
                    testTag = "attach_audio",
                    onClick = {
                        onDismiss()
                        onSelectAudio()
                    }
                )

                AttachmentItem(
                    title = "GIFs",
                    icon = Icons.Default.Gif,
                    color = Color(0xFFAB47BC),
                    testTag = "attach_gif",
                    onClick = {
                        onDismiss()
                        onSelectGif()
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AttachmentItem(
    title: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
