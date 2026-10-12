package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedContact
import com.example.data.model.StatusPrivacy
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@Composable
fun StatusPrivacyDialog(
    currentPrivacy: StatusPrivacy,
    initialExcludedIds: List<String>,
    initialIncludedIds: List<String>,
    availableContacts: List<SavedContact>,
    onDismiss: () -> Unit,
    onSavePrivacy: (privacy: StatusPrivacy, excludedIds: List<String>, includedIds: List<String>) -> Unit
) {
    var selectedPrivacy by remember { mutableStateOf(currentPrivacy) }
    var excludedIds by remember { mutableStateOf(initialExcludedIds.toSet()) }
    var includedIds by remember { mutableStateOf(initialIncludedIds.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Privacidad de estados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "¿Quién puede ver mis actualizaciones de estado y reels?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                StatusPrivacy.entries.forEach { option ->
                    val isSelected = selectedPrivacy == option
                    Surface(
                        onClick = { selectedPrivacy = option },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) TelegramBlue.copy(alpha = 0.08f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("privacy_option_${option.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPrivacy = option },
                                colors = RadioButtonDefaults.colors(selectedColor = TelegramBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = when (option) {
                                    StatusPrivacy.MY_CONTACTS -> Icons.Default.Group
                                    StatusPrivacy.MY_CONTACTS_EXCEPT -> Icons.Default.PersonRemove
                                    StatusPrivacy.ONLY_SHARE_WITH -> Icons.Default.Star
                                    StatusPrivacy.PUBLIC -> Icons.Default.Public
                                },
                                contentDescription = null,
                                tint = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = option.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // If "Mis contactos excepto..." or "Solo compartir con..." show selection list
                if ((selectedPrivacy == StatusPrivacy.MY_CONTACTS_EXCEPT || selectedPrivacy == StatusPrivacy.ONLY_SHARE_WITH) && availableContacts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    val isExcept = selectedPrivacy == StatusPrivacy.MY_CONTACTS_EXCEPT
                    Text(
                        text = if (isExcept) "Selecciona los contactos a ocultar:" else "Selecciona quiénes pueden ver:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        items(availableContacts, key = { it.contactUserId }) { contact ->
                            val isChecked = if (isExcept) {
                                excludedIds.contains(contact.contactUserId)
                            } else {
                                includedIds.contains(contact.contactUserId)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isExcept) {
                                            excludedIds = if (isChecked) excludedIds - contact.contactUserId else excludedIds + contact.contactUserId
                                        } else {
                                            includedIds = if (isChecked) includedIds - contact.contactUserId else includedIds + contact.contactUserId
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (isExcept) {
                                            excludedIds = if (checked) excludedIds + contact.contactUserId else excludedIds - contact.contactUserId
                                        } else {
                                            includedIds = if (checked) includedIds + contact.contactUserId else includedIds - contact.contactUserId
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = TelegramBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = contact.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = contact.username,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSavePrivacy(selectedPrivacy, excludedIds.toList(), includedIds.toList())
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_privacy_button")
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold, color = TelegramBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
