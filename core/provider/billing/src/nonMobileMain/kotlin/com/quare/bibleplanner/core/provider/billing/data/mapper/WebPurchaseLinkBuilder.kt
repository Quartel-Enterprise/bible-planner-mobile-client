package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import io.ktor.http.encodeURLParameter

internal class WebPurchaseLinkBuilder(
    private val config: DesktopBillingConfig,
) {
    fun build(
        appUserId: String,
        packageIdentifier: String,
    ): String {
        val baseUrl = config.purchaseLink.trimEnd(PATH_SEPARATOR)
        return "$baseUrl$PATH_SEPARATOR${appUserId.encode()}?$PACKAGE_ID_PARAM=${packageIdentifier.encode()}"
    }

    private fun String.encode(): String = encodeURLParameter()

    private companion object {
        const val PATH_SEPARATOR = '/'
        const val PACKAGE_ID_PARAM = "package_id"
    }
}
