package com.quare.bibleplanner.core.remoteconfig.fake

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeRemoteConfigService(
    private val values: Map<String, Any>,
) : RemoteConfigService {
    override fun observeBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> = flowOf(values[key] as? Boolean ?: defaultValue)

    override fun observeInt(
        key: String,
        defaultValue: Int,
    ): Flow<Int> = flowOf(values[key] as? Int ?: defaultValue)

    override fun observeString(
        key: String,
        defaultValue: String,
    ): Flow<String> = flowOf(values[key] as? String ?: defaultValue)
}
