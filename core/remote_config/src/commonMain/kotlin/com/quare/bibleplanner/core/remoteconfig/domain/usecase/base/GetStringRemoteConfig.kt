package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base

interface GetStringRemoteConfig {
    suspend operator fun invoke(
        key: String,
        default: String = "",
    ): String
}
