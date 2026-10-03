package com.quare.bibleplanner.core.provider.connectivity.domain.usecase

fun interface IsConnected {
    suspend operator fun invoke(): Boolean
}
