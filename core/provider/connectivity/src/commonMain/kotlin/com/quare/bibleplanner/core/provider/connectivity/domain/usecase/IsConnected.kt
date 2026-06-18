package com.quare.bibleplanner.core.provider.connectivity.domain.usecase

import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver

/**
 * Whether the device currently has a usable internet connection, resolved as a one-shot suspend
 * check. Reads the latest value from [NetworkConnectivityObserver] without holding a subscription,
 * for callers that need a yes/no answer at a single moment (e.g. a button tap) rather than a stream.
 */
fun interface IsConnected {
    suspend operator fun invoke(): Boolean
}
