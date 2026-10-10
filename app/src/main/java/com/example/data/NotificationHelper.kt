package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.example.MainActivity
import com.example.R

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "messages_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mensajes de Paul"
            val descriptionText = "Notificaciones de nuevos mensajes en Paul Messenger"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMessageNotification(senderName: String, messageText: String, chatId: String, soundName: String = "Predeterminado") {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("chat_id", chatId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // MessagingStyle for WhatsApp-like appearance
        val person = androidx.core.app.Person.Builder()
            .setName(senderName)
            .setImportant(true)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .addMessage(messageText, System.currentTimeMillis(), person)
            .setConversationTitle(null) // null for 1-to-1 chats
            .setGroupConversation(false)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShortcutId(chatId)

        // Sound handling (mocked)
        if (soundName != "Ninguno") {
            notificationBuilder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        }

        notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
    }
}
