package com.quare.bibleplanner.notification

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal class IosBibleInitializationNotifier {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    fun showProgress(current: Int, total: Int) {
        removeNotifications(listOf(ID_PROGRESS))
        val content = UNMutableNotificationContent().apply {
            setTitle(str(en = "Preparing the Bible", pt = "Preparando a Bíblia", es = "Preparando la Biblia"))
            setBody(
                if (total > 0) {
                    str(
                        en = "Loading books… $current of $total",
                        pt = "Carregando livros… $current de $total",
                        es = "Cargando libros… $current de $total",
                    )
                } else {
                    str(en = "Starting…", pt = "Iniciando…", es = "Iniciando…")
                },
            )
        }
        val request = UNNotificationRequest.requestWithIdentifier(ID_PROGRESS, content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    fun showComplete() {
        removeNotifications(listOf(ID_PROGRESS))
        val content = UNMutableNotificationContent().apply {
            setTitle(str(en = "Bible ready!", pt = "Bíblia pronta!", es = "¡Biblia lista!"))
            setBody(
                str(
                    en = "Bible data loaded successfully.",
                    pt = "Os dados da Bíblia foram carregados com sucesso.",
                    es = "Los datos de la Biblia se cargaron correctamente.",
                ),
            )
        }
        val request = UNNotificationRequest.requestWithIdentifier(ID_COMPLETE, content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    fun showError() {
        removeNotifications(listOf(ID_PROGRESS))
        val content = UNMutableNotificationContent().apply {
            setTitle(str(en = "Error preparing the Bible", pt = "Erro ao preparar a Bíblia", es = "Error al preparar la Biblia"))
            setBody(
                str(
                    en = "An error occurred. Open the app to try again.",
                    pt = "Ocorreu um erro. Abra o app para tentar novamente.",
                    es = "Ocurrió un error. Abre la app para intentarlo de nuevo.",
                ),
            )
        }
        val request = UNNotificationRequest.requestWithIdentifier(ID_ERROR, content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    fun dismiss() {
        removeNotifications(listOf(ID_PROGRESS, ID_COMPLETE, ID_ERROR))
    }

    private fun removeNotifications(identifiers: List<String>) {
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    /**
     * Returns the string matching the device's preferred language.
     * Falls back to [en] (English) if the language is not Portuguese or Spanish.
     */
    private fun str(en: String, pt: String, es: String): String {
        val lang = NSLocale.preferredLanguages.firstOrNull()?.toString() ?: return en
        return when {
            lang.startsWith("pt") -> pt
            lang.startsWith("es") -> es
            else -> en
        }
    }

    companion object {
        private const val ID_PROGRESS = "bible_init_progress"
        private const val ID_COMPLETE = "bible_init_complete"
        private const val ID_ERROR = "bible_init_error"
    }
}
