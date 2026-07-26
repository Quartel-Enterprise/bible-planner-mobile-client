package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class WebPurchaseLinkBuilder(
    private val config: DesktopBillingConfig,
) {
    fun build(
        appUserId: String,
        packageIdentifier: String,
    ): String {
        val baseUrl = config.purchaseLink.trimEnd(PATH_SEPARATOR)
        return "$baseUrl$PATH_SEPARATOR${appUserId.encoded()}?$PACKAGE_ID_PARAM=${packageIdentifier.encoded()}"
    }

    private fun String.encoded(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

    private companion object {
        const val PATH_SEPARATOR = '/'
        const val PACKAGE_ID_PARAM = "package_id"
    }
}
