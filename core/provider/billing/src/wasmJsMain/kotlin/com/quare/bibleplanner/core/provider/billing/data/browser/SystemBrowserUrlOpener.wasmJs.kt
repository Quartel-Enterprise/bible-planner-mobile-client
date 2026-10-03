package com.quare.bibleplanner.core.provider.billing.data.browser

import com.quare.bibleplanner.core.provider.billing.domain.usecase.OpenUrl
import kotlinx.browser.window

internal actual class SystemBrowserUrlOpener : OpenUrl {
    actual override fun invoke(url: String) {
        window.open(url, BLANK_TARGET)
    }

    private companion object {
        const val BLANK_TARGET = "_blank"
    }
}
