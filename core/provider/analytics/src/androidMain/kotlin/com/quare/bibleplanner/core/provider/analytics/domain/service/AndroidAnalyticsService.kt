package com.quare.bibleplanner.core.provider.analytics.domain.service

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

internal class AndroidAnalyticsService(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsService {
    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        firebaseAnalytics.logEvent(name, params.toBundle())
    }

    private fun Map<String, Any>.toBundle(): Bundle {
        val bundle = Bundle()
        forEach { (key, value) ->
            when (value) {
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        return bundle
    }
}
