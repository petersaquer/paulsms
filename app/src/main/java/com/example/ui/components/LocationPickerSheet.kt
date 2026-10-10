package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.RealLocationHelper
import com.example.data.model.LocationData
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    onDismiss: () -> Unit,
    onSendLocation: (LocationData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { RealLocationHelper(context) }

    var isLiveModeSelected by remember { mutableStateOf(false) }
    var selectedDurationMinutes by remember { mutableIntStateOf(15) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isLoadingLocation by remember { mutableStateOf(true) }
    var currentLocationData by remember {
        mutableStateOf<LocationData?>(null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            scope.launch {
                isLoadingLocation = true
                currentLocationData = locationHelper.getCurrentLocation()
                isLoadingLocation = false
            }
        } else {
            isLoadingLocation = false
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            scope.launch {
                isLoadingLocation = true
                currentLocationData = locationHelper.getCurrentLocation()
                isLoadingLocation = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationHelper.stopLiveLocationUpdates()
        }
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
                    text = "Ubicación en Tiempo Real (GPS Real)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                if (hasLocationPermission) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isLoadingLocation = true
                                currentLocationData = locationHelper.getCurrentLocation()
                                isLoadingLocation = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar GPS",
                            tint = TelegramBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Map Preview WebView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE8ECEF))
            ) {
                if (currentLocationData != null) {
                    val lat = currentLocationData!!.latitude
                    val lng = currentLocationData!!.longitude
                    val htmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                            <style>
                                body, html, #map { margin: 0; padding: 0; height: 100%; width: 100%; background: #f3efe7; }
                            </style>
                        </head>
                        <body>
                            <div id="map"></div>
                            <script>
                                var map = L.map('map', {zoomControl: false}).setView([$lat, $lng], 15);
                                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                                L.marker([$lat, $lng]).addTo(map);
                            </script>
                        </body>
                        </html>
                    """.trimIndent()

                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.webkit.WebView(ctx).apply {
                                webViewClient = android.webkit.WebViewClient()
                                settings.javaScriptEnabled = true
                                loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { webView ->
                            // Optional: update coordinates if they change significantly
                        }
                    )
                } else {
                    MapCanvasPreview(isLive = isLiveModeSelected)
                }

                if (isLoadingLocation) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = TelegramBlue, strokeWidth = 3.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!hasLocationPermission) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Se requiere permiso de ubicación",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Para enviar tu posición satelital real por GPS debes conceder el permiso de Android.",
                            fontSize = 11.sp,
                            color = Color(0xFFBF360C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            Text("Conceder Permiso GPS")
                        }
                    }
                }
            }

            // Option 1: Send current real static location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoadingLocation && currentLocationData != null) {
                        val loc = currentLocationData ?: return@clickable
                        onDismiss()
                        onSendLocation(loc.copy(isLive = false))
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
                        text = if (isLoadingLocation) {
                            "Obteniendo coordenadas GPS reales del dispositivo..."
                        } else {
                            val loc = currentLocationData
                            if (loc != null) {
                                "${loc.title} • Precisión ±${loc.accuracyMeters.toInt()}m\n${loc.address}"
                            } else {
                                "Coordenadas GPS no disponibles"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
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
                        text = "Transmite tus movimientos satelitales en directo mientras te desplazas",
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
                            text = "Selecciona la duración de transmisión en directo:",
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
                                val loc = currentLocationData ?: LocationData(
                                    latitude = 0.0,
                                    longitude = 0.0,
                                    title = "Ubicación en tiempo real",
                                    address = "Transmitiendo en directo por satélite",
                                    isLive = true
                                )
                                onDismiss()
                                onSendLocation(
                                    loc.copy(
                                        isLive = true,
                                        liveDurationMinutes = selectedDurationMinutes,
                                        remainingMinutes = selectedDurationMinutes,
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

    Canvas(modifier = modifier.fillMaxWidth().height(130.dp)) {
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

        // Center Pin position
        val center = Offset(w * 0.42f, h * 0.45f)

        if (isLive) {
            // Live Radar pulsing ring
            drawCircle(
                color = Color(0xFF00C853).copy(alpha = alphaRatio),
                radius = 45f * radiusRatio,
                center = center
            )
            drawCircle(
                color = Color(0xFF00C853).copy(alpha = 0.25f),
                radius = 28f,
                center = center
            )
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
