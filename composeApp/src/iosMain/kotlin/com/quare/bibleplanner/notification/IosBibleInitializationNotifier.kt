package com.quare.bibleplanner.notification

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal class IosBibleInitializationNotifier {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    fun showProgress(current: Int, total: Int) {
        removeNotifications(listOf(ID_PROGRESS))
        val content = UNMutableNotificationContent().apply {
            setTitle("Preparando a Bíblia")
            setBody(
                if (total > 0) "Carregando livros... $current de $total"
                else "Iniciando...",
            )
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            ID_PROGRESS,
            content,
            trigger = null,
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    fun showComplete() {
        removeNotifications(listOf(ID_PROGRESS))
        val content = UNMutableNotificationContent().apply {
            setTitle("Bíblia pronta!")
            setBody("Os dados da Bíblia foram carregados com sucesso.")
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            ID_COMPLETE,
            content,
            trigger = null,
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    fun showError() {
        removeNotifications(listOf(ID_PROGRESS))
        val content = UNMutableNotificationContent().apply {
            setTitle("Erro ao preparar a Bíblia")
            setBody("Ocorreu um erro. Abra o app para tentar novamente.")
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            ID_ERROR,
            content,
            trigger = null,
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    fun dismiss() {
        removeNotifications(listOf(ID_PROGRESS, ID_COMPLETE, ID_ERROR))
    }

    private fun removeNotifications(identifiers: List<String>) {
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    companion object {
        private const val ID_PROGRESS = "bible_init_progress"
        private const val ID_COMPLETE = "bible_init_complete"
        private const val ID_ERROR = "bible_init_error"
    }
}
