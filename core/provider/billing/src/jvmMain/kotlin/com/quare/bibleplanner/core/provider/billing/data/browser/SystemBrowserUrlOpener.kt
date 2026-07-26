package com.quare.bibleplanner.core.provider.billing.data.browser

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingUnavailableException
import com.quare.bibleplanner.core.provider.billing.domain.usecase.OpenUrl
import java.awt.Desktop
import java.net.URI

internal class SystemBrowserUrlOpener : OpenUrl {
    override fun invoke(url: String) {
        if (!Desktop.isDesktopSupported()) throw BillingUnavailableException()
        val desktop = Desktop.getDesktop()
        if (!desktop.isSupported(Desktop.Action.BROWSE)) throw BillingUnavailableException()
        desktop.browse(URI(url))
    }
}
