package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base

interface GetIntRemoteConfig {
    suspend operator fun invoke(
        key: String,
        default: Int = 0,
    ): Int
}
