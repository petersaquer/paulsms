package com.example.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/**
 * Helper para grabación real de notas de voz en Android.
 * Utiliza MediaRecorder con códec AAC y formato MP4/M4A estándar compatible
 * con todos los dispositivos Android y reproductores multimedia.
 */
class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    var isRecording: Boolean = false
        private set

    /**
     * Inicia la grabación del micrófono y guarda el audio en el archivo proporcionado.
     */
    fun startRecording(): File? {
        if (isRecording) {
            cancelRecording()
        }

        // Validación estricta del permiso de micrófono en tiempo de ejecución
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.e("AudioRecorderHelper", "No se puede iniciar grabación: Falta permiso RECORD_AUDIO")
            return null
        }

        return try {
            val audioDir = File(context.cacheDir, "audio_notes").apply { mkdirs() }
            val file = File(audioDir, "voice_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            file
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error al iniciar grabación de audio: ${e.message}", e)
            cancelRecording()
            null
        }
    }

    /**
     * Obtiene la amplitud actual del micrófono (para construir la forma de onda visual).
     */
    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording) mediaRecorder?.maxAmplitude ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Detiene la grabación y devuelve el archivo de audio generado.
     */
    fun stopRecording(): File? {
        if (!isRecording) return null
        return try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.w("AudioRecorderHelper", "Stop warning: ${e.message}")
                }
                release()
            }
            mediaRecorder = null
            isRecording = false
            val file = currentOutputFile
            if (file != null && file.exists() && file.length() > 0) {
                file
            } else {
                Log.w("AudioRecorderHelper", "Archivo grabado vacío o inexistente")
                file?.delete()
                null
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error al detener grabación: ${e.message}", e)
            cancelRecording()
            null
        }
    }

    /**
     * Cancela la grabación y descarta el archivo temporal.
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    // Silently ignore
                }
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioRecorderHelper", "Error releasing MediaRecorder: ${e.message}")
        } finally {
            mediaRecorder = null
            isRecording = false
            currentOutputFile?.let {
                if (it.exists()) it.delete()
            }
            currentOutputFile = null
        }
    }
}
