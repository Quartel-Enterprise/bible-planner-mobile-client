package com.quare.bibleplanner.notification

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.TrayIcon.MessageType
import java.awt.image.BufferedImage
import java.util.Locale

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
                str(en = "Downloading $versionName", pt = "Baixando $versionName", es = "Descargando $versionName"),
                str(en = "Starting download…", pt = "Iniciando download…", es = "Iniciando descarga…"),
                MessageType.INFO,
            )
        }
    }

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        trayIcon?.displayMessage(
            str(en = "$versionName ready!", pt = "$versionName pronta!", es = "¡$versionName lista!"),
            str(
                en = "Bible downloaded successfully.",
                pt = "Bíblia baixada com sucesso.",
                es = "Biblia descargada correctamente.",
            ),
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
            str(
                en = "Download error — $versionName",
                pt = "Erro no download — $versionName",
                es = "Error de descarga — $versionName",
            ),
            str(
                en = "An error occurred. Try again.",
                pt = "Ocorreu um erro. Tente novamente.",
                es = "Ocurrió un error. Inténtalo de nuevo.",
            ),
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

    private fun str(
        en: String,
        pt: String,
        es: String,
    ): String = when (Locale.getDefault().language) {
        "pt" -> pt
        "es" -> es
        else -> en
    }
}
