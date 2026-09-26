package com.quare.bibleplanner.feature.daystudy.fake

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig

internal class DefaultIntRemoteConfig : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = default
}
