package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Componente oficial del logo de Paul Messenger:
 * Muestra el logo original de la aplicación instalada (Letra P con un pajarito sobre degradado azul).
 */
@Composable
fun PaulAppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showShadow: Boolean = false,
    contentDescription: String = "Paul Messenger Logo"
) {
    val baseModifier = if (showShadow) {
        modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
    } else {
        modifier
            .size(size)
            .clip(CircleShape)
    }

    Image(
        painter = painterResource(id = R.drawable.ic_paul_logo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = baseModifier
    )
}
