package com.quare.bibleplanner.core.provider.analytics.domain.usecase

fun interface GetAppInstanceId {
    suspend operator fun invoke(): String?
}
