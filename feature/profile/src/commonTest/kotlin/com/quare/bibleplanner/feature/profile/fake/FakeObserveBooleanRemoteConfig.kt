package com.quare.bibleplanner.feature.profile.fake

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeObserveBooleanRemoteConfig(
    private val value: Boolean,
) : ObserveBooleanRemoteConfig {
    override fun invoke(
        key: String,
        default: Boolean,
    ): Flow<Boolean> = flowOf(value)
}
