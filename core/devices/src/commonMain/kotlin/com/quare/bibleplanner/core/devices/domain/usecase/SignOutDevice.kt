package com.quare.bibleplanner.core.devices.domain.usecase

fun interface SignOutDevice {
    suspend operator fun invoke(deviceRowId: String): Result<Unit>
}
