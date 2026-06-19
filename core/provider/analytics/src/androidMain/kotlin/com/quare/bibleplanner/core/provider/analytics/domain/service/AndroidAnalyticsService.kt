package com.quare.bibleplanner.core.provider.analytics.domain.service

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
}
