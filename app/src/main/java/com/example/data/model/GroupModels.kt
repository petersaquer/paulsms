package com.example.data.model

enum class GroupRole {
    OWNER,
    ADMIN,
    MEMBER,
    RESTRICTED
}

data class GroupAdminPermissions(
    val canEditInfo: Boolean = true,
    val canDeleteMessages: Boolean = true,
    val canBanUsers: Boolean = true,
    val canAddAdmins: Boolean = false,
    val canPinMessages: Boolean = true,
    val canManageLinks: Boolean = true,
    val canApproveMembers: Boolean = true,
    val canPostAnonymously: Boolean = false
)

data class GroupMember(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val role: GroupRole = GroupRole.MEMBER,
    val adminPermissions: GroupAdminPermissions = GroupAdminPermissions(),
    val customTitle: String = "",
    val isMuted: Boolean = false,
    val muteUntilTimestamp: Long = 0L, // 0 = not muted, -1 = permanent, >0 = millis
    val canSendText: Boolean = true,
    val canSendMedia: Boolean = true,
    val canSendStickersGifs: Boolean = true,
    val canEmbedLinks: Boolean = true,
    val canCreatePolls: Boolean = true,
    val canAddMembers: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis()
) {
    val roleLabel: String get() = when (role) {
        GroupRole.OWNER -> "Propietario"
        GroupRole.ADMIN -> customTitle.ifBlank { "Administrador" }
        GroupRole.RESTRICTED -> "Restringido"
        GroupRole.MEMBER -> customTitle.ifBlank { "Miembro" }
    }
}

data class GroupGlobalPermissions(
    val allowText: Boolean = true,
    val allowMedia: Boolean = true,
    val allowStickersGifs: Boolean = true,
    val allowLinkPreviews: Boolean = true,
    val allowPolls: Boolean = true,
    val allowAddMembers: Boolean = true,
    val slowModeSeconds: Int = 0, // 0 (off), 5, 10, 30, 60, 300, 900, 3600
    val reactionsMode: String = "ALL", // "ALL", "LIMITED", "DISABLED"
    val allowMassMentions: Boolean = true // @todos, @admin
)

data class GroupAccessConfig(
    val isPublic: Boolean = false,
    val publicAlias: String = "", // e.g. "android_community" -> @android_community
    val inviteLink: String = "",
    val inviteLinkMaxUses: Int = 0, // 0 = unlimited
    val inviteLinkUsedCount: Int = 0,
    val inviteLinkExpiresAt: Long = 0L, // 0 = never
    val requireAdminApproval: Boolean = false,
    val newMemberHistoryVisible: Boolean = true,
    val protectedContent: Boolean = false // Block forwarding, copying, saving
)

data class JoinRequest(
    val requestId: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val requestedAt: Long = System.currentTimeMillis()
)

data class AuditLogEntry(
    val id: String = "",
    val adminUserId: String = "",
    val adminName: String = "",
    val actionType: String = "", // "DELETE_MSG", "BAN_USER", "MUTE_USER", "EDIT_PERMISSIONS", "PIN_MSG", "TRANSFER_OWNERSHIP", etc.
    val description: String = "",
    val targetUserId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Topic(
    val id: String = "",
    val title: String = "",
    val iconEmoji: String = "💬",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class GroupConfig(
    val groupId: String = "",
    val description: String = "Grupo de conversación pública y privada en Paul Messenger.",
    val ownerId: String = "",
    val members: List<GroupMember> = emptyList(),
    val bannedUserIds: List<String> = emptyList(),
    val globalPermissions: GroupGlobalPermissions = GroupGlobalPermissions(),
    val accessConfig: GroupAccessConfig = GroupAccessConfig(),
    val joinRequests: List<JoinRequest> = emptyList(),
    val auditLog: List<AuditLogEntry> = emptyList(),
    val topics: List<Topic> = emptyList()
)
