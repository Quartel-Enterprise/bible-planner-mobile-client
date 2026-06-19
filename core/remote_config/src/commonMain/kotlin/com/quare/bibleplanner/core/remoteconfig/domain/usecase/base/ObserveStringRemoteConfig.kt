package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base

import kotlinx.coroutines.flow.Flow

interface ObserveStringRemoteConfig {
    operator fun invoke(
        key: String,
        default: String = "",
    ): Flow<String>
}
