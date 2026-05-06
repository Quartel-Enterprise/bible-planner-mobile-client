package com.quare.bibleplanner.notification

import bibleplanner.composeapp.generated.resources.Res
import bibleplanner.composeapp.generated.resources.notification_complete_message
import bibleplanner.composeapp.generated.resources.notification_complete_title
import bibleplanner.composeapp.generated.resources.notification_error_message
import bibleplanner.composeapp.generated.resources.notification_error_title
import bibleplanner.composeapp.generated.resources.notification_preparing_starting
import bibleplanner.composeapp.generated.resources.notification_preparing_title
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import org.jetbrains.compose.resources.getString
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.TrayIcon.MessageType
import java.awt.image.BufferedImage

internal class DesktopBibleVersionDownloadNotifier(
    private val navigationEventBus: NavigationEventBus,
) : BibleVersionDownloadNotifier {
    private val trayIcon: TrayIcon? by lazy { createTrayIconIfSupported() }

    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        val percent = (progress * 100).toInt()
        if (percent == 0) {
            trayIcon?.displayMessage(
                getString(Res.string.notification_preparing_title, versionName),
                getString(Res.string.notification_preparing_starting),
                MessageType.INFO,
            )
        }
    }

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        trayIcon?.displayMessage(
            getString(Res.string.notification_complete_title, versionName),
            getString(Res.string.notification_complete_message),
            MessageType.INFO,
        )
    }

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) = Unit

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        trayIcon?.displayMessage(
            getString(Res.string.notification_error_title, versionName),
            getString(Res.string.notification_error_message),
            MessageType.ERROR,
        )
    }

    override suspend fun dismiss(versionId: String) = Unit

    private fun createTrayIconIfSupported(): TrayIcon? {
        if (!SystemTray.isSupported()) return null
        val tray = SystemTray.getSystemTray()
        val iconSize = tray.trayIconSize
        val image = BufferedImage(iconSize.width, iconSize.height, BufferedImage.TYPE_INT_ARGB)
        val icon = TrayIcon(image).apply {
            isImageAutoSize = true
            addActionListener { navigationEventBus.send(BibleVersionSelectorRoute) }
        }
        runCatching { tray.add(icon) }
        return icon
    }
}
