package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object BolotaNotificationHelper {

    const val CHANNEL_ID = "bolota_daily_reminders"
    const val CHANNEL_NAME = "Lembretes Diários do Bolota"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Lembretes diários para completar hábitos e cuidar do Bolota"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun canSendNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showDailyReminderNotification(
        context: Context,
        petName: String,
        pendingHabitsCount: Int,
        totalHabitsCount: Int
    ) {
        createNotificationChannel(context)

        if (!canSendNotifications(context)) {
            return
        }

        // Tap action: open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Personalized message based on habits progress
        val title: String
        val body: String

        when {
            totalHabitsCount > 0 && pendingHabitsCount == 0 -> {
                title = "Incrível! Tudo pronto por hoje! 🌟"
                body = "Você já concluiu todos os hábitos de hoje. Venha dar um oi para o $petName e conferir seu XP!"
            }
            pendingHabitsCount > 0 -> {
                title = "$petName está te chamando! 🧡"
                body = if (pendingHabitsCount == 1) {
                    "Falta apenas 1 hábito para você fechar o dia com chave de ouro! Vamos lá?"
                } else {
                    "Você ainda tem $pendingHabitsCount hábitos pendentes hoje. Tire alguns minutinhos para cuidar de você!"
                }
            }
            else -> {
                title = "Hora do check-in com o $petName! 🐾"
                body = "Como foi o seu dia? Registre seus hábitos, sono e tela para manter o $petName radiante!"
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handled safely
        }
    }
}
