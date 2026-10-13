package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.model.AppUpdateInfo
import com.example.data.model.AuditLogEntry
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.GroupConfig
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SavedContact
import com.example.data.model.StatusItem
import com.example.data.model.StatusPrivacy
import com.example.data.model.StatusType
import com.example.data.model.User
import com.example.data.model.UserRoles
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class FirebaseChatRepository(private val context: Context) {

    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("paul_messenger_prefs", Context.MODE_PRIVATE)

    var activeUser: User?
        get() {
            try {
                val uid = prefs.getString("user_id", null) ?: auth?.currentUser?.uid ?: return null
                val displayName = prefs.getString("display_name", "") ?: ""
                val username = prefs.getString("username", "") ?: ""
                val email = prefs.getString("email", "") ?: ""
                val phone = prefs.getString("phone", "") ?: ""
                val bio = prefs.getString("bio", "") ?: ""
                val profilePictureUrl = prefs.getString("profile_picture_url", "") ?: ""
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
                        profilePictureUrl = profilePictureUrl,
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
                    profilePictureUrl = profilePictureUrl,
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
                    .putString("profile_picture_url", value.profilePictureUrl)
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
        } catch (_: Exception) {
            0
        }
    }

    suspend fun registerUser(
        fullName: String,
        username: String,
        email: String,
        pass: String,
        phone: String,
        bio: String
    ): Result<User> {
        return try {
            val cleanUser = username.trim().removePrefix("@").lowercase()
            val existing = db.collection("users").whereEqualTo("username", cleanUser).get().await()
            if (!existing.isEmpty) {
                return Result.failure(IllegalArgumentException("El nombre de usuario @$cleanUser ya está registrado"))
            }

            val authResult = auth?.createUserWithEmailAndPassword(email.trim(), pass)?.await()
            val generatedId = authResult?.user?.uid ?: "user_${System.currentTimeMillis()}"

            val assignedRole = if (email.trim().equals("admin@paul.com", ignoreCase = true)) UserRoles.ADMIN else UserRoles.USER

            val newUser = User(
                userId = generatedId,
                displayName = fullName.trim().ifBlank { "@$cleanUser" },
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
            db.collection("users").document(generatedId).set(docData).await()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(identifier: String, pass: String): Result<User> {
        return try {
            val clean = identifier.trim().removePrefix("@").lowercase()
            val querySnapshot = if (clean.contains("@")) {
                db.collection("users").whereEqualTo("email", clean).limit(1).get().await()
            } else {
                db.collection("users").whereEqualTo("username", clean).limit(1).get().await()
            }

            val doc = querySnapshot.documents.firstOrNull()
                ?: return Result.failure(IllegalArgumentException("Usuario o correo no encontrado"))

            val userEmail = doc.getString("email") ?: ""
            if (userEmail.isNotBlank()) {
                auth?.signInWithEmailAndPassword(userEmail, pass)?.await()
            }

            val user = User(
                userId = doc.getString("userId") ?: doc.id,
                displayName = doc.getString("displayName") ?: "@$clean",
                username = "@" + (doc.getString("username") ?: clean),
                email = doc.getString("email") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                bio = doc.getString("bio") ?: "",
                profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                role = doc.getString("role") ?: UserRoles.USER,
                isBanned = doc.getBoolean("isBanned") ?: false,
                isOnline = true,
                avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF2AABEE,
                avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF1976D2
            )

            activeUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(newDisplayName: String, newBio: String, newProfilePictureUrl: String? = null): Result<Unit> {
        val current = activeUser ?: return Result.failure(IllegalStateException("No hay sesión activa"))
        return try {
            val photoUrl = newProfilePictureUrl ?: current.profilePictureUrl
            val updates = mapOf(
                "displayName" to newDisplayName.trim(),
                "bio" to newBio.trim(),
                "profilePictureUrl" to photoUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(current.userId).update(updates).await()
            activeUser = current.copy(
                displayName = newDisplayName.trim(),
                bio = newBio.trim(),
                profilePictureUrl = photoUrl
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun onGoogleSignInClicked(
        activity: Activity,
        scope: CoroutineScope,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            val resId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
            if (resId != 0) activity.getString(resId) else "1026314511853-t8h1vbdijdtbbb3oik3rb388g6ji4k6j.apps.googleusercontent.com"
        } catch (_: Exception) {
            "1026314511853-t8h1vbdijdtbbb3oik3rb388g6ji4k6j.apps.googleusercontent.com"
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth?.signInWithCredential(firebaseCred)?.await()
                    val fbUser = authResult?.user

                    if (fbUser != null) {
                        val cleanUsername = (fbUser.displayName?.lowercase()?.replace(" ", "_")?.replace("[^a-z0-9_]".toRegex(), "") ?: "user_${fbUser.uid.take(4)}")
                        val loggedUser = User(
                            userId = fbUser.uid,
                            displayName = fbUser.displayName ?: "Usuario Google",
                            username = "@$cleanUsername",
                            email = fbUser.email ?: "",
                            phoneNumber = fbUser.phoneNumber ?: "",
                            bio = "Usuario en Paul Messenger",
                            profilePictureUrl = fbUser.photoUrl?.toString() ?: "",
                            role = UserRoles.USER,
                            isBanned = false,
                            isOnline = true
                        )

                        activeUser = loggedUser
                        val userDoc = mapOf(
                            "userId" to loggedUser.userId,
                            "displayName" to loggedUser.displayName,
                            "username" to cleanUsername,
                            "email" to loggedUser.email,
                            "profilePictureUrl" to loggedUser.profilePictureUrl,
                            "role" to loggedUser.role,
                            "isBanned" to false,
                            "isOnline" to true,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                        db.collection("users").document(loggedUser.userId).set(userDoc, com.google.firebase.firestore.SetOptions.merge()).await()
                        onComplete()
                    } else {
                        onError("No se pudo obtener información del usuario de Google")
                    }
                } else {
                    onError("Tipo de credencial no soportado")
                }
            } catch (e: GetCredentialCancellationException) {
                // User cancelled
            } catch (e: NoCredentialException) {
                onError("No hay cuentas de Google configuradas en el dispositivo")
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Error al iniciar sesión con Google")
            }
        }
    }

    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { doc ->
                val uid = doc.getString("userId") ?: doc.id
                val dName = doc.getString("displayName") ?: ""
                val uName = doc.getString("username") ?: ""
                User(
                    userId = uid,
                    displayName = dName,
                    username = "@$uName",
                    email = doc.getString("email") ?: "",
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    bio = doc.getString("bio") ?: "",
                    profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                    role = doc.getString("role") ?: UserRoles.USER,
                    isBanned = doc.getBoolean("isBanned") ?: false,
                    avatarGradientStart = doc.getLong("avatarGradientStart") ?: 0xFF2AABEE,
                    avatarGradientEnd = doc.getLong("avatarGradientEnd") ?: 0xFF1976D2
                )
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    fun observeLatestAppUpdate(): Flow<AppUpdateInfo?> = callbackFlow {
        val listener = db.collection("app_system").document("latest_update")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val info = AppUpdateInfo(
                        versionCode = snapshot.getLong("versionCode")?.toInt() ?: 1,
                        versionName = snapshot.getString("versionName") ?: "1.0.0",
                        releaseNotes = snapshot.getString("releaseNotes") ?: "",
                        downloadUrl = snapshot.getString("downloadUrl") ?: "",
                        isMandatory = snapshot.getBoolean("isMandatory") ?: false
                    )
                    trySend(info)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun searchUserInFirebase(query: String): User? {
        val clean = query.trim().removePrefix("@").lowercase()
        return try {
            val qSnap = db.collection("users").whereEqualTo("username", clean).limit(1).get().await()
            val doc = qSnap.documents.firstOrNull() ?: return null
            User(
                userId = doc.getString("userId") ?: doc.id,
                displayName = doc.getString("displayName") ?: "@$clean",
                username = "@" + (doc.getString("username") ?: clean),
                email = doc.getString("email") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                bio = doc.getString("bio") ?: "",
                profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                role = doc.getString("role") ?: UserRoles.USER,
                isBanned = doc.getBoolean("isBanned") ?: false
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun toggleUserBan(targetUserId: String, isBanned: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(targetUserId).update("isBanned", isBanned).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(targetUserId: String, newRole: String): Result<Unit> {
        return try {
            db.collection("users").document(targetUserId).update("role", newRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(onComplete: () -> Unit) {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        activeUser = null
        onComplete()
    }
}
