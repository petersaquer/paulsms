package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AuditLogEntry(
    val id: String = "",
    val action: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val targetUserId: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
