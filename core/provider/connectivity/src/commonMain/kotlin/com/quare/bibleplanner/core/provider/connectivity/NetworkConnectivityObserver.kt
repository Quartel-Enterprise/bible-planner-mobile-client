package com.quare.bibleplanner.core.provider.connectivity

import kotlinx.coroutines.flow.Flow

fun interface NetworkConnectivityObserver {
    fun observe(): Flow<Boolean>
}
