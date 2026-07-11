package com.quare.bibleplanner.feature.inappupdate

import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal class IosStartUpdate(
    private val getAppStoreLink: GetAppStoreLinkUseCase,
) : StartUpdate {
    override suspend fun invoke() {
        val url = NSURL.URLWithString(getAppStoreLink()) ?: return
        UIApplication.sharedApplication.openURL(
            url = url,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}
