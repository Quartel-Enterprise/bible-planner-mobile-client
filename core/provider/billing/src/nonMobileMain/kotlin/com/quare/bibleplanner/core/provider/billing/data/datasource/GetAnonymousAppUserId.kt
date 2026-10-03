package com.quare.bibleplanner.core.provider.billing.data.datasource

internal fun interface GetAnonymousAppUserId {
    operator fun invoke(): String
}
