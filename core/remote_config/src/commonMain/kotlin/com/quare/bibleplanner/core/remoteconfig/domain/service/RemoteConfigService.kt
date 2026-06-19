package com.quare.bibleplanner.core.remoteconfig.domain.service

import kotlinx.coroutines.flow.Flow

interface RemoteConfigService {
    fun observeBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean>

    fun observeInt(
        key: String,
        defaultValue: Int,
    ): Flow<Int>

    fun observeString(
        key: String,
        defaultValue: String,
    ): Flow<String>
}
