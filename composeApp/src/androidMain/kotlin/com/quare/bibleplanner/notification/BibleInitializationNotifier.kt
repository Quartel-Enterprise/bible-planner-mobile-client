package com.quare.bibleplanner.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

internal class BibleInitializationNotifier(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Preparação da Bíblia",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Exibe o progresso enquanto os dados da Bíblia são preparados"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildProgressNotification(current: Int, total: Int) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Preparando a Bíblia")
            .setContentText(
                if (total > 0) "Carregando livros... $current de $total"
                else "Iniciando...",
            )
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(total, current, total == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    fun showComplete() {
        if (!notificationManager.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Bíblia pronta!")
            .setContentText("Os dados da Bíblia foram carregados com sucesso.")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showError() {
        if (!notificationManager.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Erro ao preparar a Bíblia")
            .setContentText("Ocorreu um erro. Abra o app para tentar novamente.")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismiss() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "bible_initialization_channel"
        const val NOTIFICATION_ID = 1001
    }
}
