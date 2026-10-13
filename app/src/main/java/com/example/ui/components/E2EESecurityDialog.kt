package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun E2EESecurityDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seguridad y Encriptación E2EE") },
        text = { Text("Tus mensajes, notas de voz y llamadas están protegidos con encriptación de extremo a extremo. Solo tú y el destinatario pueden leerlos.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}
