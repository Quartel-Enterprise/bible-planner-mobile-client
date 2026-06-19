package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base

import kotlinx.coroutines.flow.Flow

interface ObserveIntRemoteConfig {
    operator fun invoke(
        key: String,
        default: Int = 0,
    ): Flow<Int>
}
