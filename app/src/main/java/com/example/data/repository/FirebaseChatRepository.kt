package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.RealContactsHelper
import com.example.data.RealLocationHelper
import com.example.data.local.AppDatabase
import com.example.data.local.ChatEntity
import com.example.data.local.ContactEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AppUpdateInfo
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.DeviceContact
import com.example.data.model.Message
import com.example.data.model.GroupAccessConfig
import com.example.data.model.GroupAdminPermissions
import com.example.data.model.GroupConfig
import com.example.data.model.GroupGlobalPermissions
import com.example.data.model.GroupMember
import com.example.data.model.GroupRole
import com.example.data.model.JoinRequest
import com.example.data.model.AuditLogEntry
import com.example.data.model.Topic
import com.example.data.model.SavedContact
import com.example.data.model.StatusItem
import com.example.data.model.StatusPrivacy
import com.example.data.model.StatusViewer
import com.example.data.model.User
import com.example.data.model.UserRoles
import com.example.data.model.UserStatusGroup
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirebaseChatRepository(private val context: Context) {

    private val localDb: AppDatabase by lazy { AppDatabase.getInstance(context) }
    val chatDao by lazy { localDb.chatDao() }
    val messageDao by lazy { localDb.messageDao() }
    val contactDao by lazy { localDb.contactDao() }

    private val prefs: SharedPreferences = context.getSharedPreferences("paul_user_prefs", Context.MODE_PRIVATE)

    private val db: FirebaseFirestore by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "FirebaseApp init: ${e.message}")
        }

        try {
            val databaseId = context.getString(R.string.firestore_database_id)
            if (databaseId.isNotBlank()) {
                FirebaseFirestore.getInstance(databaseId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Database init error, falling back to default: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    private val auth: FirebaseAuth?
        get() = try {
            Firebase.auth
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Auth init error: ${e.message}")
            null
        }

    val locationHelper = RealLocationHelper(context)
    val contactsHelper = RealContactsHelper(context)

    // Retrieve saved user from prefs or Firebase Auth
    var activeUser: User?
        get() {
            try {
                val uid = prefs.getString("user_id", null) ?: auth?.currentUser?.uid ?: return null
                val displayName = prefs.getString("display_name", "") ?: ""
                val username = prefs.getString("username", "") ?: ""
                val email = prefs.getString("email", "") ?: ""
                val phone = prefs.getString("phone", "") ?: ""
                val bio = prefs.getString("bio", "") ?: ""
                val role = prefs.getString("role", UserRoles.USER) ?: UserRoles.USER
                val isBanned = prefs.getBoolean("is_banned", false)

                if (displayName.isEmpty() && username.isEmpty()) {
                    val fb = auth?.currentUser ?: return null
                    return User(
                        userId = fb.uid,
                        displayName = fb.displayName ?: "Usuario Paul",
                        username = "@" + (fb.displayName?.lowercase()?.replace(" ", "_") ?: "user_${fb.uid.take(4)}"),
                        email = fb.email ?: "",
                        phoneNumber = fb.phoneNumber ?: "",
                        bio = "Usuario en Paul Messenger",
                        role = prefs.getString("role", UserRoles.USER) ?: UserRoles.USER,
                        isBanned = false
                    )
                }

                return User(
                    userId = uid,
                    displayName = displayName,
                    username = username,
                    email = email,
                    phoneNumber = phone,
                    bio = bio,
                    role = role,
                    isBanned = isBanned,
                    isOnline = true
                )
            } catch (e: Exception) {
                Log.e("FirebaseChatRepo", "Error getting activeUser: ${e.message}")
                return null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit().clear().apply()
            } else {
                prefs.edit()
                    .putString("user_id", value.userId)
                    .putString("display_name", value.displayName)
                    .putString("username", value.username)
                    .putString("email", value.email)
                    .putString("phone", value.phoneNumber)
                    .putString("bio", value.bio)
                    .putString("role", value.role)
                    .putBoolean("is_banned", value.isBanned)
                    .apply()
            }
        }

    val currentUser: User? get() = activeUser

    fun isUserSignedIn(): Boolean = activeUser != null

    suspend fun getRegisteredUsersCount(): Int {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.size()
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Error counting users: ${e.message}")
            0
        }
    }

    /**
     * Account Registration.
     * Rule: The very first user to register receives the ADMIN role automatically!
     */
    suspend fun registerUser(
        displayName: String,
        username: String,
        email: String,
        passwordHash: String,
        phone: String,
        bio: String
    ): Result<User> {
        val cleanUser = username.trim().removePrefix("@").lowercase()
        if (cleanUser.length < 3) {
            return Result.failure(IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres."))
        }

        try {
            // Check if username is already taken (safe query)
            try {
                val existing = db.collection("users")
                    .whereEqualTo("username", cleanUser)
                    .limit(1)
                    .get()
                    .await()

                if (!existing.isEmpty) {
                    return Result.failure(IllegalArgumentException("El nombre de usuario @$cleanUser ya está registrado. Por favor elige otro."))
                }
            } catch (e: Exception) {
                Log.w("FirebaseChatRepo", "Check username query warning: ${e.message}")
            }

            val userCount = getRegisteredUsersCount()
            val assignedRole = if (userCount == 0) UserRoles.ADMIN else UserRoles.USER
            val generatedId = "user_" + UUID.randomUUID().toString().take(12)

            val newUser = User(
                userId = generatedId,
                displayName = displayName.trim().ifBlank { "@$cleanUser" },
                username = "@$cleanUser",
                email = email.trim(),
                phoneNumber = phone.trim(),
                bio = bio.ifBlank { "¡Hola! Estoy usando Paul Messenger." },
                role = assignedRole,
                isBanned = false,
                isBlocked = false,
                isOnline = true,
                avatarGradientStart = if (assignedRole == UserRoles.ADMIN) 0xFFFF6F00 else 0xFF2AABEE,
                avatarGradientEnd = if (assignedRole == UserRoles.ADMIN) 0xFFD84315 else 0xFF1976D2
            )

            val docData = mapOf(
                "userId" to newUser.userId,
                "displayName" to newUser.displayName,
                "username" to cleanUser,
                "email" to newUser.email,
                "passwordHash" to hashPassword(passwordHash),
                "phoneNumber" to newUser.phoneNumber,
                "bio" to newUser.bio,
                "role" to newUser.role,
                "isBanned" to false,
                "isBlocked" to false,
                "isOnline" to true,
                "avatarGradientStart" to newUser.avatarGradientStart,
                "avatarGradientEnd" to newUser.avatarGradientEnd,
                "createdAt" to newUser.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            activeUser = newUser

            try {
                db.collection("users").document(generatedId).set(docData).await()
            } catch (e: Exception) {
                Log.w("FirebaseChatRepo", "Saving user doc to firestore warning: ${e.message}")
            }

            return Result.success(newUser)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error registrando usuario: ${e.message}", e)
            val msg = e.localizedMessage ?: "No se pudo registrar la cuenta. Por favor intenta de nuevo."
            return Result.failure(Exception(msg))
        }
    }

    /**
     * Account Login with Username / Email & Password
     */
    suspend fun loginUser(
        identifier: String,
        passwordInput: String
    ): Result<User> {
        val trimmed = identifier.trim()
        val clean = trimmed.removePrefix("@").lowercase()
        val hashed = hashPassword(passwordInput)

        try {
            var snapshot = db.collection("users")
                .whereEqualTo("username", clean)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty && trimmed.contains("@")) {
                snapshot = db.collection("users")
                    .whereEqualTo("email", trimmed)
                    .limit(1)
                    .get()
                    .await()
            }

            if (snapshot.isEmpty) {
                return Result.failure(IllegalArgumentException("No se encontró ningún usuario con esas credenciales."))
            }

            val doc = snapshot.documents.first()
            val isBanned = doc.getBoolean("isBanned") ?: false
            if (isBanned) {
                return Result.failure(IllegalStateException("Esta cuenta ha sido suspendida por el Administrador."))
            }

            val storedPasswordHash = doc.getString("passwordHash")
            if (storedPasswordHash != null && storedPasswordHash != hashed) {
                return Result.failure(IllegalArgumentException("Contraseña incorrecta."))
            }

            val user = User(
                userId = doc.getString("userId") ?: doc.id,
                displayName = doc.getString("displayName") ?: "@$clean",
                username = "@" + (doc.getString("username") ?: clean),
                email = doc.getString("email") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                bio = doc.getString("bio") ?: "",
                role = doc.getString("role") ?: UserRoles.USER,
                isBanned = false,
                isOnline = true,
                avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF2AABEE,
                avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF1976D2
            )

            activeUser = user
            return Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Login error: ${e.message}")
            return Result.failure(e)
        }
    }

    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("paul_salt_$password".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Real-time list of all users for the Admin Panel (Crash-safe)
     */
    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseChatRepo", "Error observing users: ${error.message}")
                        return@addSnapshotListener
                    }

                    val userList = snapshot?.documents?.mapNotNull { doc ->
                        val cleanUsername = doc.getString("username") ?: ""
                        User(
                            userId = doc.getString("userId") ?: doc.id,
                            displayName = doc.getString("displayName") ?: "@$cleanUsername",
                            username = if (cleanUsername.startsWith("@")) cleanUsername else "@$cleanUsername",
                            email = doc.getString("email") ?: "",
                            phoneNumber = doc.getString("phoneNumber") ?: "",
                            bio = doc.getString("bio") ?: "",
                            role = doc.getString("role") ?: UserRoles.USER,
                            isBanned = doc.getBoolean("isBanned") ?: false,
                            isBlocked = doc.getBoolean("isBlocked") ?: false,
                            isOnline = doc.getBoolean("isOnline") ?: true,
                            avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF2AABEE,
                            avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF1976D2,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } ?: emptyList()

                    trySend(userList)
                }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "observeAllUsers exception: ${e.message}")
        }

        awaitClose { registration?.remove() }
    }

    /**
     * Deterministic Chat ID helper for Direct Messages
     */
    fun getDirectChatId(uid1: String, uid2: String): String {
        val u1 = uid1.trim().removePrefix("@").lowercase()
        val u2 = uid2.trim().removePrefix("@").lowercase()
        return if (u1 < u2) "direct_${u1}_${u2}" else "direct_${u2}_${u1}"
    }

    /**
     * Local Room Database Flow for Chats
     */
    fun getLocalChatsFlow(): Flow<List<Chat>> {
        return chatDao.getAllChatsFlow().map { entities -> entities.map { it.toChat() } }
    }

    /**
     * Local Room Database Flow for Messages
     */
    fun getLocalMessagesFlow(chatId: String): Flow<List<Message>> {
        return messageDao.getMessagesForChatFlow(chatId).map { entities -> entities.map { it.toMessage() } }
    }

    /**
     * Sync Chat to both local Room DB and Cloud Firestore
     */
    suspend fun syncChatToCloudAndLocal(chat: Chat, participantIds: List<String>) {
        try {
            // Save to local Room DB
            chatDao.insertOrUpdateChat(ChatEntity.fromChat(chat))

            // Sync to Firestore
            val currentUid = activeUser?.userId ?: ""
            val currentUsername = activeUser?.username ?: ""
            val allParticipants = (participantIds + currentUid + currentUsername)
                .map { it.trim().removePrefix("@").lowercase() }
                .filter { it.isNotBlank() }
                .distinct()

            val chatDoc = mapOf(
                "id" to chat.id,
                "title" to chat.title,
                "subtitle" to chat.subtitle,
                "username" to chat.username,
                "type" to chat.type.name,
                "lastMessage" to chat.lastMessage,
                "lastMessageTime" to chat.lastMessageTime,
                "participants" to allParticipants,
                "avatarGradientStart" to chat.avatarGradientStart,
                "avatarGradientEnd" to chat.avatarGradientEnd,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("chats").document(chat.id).set(chatDoc, SetOptions.merge())
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "syncChatToCloudAndLocal warning: ${e.message}")
        }
    }

    /**
     * Observe User Chats in Firestore (Real-time Cloud Sync + Local Persist)
     */
    fun observeUserChats(userId: String, username: String, scope: CoroutineScope): Flow<List<Chat>> = callbackFlow {
        if (userId.isBlank() && username.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val cleanUser = username.trim().removePrefix("@").lowercase()
        val cleanUid = userId.trim().lowercase()
        val searchTerms = listOf(cleanUid, cleanUser, "@$cleanUser").filter { it.isNotBlank() }.distinct()

        var reg1: ListenerRegistration? = null
        try {
            if (searchTerms.isNotEmpty()) {
                reg1 = db.collection("chats")
                    .whereArrayContainsAny("participants", searchTerms)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w("FirebaseChatRepo", "observeUserChats error: ${error.message}")
                            return@addSnapshotListener
                        }
                        val chats = snapshot?.documents?.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val participants = (doc.get("participants") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            val title = doc.getString("title") ?: "Chat"
                            val subtitle = doc.getString("subtitle") ?: "en línea"
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val lastTime = doc.getString("lastMessageTime") ?: ""
                            val rawType = doc.getString("type") ?: ChatType.DIRECT.name
                            val cType = try { ChatType.valueOf(rawType) } catch (e: Exception) { ChatType.DIRECT }
                            val rawUser = doc.getString("username") ?: ""
                            val partnerUser = participants.firstOrNull { it != cleanUid && it != cleanUser && it != "@$cleanUser" } ?: rawUser

                            Chat(
                                id = id,
                                title = title,
                                subtitle = subtitle,
                                username = if (partnerUser.startsWith("@")) partnerUser else "@$partnerUser",
                                type = cType,
                                lastMessage = lastMsg,
                                lastMessageTime = lastTime,
                                isCloudSynced = true,
                                avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF2AABEE,
                                avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF1976D2
                            )
                        } ?: emptyList()

                        // Save chats to local Room DB
                        if (chats.isNotEmpty()) {
                            scope.launch(Dispatchers.IO) {
                                chatDao.insertOrUpdateChats(chats.map { ChatEntity.fromChat(it) })
                            }
                        }

                        trySend(chats)
                    }
            }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "observeUserChats exception: ${e.message}")
        }
        awaitClose { reg1?.remove() }
    }

    /**
     * Real-time Messages for a Chat (Cloud Firestore + Local Room DB)
     */
    fun observeRealtimeMessages(chatId: String, scope: CoroutineScope): Flow<List<Message>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseChatRepo", "observeRealtimeMessages error: ${error.message}")
                        return@addSnapshotListener
                    }
                    val msgs = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Message::class.java)
                    } ?: emptyList()

                    // Save incoming cloud messages into local Room disk
                    if (msgs.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            messageDao.insertOrUpdateMessages(msgs.map { MessageEntity.fromMessage(it) })
                        }
                    }

                    trySend(msgs)
                }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Failed to observe messages: ${e.message}")
        }
        awaitClose { registration?.remove() }
    }

    /**
     * Sync Message to both local Room DB and Cloud Firestore
     */
    fun syncMessage(chatId: String, message: Message, scope: CoroutineScope) {
        // 1. Immediately save message to local Room DB on disk
        scope.launch(Dispatchers.IO) {
            try {
                messageDao.insertOrUpdateMessage(MessageEntity.fromMessage(message))
            } catch (e: Exception) {
                Log.w("FirebaseChatRepo", "Error saving message to Room DB: ${e.message}")
            }
        }

        // 2. Sync to Cloud Firestore
        try {
            db.collection("chats").document(chatId).collection("messages").document(message.id).set(message)
            
            // Also update last message on the chat document in Firestore
            val chatUpdates = mapOf(
                "lastMessage" to if (message.isViewOnce) "🔒 Mensaje de lectura única" else message.text,
                "lastMessageTime" to message.timestamp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("chats").document(chatId).set(chatUpdates, SetOptions.merge())
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "syncMessage error: ${e.message}")
        }
    }

    /**
     * Cloud Backup and Restore (WhatsApp-style)
     */
    suspend fun createCloudBackup(): Result<Unit> {
        val currentUid = activeUser?.userId ?: return Result.failure(IllegalStateException("No hay sesión activa"))
        return try {
            val localChats = chatDao.getAllChats()
            val localMsgs = messageDao.getAllMessages()

            val backupData = mapOf(
                "userId" to currentUid,
                "backupTimestamp" to System.currentTimeMillis(),
                "chatsCount" to localChats.size,
                "messagesCount" to localMsgs.size,
                "chats" to localChats,
                "messages" to localMsgs
            )

            db.collection("users").document(currentUid).collection("backups").document("latest_backup")
                .set(backupData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error creating backup: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun restoreCloudBackup(): Result<Int> {
        val currentUid = activeUser?.userId ?: return Result.failure(IllegalStateException("No hay sesión activa"))
        return try {
            val doc = db.collection("users").document(currentUid).collection("backups").document("latest_backup")
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(IllegalStateException("No se encontró ninguna copia de seguridad en la nube."))
            }

            val rawChats = doc.get("chats") as? List<Map<String, Any>> ?: emptyList()
            val rawMsgs = doc.get("messages") as? List<Map<String, Any>> ?: emptyList()

            // Restore into Room database
            var restoredCount = 0
            for (rawChat in rawChats) {
                val chatId = rawChat["id"] as? String ?: continue
                val entity = ChatEntity(
                    id = chatId,
                    title = rawChat["title"] as? String ?: "Chat",
                    subtitle = rawChat["subtitle"] as? String ?: "",
                    username = rawChat["username"] as? String ?: "",
                    type = rawChat["type"] as? String ?: ChatType.DIRECT.name,
                    lastMessage = rawChat["lastMessage"] as? String ?: "",
                    lastMessageTime = rawChat["lastMessageTime"] as? String ?: "",
                    unreadCount = (rawChat["unreadCount"] as? Long)?.toInt() ?: 0,
                    isOnline = rawChat["isOnline"] as? Boolean ?: false,
                    isPinned = rawChat["isPinned"] as? Boolean ?: false,
                    isMuted = rawChat["isMuted"] as? Boolean ?: false,
                    isVerified = rawChat["isVerified"] as? Boolean ?: false,
                    isEncrypted = rawChat["isEncrypted"] as? Boolean ?: false,
                    isCloudSynced = rawChat["isCloudSynced"] as? Boolean ?: true,
                    isBlocked = rawChat["isBlocked"] as? Boolean ?: false,
                    encryptionKeyFingerprint = rawChat["encryptionKeyFingerprint"] as? String,
                    contactPhone = rawChat["contactPhone"] as? String,
                    typingStatus = rawChat["typingStatus"] as? String,
                    memberCount = (rawChat["memberCount"] as? Long)?.toInt(),
                    avatarGradientStart = rawChat["avatarGradientStart"] as? Long ?: 0xFF2AABEE,
                    avatarGradientEnd = rawChat["avatarGradientEnd"] as? Long ?: 0xFF1976D2,
                    pinnedMessageText = rawChat["pinnedMessageText"] as? String
                )
                chatDao.insertOrUpdateChat(entity)
                restoredCount++
            }

            for (rawMsg in rawMsgs) {
                val msgId = rawMsg["id"] as? String ?: continue
                val chatId = rawMsg["chatId"] as? String ?: continue
                val msgEntity = MessageEntity(
                    id = msgId,
                    chatId = chatId,
                    text = rawMsg["text"] as? String ?: "",
                    timestamp = rawMsg["timestamp"] as? String ?: "",
                    isOutgoing = rawMsg["isOutgoing"] as? Boolean ?: false,
                    senderName = rawMsg["senderName"] as? String,
                    senderAvatarGradient = rawMsg["senderAvatarGradient"] as? Long,
                    status = rawMsg["status"] as? String ?: "READ",
                    type = rawMsg["type"] as? String ?: "TEXT",
                    isEncrypted = rawMsg["isEncrypted"] as? Boolean ?: false,
                    isViewOnce = rawMsg["isViewOnce"] as? Boolean ?: false,
                    hasBeenViewed = rawMsg["hasBeenViewed"] as? Boolean ?: false,
                    forwardedFrom = rawMsg["forwardedFrom"] as? String,
                    voiceDurationSeconds = (rawMsg["voiceDurationSeconds"] as? Long)?.toInt(),
                    photoUrl = rawMsg["photoUrl"] as? String,
                    videoUrl = rawMsg["videoUrl"] as? String,
                    fileUrl = rawMsg["fileUrl"] as? String,
                    fileName = rawMsg["fileName"] as? String,
                    fileSize = rawMsg["fileSize"] as? String,
                    contactName = rawMsg["contactName"] as? String,
                    contactPhone = rawMsg["contactPhone"] as? String,
                    contactUsername = rawMsg["contactUsername"] as? String,
                    gifUrl = rawMsg["gifUrl"] as? String,
                    stickerEmoji = rawMsg["stickerEmoji"] as? String
                )
                messageDao.insertOrUpdateMessage(msgEntity)
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error restoring backup: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * In-App Updates system
     */
    fun observeLatestAppUpdate(): Flow<AppUpdateInfo?> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("app_system").document("latest_update")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseChatRepo", "Error observing app updates: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val update = snapshot.toObject(AppUpdateInfo::class.java)
                        trySend(update)
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Error observing app updates: ${e.message}")
        }
        awaitClose { registration?.remove() }
    }

    suspend fun publishAppUpdate(
        versionName: String,
        versionCode: Int,
        apkUrl: String,
        changelog: String,
        isMandatory: Boolean
    ): Result<Unit> {
        val current = activeUser
        if (current?.isAdmin != true) {
            return Result.failure(SecurityException("Solo el Administrador puede publicar actualizaciones."))
        }

        val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val update = AppUpdateInfo(
            versionName = versionName.trim(),
            versionCode = versionCode,
            releaseDate = System.currentTimeMillis(),
            releaseDateFormatted = dateFormatted,
            apkDownloadUrl = apkUrl.trim(),
            changelog = changelog.trim(),
            isMandatory = isMandatory,
            publishedBy = current.displayName.ifBlank { current.username }
        )

        return try {
            db.collection("app_system").document("latest_update").set(update).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error publishing update: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Admin: Change user role (ADMIN, MODERATOR, USER)
     */
    suspend fun updateUserRole(targetUserId: String, newRole: String): Result<Unit> {
        val current = activeUser
        if (current?.isAdmin != true) {
            return Result.failure(SecurityException("Solo el Administrador Principal puede cambiar roles."))
        }
        return try {
            db.collection("users").document(targetUserId).update("role", newRole).await()
            if (targetUserId == current.userId) {
                activeUser = current.copy(role = newRole)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(newDisplayName: String, newBio: String): Result<Unit> {
        val current = activeUser ?: return Result.failure(IllegalStateException("No hay sesión activa"))
        return try {
            val updates = mapOf(
                "displayName" to newDisplayName.trim(),
                "bio" to newBio.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(current.userId).update(updates).await()
            activeUser = current.copy(displayName = newDisplayName.trim(), bio = newBio.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin/Moderator: Ban or Unban a user
     */
    suspend fun toggleUserBan(targetUserId: String, isBanned: Boolean): Result<Unit> {
        val current = activeUser
        if (current?.isModerator != true) {
            return Result.failure(SecurityException("No tienes permisos de moderador o administrador."))
        }
        return try {
            db.collection("users").document(targetUserId).update(
                mapOf(
                    "isBanned" to isBanned,
                    "isBlocked" to isBanned
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin: Delete user account completely
     */
    suspend fun deleteUserAccount(targetUserId: String): Result<Unit> {
        val current = activeUser
        if (current?.isAdmin != true) {
            return Result.failure(SecurityException("Solo el Administrador puede eliminar cuentas."))
        }
        return try {
            db.collection("users").document(targetUserId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search users in Firestore by @username, email or phone
     * Uses real Firestore queries and merges results without duplicates.
     */
    suspend fun searchUsersInFirestore(query: String): List<User> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val cleanQuery = trimmed.removePrefix("@").lowercase()
        val currentUserId = activeUser?.userId ?: ""
        val resultsMap = mutableMapOf<String, User>()

        fun mapDocToUser(doc: com.google.firebase.firestore.DocumentSnapshot): User? {
            val uId = doc.getString("userId") ?: doc.id
            if (uId == currentUserId) return null
            val isBanned = doc.getBoolean("isBanned") ?: false
            if (isBanned) return null

            val rawUsername = doc.getString("username") ?: ""
            val formattedUsername = if (rawUsername.startsWith("@")) rawUsername else "@$rawUsername"

            return User(
                userId = uId,
                displayName = doc.getString("displayName") ?: formattedUsername,
                username = formattedUsername,
                email = doc.getString("email") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                bio = doc.getString("bio") ?: "Usuario en Paul Messenger",
                role = doc.getString("role") ?: UserRoles.USER,
                isBanned = false,
                isOnline = doc.getBoolean("isOnline") ?: true,
                isFirebaseUser = true,
                avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF2AABEE,
                avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF1976D2
            )
        }

        try {
            // 1. Query by exact username or prefix
            val byUser = db.collection("users")
                .whereGreaterThanOrEqualTo("username", cleanQuery)
                .whereLessThanOrEqualTo("username", cleanQuery + "\uf8ff")
                .limit(20)
                .get()
                .await()
            for (doc in byUser.documents) {
                mapDocToUser(doc)?.let { resultsMap[it.userId] = it }
            }

            // 2. Query by email prefix or exact email
            val byEmail = db.collection("users")
                .whereGreaterThanOrEqualTo("email", cleanQuery)
                .whereLessThanOrEqualTo("email", cleanQuery + "\uf8ff")
                .limit(20)
                .get()
                .await()
            for (doc in byEmail.documents) {
                mapDocToUser(doc)?.let { resultsMap[it.userId] = it }
            }

            // 3. Exact email query if contains @
            if (trimmed.contains("@")) {
                val exactEmail = db.collection("users")
                    .whereEqualTo("email", trimmed.lowercase())
                    .limit(5)
                    .get()
                    .await()
                for (doc in exactEmail.documents) {
                    mapDocToUser(doc)?.let { resultsMap[it.userId] = it }
                }
            }

            // 4. Query by phone if digits
            val cleanPhone = trimmed.replace(" ", "").replace("-", "")
            if (cleanPhone.length >= 5 && cleanPhone.any { it.isDigit() }) {
                val byPhone = db.collection("users")
                    .whereEqualTo("phoneNumber", cleanPhone)
                    .limit(5)
                    .get()
                    .await()
                for (doc in byPhone.documents) {
                    mapDocToUser(doc)?.let { resultsMap[it.userId] = it }
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Error searching users in Firestore: ${e.message}")
        }

        return resultsMap.values.toList()
    }

    suspend fun searchUserInFirebase(query: String): User? {
        return searchUsersInFirestore(query).firstOrNull()
    }

    /**
     * Local Room Flow for Saved Contacts
     */
    fun getLocalContactsFlow(): Flow<List<SavedContact>> {
        return contactDao.getAllContactsFlow().map { entities -> entities.map { it.toSavedContact() } }
    }

    /**
     * Contact persistence in Firestore and Room DB (users/{userId}/saved_contacts)
     */
    fun observeSavedContacts(userId: String, scope: CoroutineScope): Flow<List<SavedContact>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("users").document(userId)
                .collection("saved_contacts")
                .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseChatRepo", "Error observing saved contacts: ${error.message}")
                        return@addSnapshotListener
                    }
                    val contacts = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(SavedContact::class.java)
                    } ?: emptyList()

                    if (contacts.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            contactDao.insertOrUpdateContacts(contacts.map { ContactEntity.fromSavedContact(it) })
                        }
                    }

                    trySend(contacts)
                }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "observeSavedContacts exception: ${e.message}")
        }
        awaitClose { registration?.remove() }
    }

    suspend fun addSavedContact(contactUser: User): Result<Unit> {
        val current = activeUser
        val safeContactId = contactUser.userId.ifBlank { contactUser.username.removePrefix("@") }.ifBlank { "contact_${UUID.randomUUID().toString().take(8)}" }
        val contact = SavedContact(
            contactUserId = safeContactId,
            displayName = contactUser.name,
            username = contactUser.username,
            email = contactUser.email,
            phoneNumber = contactUser.phoneNumber,
            bio = contactUser.bio,
            avatarGradientStart = contactUser.avatarGradientStart,
            avatarGradientEnd = contactUser.avatarGradientEnd,
            role = contactUser.role,
            isOnline = contactUser.isOnline,
            addedAt = System.currentTimeMillis()
        )

        // Always save to local Room DB immediately
        try {
            contactDao.insertOrUpdateContact(ContactEntity.fromSavedContact(contact))
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Error saving contact to local Room DB: ${e.message}")
        }

        if (current != null && current.userId.isNotBlank()) {
            try {
                db.collection("users").document(current.userId)
                    .collection("saved_contacts").document(safeContactId)
                    .set(contact)
                    .await()
            } catch (e: Exception) {
                Log.w("FirebaseChatRepo", "Error syncing contact to Firestore: ${e.message}")
            }
        }
        return Result.success(Unit)
    }

    suspend fun removeSavedContact(contactUserId: String): Result<Unit> {
        val current = activeUser

        try {
            contactDao.deleteContact(contactUserId)
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Error deleting contact from Room DB: ${e.message}")
        }

        if (current != null && current.userId.isNotBlank()) {
            try {
                db.collection("users").document(current.userId)
                    .collection("saved_contacts").document(contactUserId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.w("FirebaseChatRepo", "Error deleting contact from Firestore: ${e.message}")
            }
        }
        return Result.success(Unit)
    }

    // ==========================================
    // WHATSAPP-STYLE ESTADOS / REELS / STORIES
    // ==========================================

    /**
     * Publishes a new status / reel with privacy rules
     */
    suspend fun createStatus(status: StatusItem): Result<Unit> {
        return try {
            db.collection("statuses").document(status.id).set(status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error creating status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time active statuses stream (filters expired and respects privacy)
     */
    fun observeActiveStatuses(): Flow<List<StatusItem>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("statuses")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseChatRepo", "Error observing statuses: ${error.message}")
                        return@addSnapshotListener
                    }
                    val now = System.currentTimeMillis()
                    val currentUid = activeUser?.userId ?: ""
                    val all = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(StatusItem::class.java)
                    } ?: emptyList()

                    val filtered = all.filter { status ->
                        status.expiresAt > now && status.canUserView(currentUid)
                    }
                    trySend(filtered)
                }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "observeActiveStatuses error: ${e.message}")
        }
        awaitClose { registration?.remove() }
    }

    /**
     * Mark status as viewed by the current user
     */
    suspend fun recordStatusView(statusId: String, viewer: StatusViewer) {
        try {
            val docRef = db.collection("statuses").document(statusId)
            val doc = docRef.get().await()
            if (!doc.exists()) return

            val currentViewers = doc.toObject(StatusItem::class.java)?.viewers ?: emptyList()
            if (currentViewers.none { it.userId == viewer.userId }) {
                docRef.update("viewers", FieldValue.arrayUnion(viewer)).await()
            }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Error recording status view: ${e.message}")
        }
    }

    /**
     * React to a status (❤️, 🔥, 😂, etc.)
     */
    suspend fun reactToStatus(statusId: String, emoji: String) {
        val currentUid = activeUser?.userId ?: return
        try {
            db.collection("statuses").document(statusId)
                .update("reactions.$currentUid", emoji)
                .await()
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Error reacting to status: ${e.message}")
        }
    }

    /**
     * Delete status
     */
    suspend fun deleteStatus(statusId: String): Result<Unit> {
        return try {
            db.collection("statuses").document(statusId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun onGoogleSignInClicked(
        activity: Activity,
        onAuthSuccess: (User) -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onAuthError("Firebase Auth no disponible")
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onAuthError("Falta default_web_client_id en configuración")
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    authInstance.signInWithCredential(authCredential).await()
                    val fb = authInstance.currentUser
                    if (fb != null) {
                        val count = getRegisteredUsersCount()
                        val existingDoc = db.collection("users").document(fb.uid).get().await()
                        val role = if (existingDoc.exists()) {
                            existingDoc.getString("role") ?: UserRoles.USER
                        } else {
                            if (count == 0) UserRoles.ADMIN else UserRoles.USER
                        }

                        val cleanUsername = (fb.displayName?.lowercase()?.replace(" ", "_") ?: "user_${fb.uid.take(4)}")
                        val loggedUser = User(
                            userId = fb.uid,
                            displayName = fb.displayName ?: "Usuario Google",
                            username = "@$cleanUsername",
                            email = fb.email ?: "",
                            phoneNumber = fb.phoneNumber ?: "",
                            role = role,
                            bio = "Disponible"
                        )

                        val docData = mapOf(
                            "userId" to fb.uid,
                            "displayName" to loggedUser.displayName,
                            "username" to cleanUsername,
                            "email" to loggedUser.email,
                            "role" to role,
                            "bio" to loggedUser.bio,
                            "isOnline" to true,
                            "avatarGradientStart" to loggedUser.avatarGradientStart,
                            "avatarGradientEnd" to loggedUser.avatarGradientEnd,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                        db.collection("users").document(fb.uid).set(docData, SetOptions.merge()).await()
                        activeUser = loggedUser
                        onAuthSuccess(loggedUser)
                    }
                } else {
                    onAuthError("Tipo de credencial no compatible")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelado: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Fallo en Google Sign-In", e)
                onAuthError(e.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    // ==========================================
    // ADVANCED GROUPS & RBAC MANAGEMENT
    // ==========================================

    suspend fun createGroupChat(
        title: String,
        description: String,
        isPublic: Boolean,
        alias: String,
        initialMembers: List<User>
    ): Result<Chat> {
        val current = activeUser ?: return Result.failure(IllegalStateException("No hay sesión activa"))
        val groupId = "group_" + UUID.randomUUID().toString().take(10)
        val cleanAlias = alias.trim().removePrefix("@").lowercase()

        val ownerMember = GroupMember(
            userId = current.userId,
            username = current.username,
            displayName = current.displayName,
            role = GroupRole.OWNER,
            customTitle = "Propietario",
            joinedAt = System.currentTimeMillis()
        )

        val memberList = mutableListOf(ownerMember)
        initialMembers.forEach { u ->
            if (u.userId != current.userId) {
                memberList.add(
                    GroupMember(
                        userId = u.userId,
                        username = u.username,
                        displayName = u.displayName,
                        role = GroupRole.MEMBER,
                        joinedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        val allParticipants = memberList.map { it.userId.ifBlank { it.username } }.filter { it.isNotBlank() }.distinct()

        val accessConfig = GroupAccessConfig(
            isPublic = isPublic,
            publicAlias = if (isPublic && cleanAlias.isNotBlank()) "@$cleanAlias" else "",
            inviteLink = "https://paul.chat/join/$groupId"
        )

        val initialGroupConfig = GroupConfig(
            groupId = groupId,
            description = description.ifBlank { "Grupo oficial en Paul Messenger" },
            ownerId = current.userId,
            members = memberList,
            accessConfig = accessConfig,
            auditLog = listOf(
                AuditLogEntry(
                    id = "log_" + UUID.randomUUID().toString().take(6),
                    adminUserId = current.userId,
                    adminName = current.displayName,
                    actionType = "CREATE_GROUP",
                    description = "Grupo creado por ${current.displayName}"
                )
            )
        )

        val newChat = Chat(
            id = groupId,
            title = title.trim(),
            subtitle = "${memberList.size} miembros • ${if (isPublic) "Público" else "Privado"}",
            username = if (isPublic && cleanAlias.isNotBlank()) "@$cleanAlias" else "",
            type = ChatType.GROUP,
            lastMessage = "Grupo '$title' creado por ${current.displayName}",
            lastMessageTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            unreadCount = 0,
            isOnline = true,
            isCloudSynced = true,
            memberCount = memberList.size,
            avatarGradientStart = 0xFF7C4DFF,
            avatarGradientEnd = 0xFF536DFE,
            groupConfig = initialGroupConfig
        )

        try {
            // 1. Local Room DB
            chatDao.insertOrUpdateChat(ChatEntity.fromChat(newChat))

            // 2. Cloud Firestore
            val chatDoc = mapOf(
                "id" to newChat.id,
                "title" to newChat.title,
                "subtitle" to newChat.subtitle,
                "username" to newChat.username,
                "type" to ChatType.GROUP.name,
                "lastMessage" to newChat.lastMessage,
                "lastMessageTime" to newChat.lastMessageTime,
                "participants" to allParticipants,
                "memberCount" to newChat.memberCount,
                "avatarGradientStart" to newChat.avatarGradientStart,
                "avatarGradientEnd" to newChat.avatarGradientEnd,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("chats").document(groupId).set(chatDoc).await()
            db.collection("chats").document(groupId).collection("group_config").document("config").set(initialGroupConfig).await()

            return Result.success(newChat)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error creating group: ${e.message}", e)
            return Result.success(newChat) // Local fallback
        }
    }

    fun observeGroupConfig(groupId: String): Flow<GroupConfig?> = callbackFlow {
        if (groupId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("chats").document(groupId)
                .collection("group_config").document("config")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseChatRepo", "Error observing group config: ${error.message}")
                        return@addSnapshotListener
                    }
                    val config = snapshot?.toObject(GroupConfig::class.java)
                    trySend(config)
                }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "observeGroupConfig exception: ${e.message}")
        }
        awaitClose { registration?.remove() }
    }

    suspend fun saveGroupConfig(config: GroupConfig): Result<Unit> {
        return try {
            db.collection("chats").document(config.groupId)
                .collection("group_config").document("config")
                .set(config)
                .await()

            // Update participant list and member count on main chat document
            val currentUids = config.members.map { it.userId.ifBlank { it.username } }.filter { it.isNotBlank() }
            val updates = mapOf(
                "participants" to currentUids,
                "memberCount" to config.members.size,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("chats").document(config.groupId).set(updates, SetOptions.merge()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error saving group config: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun banUserFromGroup(groupId: String, targetUserId: String, massDeleteMessages: Boolean): Result<Unit> {
        val current = activeUser ?: return Result.failure(IllegalStateException("No hay sesión activa"))
        try {
            val doc = db.collection("chats").document(groupId)
                .collection("group_config").document("config").get().await()
            val config = doc.toObject(GroupConfig::class.java) ?: return Result.failure(IllegalStateException("Configuración de grupo no encontrada"))

            val updatedMembers = config.members.filterNot { it.userId == targetUserId || it.username == targetUserId }
            val updatedBanned = (config.bannedUserIds + targetUserId).distinct()
            val newAudit = config.auditLog + AuditLogEntry(
                id = "log_" + UUID.randomUUID().toString().take(6),
                adminUserId = current.userId,
                adminName = current.displayName,
                actionType = "BAN_USER",
                description = "Usuario $targetUserId baneado por ${current.displayName}",
                targetUserId = targetUserId
            )

            val newConfig = config.copy(members = updatedMembers, bannedUserIds = updatedBanned, auditLog = newAudit)
            saveGroupConfig(newConfig)

            if (massDeleteMessages) {
                val msgs = db.collection("chats").document(groupId).collection("messages")
                    .whereEqualTo("senderId", targetUserId)
                    .get()
                    .await()
                for (m in msgs.documents) {
                    m.reference.delete()
                }
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun pinMessageInChat(chatId: String, messageId: String, pin: Boolean): Result<Unit> {
        return try {
            db.collection("chats").document(chatId)
                .collection("messages").document(messageId)
                .update("isPinned", pin)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLocalMessage(messageId: String) {
        try {
            messageDao.deleteMessage(messageId)
        } catch (e: Exception) {
            Log.w("Repository", "Delete local message error: ${e.message}")
        }
    }

    suspend fun clearMessagesForChat(chatId: String) {
        try {
            messageDao.deleteMessagesForChat(chatId)
        } catch (e: Exception) {
            Log.w("Repository", "Clear local messages error: ${e.message}")
        }
    }

    suspend fun saveAuditLog(entry: AuditLogEntry) {
        try {
            db.collection("audit_logs").document(entry.id).set(entry).await()
        } catch (e: Exception) {
            Log.w("Repository", "Save audit log error: ${e.message}")
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w("Auth", "Sign out error: ${e.message}")
        }
        activeUser = null
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Error al limpiar credenciales", e)
            } finally {
                onComplete()
            }
        }
    }
}
