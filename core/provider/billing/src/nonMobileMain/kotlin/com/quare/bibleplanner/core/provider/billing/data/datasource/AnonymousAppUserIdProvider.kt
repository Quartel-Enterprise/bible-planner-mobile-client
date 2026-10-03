package com.quare.bibleplanner.core.provider.billing.data.datasource

internal expect class AnonymousAppUserIdProvider() : GetAnonymousAppUserId {
    override fun invoke(): String
}
