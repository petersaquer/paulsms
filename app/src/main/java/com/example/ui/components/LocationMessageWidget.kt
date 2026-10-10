package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationData
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@Composable
fun LocationMessageWidget(
    location: LocationData,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .clickable { /* Tap to view in map */ }
    ) {
        // Map Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Color(0xFFE8ECEF))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Map styling
                drawRect(color = Color(0xFFF3EFE7))

                // Roads
                drawLine(
                    color = Color.White,
                    start = Offset(0f, h * 0.5f),
                    end = Offset(w, h * 0.5f),
                    strokeWidth = 14f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.5f, 0f),
                    end = Offset(w * 0.5f, h),
                    strokeWidth = 14f
                )
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(0f, h * 0.3f),
                    end = Offset(w, h * 0.7f),
                    strokeWidth = 8f
                )

                // Park
                val parkPath = Path().apply {
                    moveTo(w * 0.65f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.45f)
                    lineTo(w * 0.65f, h * 0.2f)
                    close()
                }
                drawPath(parkPath, color = Color(0xFFC8E6C9))

                val center = Offset(w * 0.5f, h * 0.5f)

                if (location.isLive) {
                    // Pulsing green live radar
                    drawCircle(
                        color = Color(0xFF00C853).copy(alpha = pulseAlpha),
                        radius = 42f * pulseRadius,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF00C853).copy(alpha = 0.2f),
                        radius = 24f,
                        center = center
                    )
                    drawCircle(color = Color.White, radius = 10f, center = center)
                    drawCircle(color = Color(0xFF00C853), radius = 7f, center = center)
                } else {
                    // Static Red/Blue Location Pin
                    drawCircle(color = Color(0x33000000), radius = 10f, center = Offset(center.x, center.y + 4f))
                    drawCircle(color = Color(0xFFE53935), radius = 14f, center = center)
                    drawCircle(color = Color.White, radius = 5f, center = center)
                }
            }

            // Top Badge on Map
            if (location.isLive) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00C853))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EN DIRECTO • ${location.remainingMinutes} min",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Location Info
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (location.isLive) Icons.Default.Radar else Icons.Default.Place,
                    contentDescription = null,
                    tint = if (location.isLive) Color(0xFF00C853) else TelegramBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = location.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = location.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (location.isLive) "Transmitiendo en tiempo real" else "Tocar para abrir en mapa",
                    style = MaterialTheme.typography.labelSmall,
                    color = TelegramAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = "Ruta",
                    tint = TelegramAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
