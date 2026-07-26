package com.quare.bibleplanner.core.provider.billing.data.datasource

internal fun interface GetAppUserId {
    suspend operator fun invoke(): String
}
