package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun VoicePlayer(
    durationSeconds: Int,
    waveform: List<Float>?,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val totalMillis = (durationSeconds.coerceAtLeast(1)) * 1000L
            val stepMillis = 100L
            val steps = (totalMillis / stepMillis).coerceAtLeast(1)
            val toneGenerator = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 70) }.getOrNull()

            try {
                for (i in 0..steps) {
                    if (!isPlaying) break
                    progress = i.toFloat() / steps

                    // Reproducir tono de audio en vivo durante la reproducción de la nota de voz
                    if (i % 3L == 0L) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                    }
                    delay(stepMillis)
                }
            } finally {
                toneGenerator?.release()
            }
            isPlaying = false
            progress = 0f
        }
    }

    val bars = waveform ?: remember {
        listOf(0.3f, 0.6f, 0.8f, 0.4f, 0.9f, 0.5f, 0.7f, 0.3f, 0.8f, 0.6f, 0.4f, 0.7f, 0.5f, 0.2f)
    }

    val accentColor = if (isOutgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play / Pause Circle
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accentColor)
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Waveform bars
            Row(
                modifier = Modifier
                    .height(26.dp)
                    .clickable {
                        // Toggle play
                        isPlaying = !isPlaying
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.5.dp)
            ) {
                bars.forEachIndexed { index, heightRatio ->
                    val barProgressThreshold = index.toFloat() / bars.size
                    val isPlayed = progress >= barProgressThreshold
                    val barColor = if (isPlayed) accentColor else accentColor.copy(alpha = 0.35f)

                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((heightRatio * 22).dp.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(barColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Time indicator
            val currentSeconds = (progress * durationSeconds).roundToInt()
            val timeText = if (isPlaying) {
                String.format("%02d:%02d", currentSeconds / 60, currentSeconds % 60)
            } else {
                String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
