package com.quare.bibleplanner.core.devices.domain.usecase

fun interface RenameDevice {
    suspend operator fun invoke(
        deviceRowId: String,
        name: String,
    ): Result<Unit>
}
