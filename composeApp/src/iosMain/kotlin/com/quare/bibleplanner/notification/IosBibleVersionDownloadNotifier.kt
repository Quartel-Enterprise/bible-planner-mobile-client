package com.quare.bibleplanner.notification

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.NavigationEventBus
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal class IosBibleVersionDownloadNotifier(
    navigationEventBus: NavigationEventBus,
) : BibleVersionDownloadNotifier {
    init {
        NotificationTapRouter.setNavigationEventBus(navigationEventBus)
    }

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        if (progress > 0f) return
        val content = UNMutableNotificationContent().apply {
            setTitle(
                str(en = "Downloading $versionName", pt = "Baixando $versionName", es = "Descargando $versionName"),
            )
            setBody(str(en = "Starting…", pt = "Iniciando…", es = "Iniciando…"))
        }
        val request = UNNotificationRequest.requestWithIdentifier(progressId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        val percent = progress * 100
        val percentFormatted = "%.1f".format(percent)
        removeNotifications(listOf(progressId(versionId)))
        val content = UNMutableNotificationContent().apply {
            setTitle(
                str(
                    en = "Download paused — $versionName",
                    pt = "Download pausado — $versionName",
                    es = "Descarga pausada — $versionName",
                ),
            )
            setBody(
                str(
                    en = "Paused at $percentFormatted%. Open the app to resume.",
                    pt = "Pausado em $percentFormatted%. Abra o app para continuar.",
                    es = "Pausado en $percentFormatted%. Abre la app para continuar.",
                ),
            )
        }
        val request = UNNotificationRequest.requestWithIdentifier(progressId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        removeNotifications(listOf(progressId(versionId)))
        val content = UNMutableNotificationContent().apply {
            setTitle(str(en = "$versionName ready!", pt = "$versionName pronta!", es = "¡$versionName lista!"))
            setBody(
                str(
                    en = "Bible data loaded successfully.",
                    pt = "Os dados da Bíblia foram carregados com sucesso.",
                    es = "Los datos de la Biblia se cargaron correctamente.",
                ),
            )
        }
        val request = UNNotificationRequest.requestWithIdentifier(completeId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        removeNotifications(listOf(progressId(versionId)))
        val content = UNMutableNotificationContent().apply {
            setTitle(
                str(
                    en = "Error downloading $versionName",
                    pt = "Erro ao baixar $versionName",
                    es = "Error al descargar $versionName",
                ),
            )
            setBody(
                str(
                    en = "An error occurred. Open the app to try again.",
                    pt = "Ocorreu um erro. Abra o app para tentar novamente.",
                    es = "Ocurrió un error. Abre la app para intentarlo de nuevo.",
                ),
            )
        }
        val request = UNNotificationRequest.requestWithIdentifier(errorId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override suspend fun dismiss(versionId: String) {
        removeNotifications(listOf(progressId(versionId), completeId(versionId), errorId(versionId)))
    }

    private fun progressId(versionId: String): String = "${versionId}_progress"

    private fun completeId(versionId: String): String = "${versionId}_complete"

    private fun errorId(versionId: String): String = "${versionId}_error"

    private fun removeNotifications(identifiers: List<String>) {
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    private fun str(
        en: String,
        pt: String,
        es: String,
    ): String {
        val lang = NSLocale.preferredLanguages.firstOrNull()?.toString() ?: return en
        return when {
            lang.startsWith("pt") -> pt
            lang.startsWith("es") -> es
            else -> en
        }
    }
}
