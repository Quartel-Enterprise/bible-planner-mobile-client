package com.quare.bibleplanner.core.provider.billing.data.browser

import com.quare.bibleplanner.core.provider.billing.domain.usecase.OpenUrl

internal expect class SystemBrowserUrlOpener() : OpenUrl {
    override fun invoke(url: String)
}
