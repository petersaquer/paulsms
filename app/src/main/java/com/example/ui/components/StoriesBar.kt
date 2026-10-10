package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserStatusGroup
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@Composable
fun StoriesBar(
    currentUser: User?,
    statusGroups: List<UserStatusGroup>,
    onOpenStatusGroup: (group: UserStatusGroup) -> Unit,
    onCreateTextStatus: () -> Unit,
    onCreateMediaStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myGroup = statusGroups.find { it.isCurrentUser }
    val otherGroups = statusGroups.filter { !it.isCurrentUser }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // MY STATUS ITEM
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable {
                        if (myGroup != null && myGroup.statuses.isNotEmpty()) {
                            onOpenStatusGroup(myGroup)
                        } else {
                            onCreateTextStatus()
                        }
                    }
                    .testTag("my_status_story_item")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    val hasMyStatus = myGroup != null && myGroup.statuses.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .then(
                                if (hasMyStatus) {
                                    Modifier
                                        .border(
                                            width = 2.5.dp,
                                            brush = Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFF00C853),
                                                    Color(0xFF2AABEE),
                                                    Color(0xFF00C853)
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                        .padding(3.dp)
                                } else {
                                    Modifier
                                }
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(currentUser?.avatarGradientStart ?: 0xFF2AABEE),
                                        Color(currentUser?.avatarGradientEnd ?: 0xFF1976D2)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.displayName?.take(1) ?: "M").uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Plus badge to add status
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(TelegramAccent)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable { onCreateTextStatus() }
                            .testTag("add_status_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir estado",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (myGroup != null && myGroup.statuses.isNotEmpty()) "Mi estado (${myGroup.statuses.size})" else "Mi estado",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // OTHER CONTACTS' STATUSES
            otherGroups.forEach { group ->
                val hasReel = group.statuses.any { it.isReel }
                val hasUnseen = group.hasUnseen

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(68.dp)
                        .clickable { onOpenStatusGroup(group) }
                        .testTag("status_group_${group.userId}")
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.5.dp,
                                    brush = if (hasUnseen) {
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFFE91E63),
                                                Color(0xFFFF9800),
                                                Color(0xFF2AABEE),
                                                Color(0xFF00C853),
                                                Color(0xFFE91E63)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            )
                                        )
                                    },
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(group.userAvatarStart),
                                            Color(group.userAvatarEnd)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = group.displayName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (hasReel) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE91E63))
                                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = group.displayName.split(" ").firstOrNull() ?: group.username,
                        fontSize = 11.sp,
                        fontWeight = if (hasUnseen) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = if (hasUnseen) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
