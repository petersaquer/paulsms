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
import com.example.data.model.AppUpdateInfo
import com.example.data.model.Chat
import com.example.data.model.DeviceContact
import com.example.data.model.Message
import com.example.data.model.User
import com.example.data.model.UserRoles
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirebaseChatRepository(private val context: Context) {

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
            // Check if username is already taken
            val existing = db.collection("users")
                .whereEqualTo("username", cleanUser)
                .limit(1)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(IllegalArgumentException("El nombre de usuario @$cleanUser ya está en uso. Elige otro."))
            }

            val userCount = getRegisteredUsersCount()
            // THE FIRST REGISTERED USER BECOMES ADMIN!
            val assignedRole = if (userCount == 0) UserRoles.ADMIN else UserRoles.USER
            val generatedId = "user_" + UUID.randomUUID().toString().take(12)

            val newUser = User(
                userId = generatedId,
                displayName = displayName.trim(),
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

            db.collection("users").document(generatedId).set(docData).await()
            activeUser = newUser
            return Result.success(newUser)
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error registrando usuario: ${e.message}", e)
            return Result.failure(e)
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
     * Real-time Messages for a Chat
     */
    fun observeRealtimeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
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
                    trySend(msgs)
                }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Failed to observe messages: ${e.message}")
        }
        awaitClose { registration?.remove() }
    }

    fun syncMessage(chatId: String, message: Message) {
        try {
            db.collection("chats").document(chatId).collection("messages").document(message.id).set(message)
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "syncMessage error: ${e.message}")
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

    suspend fun searchUserInFirebase(query: String): User? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null

        val isUsername = trimmed.startsWith("@") || trimmed.matches(Regex("^[a-zA-Z0-9_]{3,}$"))
        val isPhone = trimmed.matches(Regex("^[+]?[0-9]{6,15}$"))

        try {
            if (isUsername) {
                val cleanUser = trimmed.removePrefix("@").lowercase()
                val snapshot = db.collection("users")
                    .whereEqualTo("username", cleanUser)
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    return User(
                        userId = doc.getString("userId") ?: doc.id,
                        displayName = doc.getString("displayName") ?: "@$cleanUser",
                        username = "@" + (doc.getString("username") ?: cleanUser),
                        email = doc.getString("email") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        bio = doc.getString("bio") ?: "Usuario en Paul Messenger",
                        role = doc.getString("role") ?: UserRoles.USER,
                        isBanned = doc.getBoolean("isBanned") ?: false,
                        isOnline = doc.getBoolean("isOnline") ?: true,
                        isFirebaseUser = true,
                        avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF00C853,
                        avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF00796B
                    )
                }
            }

            if (isPhone) {
                val cleanPhone = trimmed.replace(" ", "").replace("-", "")
                val snapshot = db.collection("users")
                    .whereEqualTo("phoneNumber", cleanPhone)
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    return User(
                        userId = doc.getString("userId") ?: doc.id,
                        displayName = doc.getString("displayName") ?: "Contacto $cleanPhone",
                        username = "@" + (doc.getString("username") ?: "usuario"),
                        phoneNumber = cleanPhone,
                        bio = doc.getString("bio") ?: "Usuario en Paul Messenger",
                        role = doc.getString("role") ?: UserRoles.USER,
                        isBanned = doc.getBoolean("isBanned") ?: false,
                        isOnline = doc.getBoolean("isOnline") ?: true,
                        isFirebaseUser = true,
                        avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF2AABEE,
                        avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF1565C0
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Búsqueda en Firestore falló: ${e.message}")
        }

        return null
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
                            bio = "Conectado vía Google Cloud"
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
