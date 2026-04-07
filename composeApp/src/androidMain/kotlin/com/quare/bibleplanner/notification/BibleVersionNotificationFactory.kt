package com.quare.bibleplanner.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.quare.bibleplanner.MainActivity
import java.util.Locale

internal class BibleVersionNotificationFactory(
    private val context: Context,
) {
    fun createProgress(
        versionName: String,
        progress: Float,
    ): Notification {
        val percent = progress * 100
        val percentInt = percent.toInt()
        return downloadingBuilder(versionName)
            .setContentText(
                if (percent > 0) {
                    str(
                        en = "Downloading… ${"%.1f".format(percent)}%",
                        pt = "Baixando… ${"%.1f".format(percent)}%",
                        es = "Descargando… ${"%.1f".format(percent)}%",
                    )
                } else {
                    str(en = "Starting download…", pt = "Iniciando download…", es = "Iniciando descarga…")
                },
            ).setProgress(100, percentInt, percentInt == 0)
            .build()
    }

    fun createForeground(versionName: String): Notification = downloadingBuilder(versionName)
        .setProgress(100, 0, true)
        .build()

    fun createPaused(
        versionName: String,
        progress: Float,
    ): Notification {
        val percent = progress * 100
        return builder()
            .setContentTitle(
                str(
                    en = "Download paused — $versionName",
                    pt = "Download pausado — $versionName",
                    es = "Descarga pausada — $versionName",
                ),
            ).setContentText(
                str(
                    en = "Paused at ${"%.1f".format(percent)}%. Open the app to resume.",
                    pt = "Pausado em ${"%.1f".format(percent)}%. Abra o app para continuar.",
                    es = "Pausado en ${"%.1f".format(percent)}%. Abre la app para continuar.",
                ),
            ).setSmallIcon(android.R.drawable.stat_sys_download)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun createComplete(versionName: String): Notification = builder()
        .setContentTitle(str(en = "$versionName ready!", pt = "$versionName pronta!", es = "¡$versionName lista!"))
        .setContentText(
            str(
                en = "Bible downloaded successfully.",
                pt = "Bíblia baixada com sucesso.",
                es = "Biblia descargada correctamente.",
            ),
        ).setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    fun createError(versionName: String): Notification = builder()
        .setContentTitle(
            str(
                en = "Download error — $versionName",
                pt = "Erro no download — $versionName",
                es = "Error de descarga — $versionName",
            ),
        ).setContentText(
            str(
                en = "An error occurred. Open the app to try again.",
                pt = "Ocorreu um erro. Abra o app para tentar novamente.",
                es = "Ocurrió un error. Abre la app para intentarlo de nuevo.",
            ),
        ).setSmallIcon(android.R.drawable.stat_notify_error)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    private fun downloadingBuilder(versionName: String): NotificationCompat.Builder = builder()
        .setContentTitle(
            str(en = "Downloading $versionName", pt = "Baixando $versionName", es = "Descargando $versionName"),
        ).setSmallIcon(android.R.drawable.stat_sys_download)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)

    private fun builder(): NotificationCompat.Builder = NotificationCompat
        .Builder(context, CHANNEL_ID)
        .setContentIntent(tapPendingIntent())

    private fun tapPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AndroidBibleVersionDownloadNotifier.EXTRA_NAVIGATE_TO_BIBLE_VERSIONS, true)
        }
        return PendingIntent.getActivity(
            context,
            TAP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun str(
        en: String,
        pt: String,
        es: String,
    ): String = when (Locale.getDefault().language) {
        "pt" -> pt
        "es" -> es
        else -> en
    }

    companion object {
        internal const val CHANNEL_ID = "bible_download_channel"
        private const val TAP_REQUEST_CODE = 1001
    }
}
