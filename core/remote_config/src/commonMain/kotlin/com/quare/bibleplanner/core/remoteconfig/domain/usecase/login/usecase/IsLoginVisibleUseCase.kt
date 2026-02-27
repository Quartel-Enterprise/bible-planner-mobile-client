package com.quare.bibleplanner.core.remoteconfig.domain.usecase.login.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetBooleanRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.login.IsLoginVisible

internal class IsLoginVisibleUseCase(
    private val getBooleanRemoteConfig: GetBooleanRemoteConfigUseCase,
) : IsLoginVisible {
    override suspend fun invoke(): Boolean = getBooleanRemoteConfig("is_login_visible")
}
