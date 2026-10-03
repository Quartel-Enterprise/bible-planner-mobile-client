package com.quare.bibleplanner.core.devices.domain.usecase

fun interface UnregisterCurrentDevice {
    suspend operator fun invoke(): Result<Unit>
}
