package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class GroupConfig(
    val groupId: String = "",
    val allowMemberMessages: Boolean = true,
    val allowMediaSharing: Boolean = true,
    val allowAddMembers: Boolean = true,
    val slowModeSeconds: Int = 0,
    val bannedUserIds: List<String> = emptyList(),
    val adminUserIds: List<String> = emptyList()
)
