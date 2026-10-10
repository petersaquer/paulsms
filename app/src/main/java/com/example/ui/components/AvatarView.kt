package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatType

@Composable
fun AvatarView(
    title: String,
    type: ChatType,
    gradientStart: Long = 0xFF2AABEE,
    gradientEnd: Long = 0xFF2275B3,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    val effectiveStart = if (type == ChatType.SECRET) 0xFF00C853 else gradientStart
    val effectiveEnd = if (type == ChatType.SECRET) 0xFF00796B else gradientEnd

    val brush = Brush.linearGradient(
        colors = listOf(Color(effectiveStart), Color(effectiveEnd))
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            ChatType.SAVED_MESSAGES -> {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Mensajes guardados",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
            ChatType.BOT -> {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Bot",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
            ChatType.CHANNEL -> {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Canal",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
            ChatType.GROUP -> {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Grupo",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
            ChatType.SECRET -> {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Chat Secreto Cifrado",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
            ChatType.DIRECT -> {
                val initial = title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
