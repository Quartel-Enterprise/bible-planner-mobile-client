package com.quare.bibleplanner.core.devices.domain.usecase

fun interface ObserveDeviceRegistration {
    suspend operator fun invoke()
}
