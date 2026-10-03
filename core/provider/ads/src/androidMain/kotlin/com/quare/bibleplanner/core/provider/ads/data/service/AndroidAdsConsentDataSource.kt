package com.quare.bibleplanner.core.provider.ads.data.service

import android.content.Context
import co.touchlab.kermit.Logger
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentDataSource
import com.quare.bibleplanner.core.provider.platform.CurrentActivityProvider

internal class AndroidAdsConsentDataSource(
    private val currentActivityProvider: CurrentActivityProvider,
    context: Context,
) : AdsConsentDataSource {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
    private val logger = Logger.withTag(LOG_TAG)

    override fun gatherConsent(onComplete: (isInfoUpdated: Boolean) -> Unit) {
        val activity = currentActivityProvider.activity
        if (activity == null) {
            onComplete(false)
            return
        }
        consentInformation.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    error?.let(::logFormError)
                    onComplete(true)
                }
            },
            { error ->
                logFormError(error)
                onComplete(false)
            },
        )
    }

    override fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    override fun isPrivacyOptionsRequired(): Boolean = consentInformation.privacyOptionsRequirementStatus ==
        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    override fun showPrivacyOptions(onComplete: () -> Unit) {
        val activity = currentActivityProvider.activity
        if (activity == null) {
            onComplete()
            return
        }
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            error?.let(::logFormError)
            onComplete()
        }
    }

    private fun logFormError(error: FormError) {
        logger.w { "Consent form error: ${error.errorCode} ${error.message}" }
    }

    private companion object {
        const val LOG_TAG = "AdsConsent"
    }
}
