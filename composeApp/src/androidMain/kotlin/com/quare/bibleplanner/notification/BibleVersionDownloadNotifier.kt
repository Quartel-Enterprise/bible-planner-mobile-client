package com.quare.bibleplanner.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import java.util.Locale

internal class AndroidBibleVersionDownloadNotifier(
    private val context: Context,
) : BibleVersionDownloadNotifier {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            str(en = "Bible Download", pt = "Download da Bíblia", es = "Descarga de la Biblia"),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = str(
                en = "Shows download progress for Bible versions",
                pt = "Exibe o progresso do download das versões da Bíblia",
                es = "Muestra el progreso de descarga de las versiones de la Biblia",
            )
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    override suspend fun showProgress(versionId: String, versionName: String, progress: Float) {
        if (!canShowNotification()) return
        val percent = (progress * 100).toInt()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(str(en = "Downloading $versionName", pt = "Baixando $versionName", es = "Descargando $versionName"))
            .setContentText(
                if (percent > 0) str(
                    en = "Downloading… $percent%",
                    pt = "Baixando… $percent%",
                    es = "Descargando… $percent%",
                )
                else str(en = "Starting download…", pt = "Iniciando download…", es = "Iniciando descarga…"),
            )
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, percent, percent == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(notificationId(versionId), notification)
    }

    @SuppressLint("MissingPermission")
    override suspend fun showComplete(versionId: String, versionName: String) {
        dismiss(versionId)
        if (!canShowNotification()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(str(en = "$versionName ready!", pt = "$versionName pronta!", es = "¡$versionName lista!"))
            .setContentText(
                str(
                    en = "Bible downloaded successfully.",
                    pt = "Bíblia baixada com sucesso.",
                    es = "Biblia descargada correctamente.",
                ),
            )
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(notificationId(versionId), notification)
    }

    @SuppressLint("MissingPermission")
    override suspend fun showError(versionId: String, versionName: String) {
        dismiss(versionId)
        if (!canShowNotification()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(str(en = "Download error — $versionName", pt = "Erro no download — $versionName", es = "Error de descarga — $versionName"))
            .setContentText(
                str(
                    en = "An error occurred. Open the app to try again.",
                    pt = "Ocorreu um erro. Abra o app para tentar novamente.",
                    es = "Ocurrió un error. Abre la app para intentarlo de nuevo.",
                ),
            )
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(notificationId(versionId), notification)
    }

    override suspend fun dismiss(versionId: String) {
        notificationManager.cancel(notificationId(versionId))
    }

    private fun canShowNotification(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationManager.areNotificationsEnabled()
        }

    private fun notificationId(versionId: String): Int = versionId.hashCode()

    private fun str(en: String, pt: String, es: String): String =
        when (Locale.getDefault().language) {
            "pt" -> pt
            "es" -> es
            else -> en
        }

    companion object {
        private const val CHANNEL_ID = "bible_download_channel"
    }
}
