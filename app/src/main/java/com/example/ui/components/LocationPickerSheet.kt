package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationData
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

data class NearbyPlace(
    val name: String,
    val address: String,
    val distance: String,
    val lat: Double,
    val lng: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    onDismiss: () -> Unit,
    onSendLocation: (LocationData) -> Unit
) {
    var isLiveModeSelected by remember { mutableStateOf(false) }
    var selectedDurationMinutes by remember { mutableIntStateOf(15) }

    val nearbyPlaces = remember {
        listOf(
            NearbyPlace("Plaza Mayor", "Calle Mayor, s/n, Madrid", "Ubicación actual", 40.415363, -3.707398),
            NearbyPlace("Parque del Retiro", "Plaza de la Independencia, 7", "a 350 m", 40.415260, -3.684499),
            NearbyPlace("Cafetería Paul & Books", "Calle del Arenal, 14", "a 80 m", 40.416820, -3.707100),
            NearbyPlace("Estación de Metro Sol", "Puerta del Sol", "a 150 m", 40.416900, -3.703500)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = TelegramBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compartir Ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Map Preview Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFE8ECEF))
            ) {
                MapCanvasPreview(isLive = isLiveModeSelected)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: Send current static location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onSendLocation(
                            LocationData(
                                latitude = 40.416775,
                                longitude = -3.703790,
                                title = "Ubicación actual",
                                address = "Plaza Mayor, 28012 Madrid, España",
                                isLive = false,
                                accuracyMeters = 8.5f
                            )
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("send_current_location_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enviar mi ubicación actual",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TelegramBlue
                    )
                    Text(
                        text = "Precisión de 8.5 metros • Madrid, España",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Option 2: Live Location Sharing (Tiempo Real)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isLiveModeSelected = !isLiveModeSelected }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("toggle_live_location_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00C853)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Compartir ubicación en tiempo real",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00C853)
                    )
                    Text(
                        text = "Actualiza tu movimiento en directo con este chat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Duration selector if Live Location selected
            AnimatedVisibility(visible = isLiveModeSelected) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Selecciona la duración del directo:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(15 to "15 min", 60 to "1 hora", 480 to "8 horas").forEach { (mins, label) ->
                                val isSelected = selectedDurationMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDurationMinutes = mins },
                                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00C853),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onSendLocation(
                                    LocationData(
                                        latitude = 40.416775,
                                        longitude = -3.703790,
                                        title = "Ubicación en tiempo real",
                                        address = "Compartiendo en directo mientras te mueves",
                                        isLive = true,
                                        liveDurationMinutes = selectedDurationMinutes,
                                        remainingMinutes = selectedDurationMinutes,
                                        accuracyMeters = 5.0f,
                                        isMoving = true
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Iniciar transmisión en tiempo real")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nearby Places Section
            Text(
                text = "LUGARES CERCANOS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            nearbyPlaces.forEach { place ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onSendLocation(
                                LocationData(
                                    latitude = place.lat,
                                    longitude = place.lng,
                                    title = place.name,
                                    address = place.address,
                                    isLive = false
                                )
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${place.distance} • ${place.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MapCanvasPreview(
    isLive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val radiusRatio by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_radius"
    )
    val alphaRatio by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        val w = size.width
        val h = size.height

        // Background terrain
        drawRect(color = Color(0xFFF1EFEA))

        // Park green area
        val parkPath = Path().apply {
            moveTo(w * 0.65f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.7f)
            lineTo(w * 0.65f, h * 0.4f)
            close()
        }
        drawPath(parkPath, color = Color(0xFFD4E7CA))

        // Water blue stream
        val riverPath = Path().apply {
            moveTo(0f, h * 0.85f)
            cubicTo(w * 0.3f, h * 0.7f, w * 0.7f, h * 0.95f, w, h * 0.8f)
        }
        drawPath(riverPath, color = Color(0xFFA5D6A7), style = Stroke(width = 18f))

        // Roads
        drawLine(
            color = Color.White,
            start = Offset(0f, h * 0.45f),
            end = Offset(w, h * 0.45f),
            strokeWidth = 14f
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.42f, 0f),
            end = Offset(w * 0.42f, h),
            strokeWidth = 12f
        )
        drawLine(
            color = Color.White,
            start = Offset(0f, h * 0.2f),
            end = Offset(w, h * 0.65f),
            strokeWidth = 8f
        )

        // Center Pin position
        val center = Offset(w * 0.42f, h * 0.45f)

        if (isLive) {
            // Live Radar pulsing ring
            drawCircle(
                color = Color(0xFF00C853).copy(alpha = alphaRatio),
                radius = 45f * radiusRatio,
                center = center
            )
            // Accuracy radius
            drawCircle(
                color = Color(0xFF00C853).copy(alpha = 0.25f),
                radius = 28f,
                center = center
            )
            // Center beacon dot
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = center
            )
            drawCircle(
                color = Color(0xFF00C853),
                radius = 8f,
                center = center
            )
        } else {
            // Static Location Pin
            drawCircle(
                color = Color(0x33000000),
                radius = 14f,
                center = Offset(center.x, center.y + 4f)
            )
            drawCircle(
                color = Color(0xFF0288D1),
                radius = 16f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = center
            )
        }
    }
}
