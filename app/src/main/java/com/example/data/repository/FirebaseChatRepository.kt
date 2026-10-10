package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class FirebaseChatRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    private val auth: FirebaseAuth get() = Firebase.auth

    val currentUser: User?
        get() {
            val fbUser = auth.currentUser ?: return null
            return User(
                userId = fbUser.uid,
                displayName = fbUser.displayName ?: "Usuario Paul",
                phoneNumber = fbUser.phoneNumber ?: "",
                username = "@" + (fbUser.displayName?.lowercase()?.replace(" ", "_") ?: "user_${fbUser.uid.take(4)}"),
                bio = "Conectado vía Firebase Cloud ⚡",
                isOnline = true,
                isFirebaseUser = true
            )
        }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    suspend fun registerCurrentProfile(user: User) {
        val uid = auth.currentUser?.uid ?: return
        val cleanUsername = user.username.trim().removePrefix("@").lowercase()
        val data = mapOf(
            "userId" to uid,
            "displayName" to user.displayName,
            "username" to cleanUsername,
            "phoneNumber" to user.phoneNumber.trim().replace(" ", "").replace("-", ""),
            "bio" to user.bio,
            "isOnline" to true,
            "avatarGradientStart" to user.avatarGradientStart,
            "avatarGradientEnd" to user.avatarGradientEnd,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("users").document(uid).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseChatRepo", "Error registrando perfil: ${e.message}")
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
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        bio = doc.getString("bio") ?: "Usuario verificado en Firebase Cloud",
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
                        bio = doc.getString("bio") ?: "Usuario verificado en Firebase Cloud",
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

        // Return null if not found in Firebase database
        return null
    }

    fun generateE2EEFingerprint(id1: String, id2: String): String {
        val combined = if (id1 < id2) "$id1:$id2:paul_e2ee" else "$id2:$id1:paul_e2ee"
        val bytes = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        return bytes.take(8).joinToString("") { "%02X".format(it) }
    }

    suspend fun seedDemoFirebaseUsers() {
        try {
            val demoUsers = listOf(
                User(
                    userId = "demo_maria",
                    displayName = "María López",
                    username = "maria_dev",
                    phoneNumber = "+34600112233",
                    bio = "Desarrolladora Cloud & Kotlin en Paul",
                    avatarGradientStart = 0xFF9C27B0,
                    avatarGradientEnd = 0xFF6A1B9A
                ),
                User(
                    userId = "demo_lucas",
                    displayName = "Lucas Silva",
                    username = "lucas_cloud",
                    phoneNumber = "+34611445566",
                    bio = "Arquitecto de bases de datos Firebase",
                    avatarGradientStart = 0xFF009688,
                    avatarGradientEnd = 0xFF004D40
                ),
                User(
                    userId = "demo_admin",
                    displayName = "Soporte Paul Cloud",
                    username = "soporte_paul",
                    phoneNumber = "+34622778899",
                    bio = "Canal oficial de soporte en la nube",
                    avatarGradientStart = 0xFF2196F3,
                    avatarGradientEnd = 0xFF0D47A1
                )
            )

            demoUsers.forEach { user ->
                db.collection("users").document(user.userId).set(
                    mapOf(
                        "userId" to user.userId,
                        "displayName" to user.displayName,
                        "username" to user.username.removePrefix("@"),
                        "phoneNumber" to user.phoneNumber,
                        "bio" to user.bio,
                        "isOnline" to true,
                        "avatarGradientStart" to user.avatarGradientStart,
                        "avatarGradientEnd" to user.avatarGradientEnd,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
        } catch (e: Exception) {
            Log.w("FirebaseChatRepo", "Seed users opcional: ${e.message}")
        }
    }

    fun onGoogleSignInClicked(
        activity: Activity,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
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
                    auth.signInWithCredential(authCredential).await()
                    currentUser?.let { registerCurrentProfile(it) }
                    onAuthSuccess()
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
        auth.signOut()
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
