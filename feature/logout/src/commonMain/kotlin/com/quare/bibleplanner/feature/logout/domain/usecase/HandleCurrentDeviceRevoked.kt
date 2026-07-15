package com.quare.bibleplanner.feature.logout.domain.usecase

fun interface HandleCurrentDeviceRevoked {
    suspend operator fun invoke()
}
