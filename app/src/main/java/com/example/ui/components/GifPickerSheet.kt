package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

data class GifItem(
    val id: String,
    val title: String,
    val url: String,
    val category: String,
    val previewColor: Long = 0xFF1976D2
)

object SampleGifs {
    val items = listOf(
        GifItem(
            id = "g1",
            title = "Celebración Confeti 🎉",
            url = "https://media.giphy.com/media/artj92V8o75VPL7AeQ/giphy.gif",
            category = "Celebración",
            previewColor = 0xFF673AB7
        ),
        GifItem(
            id = "g2",
            title = "Gatito bailando 🐱",
            url = "https://media.giphy.com/media/JIX9t2j0ZTN9S/giphy.gif",
            category = "Gatos",
            previewColor = 0xFFFF9800
        ),
        GifItem(
            id = "g3",
            title = "Pulgar arriba 👍",
            url = "https://media.giphy.com/media/111ebonMs90YLu/giphy.gif",
            category = "Reacciones",
            previewColor = 0xFF00C853
        ),
        GifItem(
            id = "g4",
            title = "Hola saludando 👋",
            url = "https://media.giphy.com/media/dzaUX7CAG0Ihi/giphy.gif",
            category = "Reacciones",
            previewColor = 0xFF2196F3
        ),
        GifItem(
            id = "g5",
            title = "Programador tecleando 💻",
            url = "https://media.giphy.com/media/unQ3IJU2RG7DO/giphy.gif",
            category = "Memes",
            previewColor = 0xFF3F51B5
        ),
        GifItem(
            id = "g6",
            title = "Risa carcajada 😂",
            url = "https://media.giphy.com/media/10JhviFuU2gWD6/giphy.gif",
            category = "Reacciones",
            previewColor = 0xFFE91E63
        ),
        GifItem(
            id = "g7",
            title = "Mente explotando 🤯",
            url = "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif",
            category = "Memes",
            previewColor = 0xFFFF5722
        ),
        GifItem(
            id = "g8",
            title = "Amor corazón ❤️",
            url = "https://media.giphy.com/media/l4pTdcifPZLpDjL1e/giphy.gif",
            category = "Amor",
            previewColor = 0xFFE91E63
        ),
        GifItem(
            id = "g9",
            title = "Cohete despegando 🚀",
            url = "https://media.giphy.com/media/mi6DsSSNKDbUY/giphy.gif",
            category = "Celebración",
            previewColor = 0xFF00BCD4
        ),
        GifItem(
            id = "g10",
            title = "Gato sorprendido 🙀",
            url = "https://media.giphy.com/media/mlvseq9yvZhba/giphy.gif",
            category = "Gatos",
            previewColor = 0xFFFFB300
        ),
        GifItem(
            id = "g11",
            title = "Baile feliz 🕺",
            url = "https://media.giphy.com/media/blSTtZehjAZ8I/giphy.gif",
            category = "Celebración",
            previewColor = 0xFF9C27B0
        ),
        GifItem(
            id = "g12",
            title = "Todo bien perro 🐶",
            url = "https://media.giphy.com/media/9M5jK4GXmD5o1irGrF/giphy.gif",
            category = "Memes",
            previewColor = 0xFF795548
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GifPickerSheet(
    onDismiss: () -> Unit,
    onSelectGif: (title: String, url: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Reacciones", "Celebración", "Memes", "Gatos", "Amor")

    val filteredGifs = remember(searchQuery, selectedCategory) {
        SampleGifs.items.filter { gif ->
            val matchesCategory = if (selectedCategory == "Todos") true else gif.category == selectedCategory
            val matchesSearch = if (searchQuery.isBlank()) true else {
                gif.title.contains(searchQuery, ignoreCase = true) ||
                        gif.category.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
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
                .padding(bottom = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Gif,
                    contentDescription = null,
                    tint = TelegramAccent,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GIFs de Paul",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar GIFs en vivo...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("gif_search_field")
            )

            // Category Chips
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TelegramBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of GIFs
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(filteredGifs, key = { it.id }) { gif ->
                    CardGifItem(
                        gif = gif,
                        onClick = {
                            onDismiss()
                            onSelectGif(gif.title, gif.url)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardGifItem(
    gif: GifItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(gif.previewColor).copy(alpha = 0.35f))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gif.url)
                .crossfade(true)
                .build(),
            contentDescription = gif.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Gradient overlay with title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TelegramAccent)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "GIF",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = gif.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
