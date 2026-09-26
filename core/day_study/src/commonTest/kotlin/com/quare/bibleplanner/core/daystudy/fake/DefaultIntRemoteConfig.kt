package com.quare.bibleplanner.core.daystudy.fake

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig

internal class DefaultIntRemoteConfig : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = default
}
