package com.quare.bibleplanner.core.provider.billing.data.datasource

import kotlinx.browser.localStorage
import kotlin.uuid.Uuid

internal actual class AnonymousAppUserIdProvider : GetAnonymousAppUserId {
    actual override fun invoke(): String = localStorage.getItem(ANONYMOUS_APP_USER_ID_KEY) ?: createAnonymousAppUserId()

    private fun createAnonymousAppUserId(): String {
        val appUserId = ANONYMOUS_PREFIX + Uuid.random().toHexString()
        localStorage.setItem(ANONYMOUS_APP_USER_ID_KEY, appUserId)
        return appUserId
    }

    private companion object {
        const val ANONYMOUS_APP_USER_ID_KEY = "rc_anonymous_app_user_id"
        const val ANONYMOUS_PREFIX = $$"$RCAnonymousID:"
    }
}
