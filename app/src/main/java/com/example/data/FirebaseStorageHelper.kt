package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

/**
 * Helper para subida real de multimedia a Firebase Storage:
 * Imágenes, videos, documentos, notas de voz y estados.
 */
class FirebaseStorageHelper(private val context: Context) {

    private val storage: FirebaseStorage by lazy {
        try {
            Firebase.storage
        } catch (e: Exception) {
            Log.e("FirebaseStorageHelper", "Init warning: ${e.message}")
            FirebaseStorage.getInstance()
        }
    }

    /**
     * Sube un archivo local de audio/imagen/video a Firebase Storage y retorna la URL pública de descarga.
     */
    suspend fun uploadLocalFile(file: File, remoteFolder: String): Result<String> {
        if (!file.exists() || file.length() == 0L) {
            return Result.failure(IllegalArgumentException("El archivo local no existe o está vacío."))
        }

        return try {
            val fileName = "${UUID.randomUUID()}_${file.name}"
            val ref = storage.reference.child("$remoteFolder/$fileName")
            val uploadUri = Uri.fromFile(file)

            ref.putFile(uploadUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("FirebaseStorageHelper", "Error subiendo archivo local: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sube un Uri de contenido (de la galería o selector de archivos) y retorna la URL de descarga.
     */
    suspend fun uploadContentUri(contentUri: Uri, remoteFolder: String, preferredExt: String = "dat"): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.$preferredExt"
            val ref = storage.reference.child("$remoteFolder/$fileName")

            ref.putFile(contentUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("FirebaseStorageHelper", "Error subiendo URI de contenido: ${e.message}", e)
            Result.failure(e)
        }
    }
}
