package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base

import kotlinx.coroutines.flow.Flow

interface ObserveBooleanRemoteConfig {
    operator fun invoke(
        key: String,
        default: Boolean = false,
    ): Flow<Boolean>
}
