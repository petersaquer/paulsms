package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SavedContact

@Entity(tableName = "saved_contacts")
data class ContactEntity(
    @PrimaryKey val contactUserId: String,
    val displayName: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val bio: String,
    val avatarGradientStart: Long,
    val avatarGradientEnd: Long,
    val role: String,
    val isOnline: Boolean,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toSavedContact(): SavedContact = SavedContact(
        contactUserId = contactUserId,
        displayName = displayName,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        bio = bio,
        avatarGradientStart = avatarGradientStart,
        avatarGradientEnd = avatarGradientEnd,
        role = role,
        isOnline = isOnline,
        addedAt = addedAt
    )

    companion object {
        fun fromSavedContact(c: SavedContact): ContactEntity = ContactEntity(
            contactUserId = c.contactUserId,
            displayName = c.name,
            username = c.username,
            email = c.email,
            phoneNumber = c.phoneNumber,
            bio = c.bio,
            avatarGradientStart = c.avatarGradientStart,
            avatarGradientEnd = c.avatarGradientEnd,
            role = c.role,
            isOnline = c.isOnline,
            addedAt = c.addedAt
        )
    }
}
