package com.quare.bibleplanner.core.provider.billing.data.datasource

import java.util.UUID
import java.util.prefs.Preferences

internal class AnonymousAppUserIdProvider : GetAnonymousAppUserId {
    private val preferences = Preferences.userRoot().node(PREFERENCES_NODE)

    override fun invoke(): String = preferences.get(ANONYMOUS_APP_USER_ID_KEY, null) ?: createAnonymousAppUserId()

    private fun createAnonymousAppUserId(): String {
        val rawId = UUID.randomUUID().toString().replace("-", "")
        val appUserId = ANONYMOUS_PREFIX + rawId
        preferences.put(ANONYMOUS_APP_USER_ID_KEY, appUserId)
        return appUserId
    }

    private companion object {
        const val PREFERENCES_NODE = "com/quare/bibleplanner/billing"
        const val ANONYMOUS_APP_USER_ID_KEY = "rc_anonymous_app_user_id"
        const val ANONYMOUS_PREFIX = $$"$RCAnonymousID:"
    }
}
