package com.quare.bibleplanner.core.provider.connectivity.domain.usecase.impl

import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import kotlinx.coroutines.flow.first

internal class IsConnectedUseCase(
    private val networkConnectivityObserver: NetworkConnectivityObserver,
) : IsConnected {
    override suspend fun invoke(): Boolean = networkConnectivityObserver.observe().first()
}
