package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.StatusItem
import com.example.data.model.StatusType
import com.example.data.model.User

@Composable
fun CreateStatusDialog(
    initialType: StatusType = StatusType.TEXT,
    currentUser: User?,
    onDismiss: () -> Unit,
    onPublishStatus: (StatusItem) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Estado") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("¿Qué estás pensando?") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Visible durante 24 horas para tus contactos.")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        val item = StatusItem(
                            id = "status_${System.currentTimeMillis()}",
                            userId = currentUser?.userId ?: "me",
                            displayName = currentUser?.name ?: "Yo",
                            textContent = text,
                            type = StatusType.TEXT
                        )
                        onPublishStatus(item)
                    }
                }
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
