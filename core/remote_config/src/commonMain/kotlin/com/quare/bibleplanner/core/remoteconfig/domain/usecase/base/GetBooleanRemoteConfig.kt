package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base

interface GetBooleanRemoteConfig {
    suspend operator fun invoke(
        key: String,
        default: Boolean = false,
    ): Boolean
}
