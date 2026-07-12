package com.quare.bibleplanner.core.provider.platform

import co.touchlab.kermit.Logger
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManager
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestInAppReview
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class AndroidRequestInAppReview(
    private val reviewManager: ReviewManager,
    private val activityProvider: CurrentActivityProvider,
) : RequestInAppReview {
    override suspend fun invoke(): Boolean = suspendRunCatching {
        val activity = activityProvider.activity ?: return@suspendRunCatching false
        val reviewInfo = reviewManager.requestReview()
        reviewManager.launchReview(activity, reviewInfo)
        true
    }.getOrElse { throwable ->
        Logger.w(tag = TAG, throwable = throwable, messageString = "Failed to launch in-app review")
        false
    }

    private companion object {
        const val TAG = "AndroidRequestInAppReview"
    }
}
