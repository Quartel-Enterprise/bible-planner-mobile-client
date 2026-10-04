package com.quare.bibleplanner.core.devices.domain.usecase

/*
 * Why: must run during logout while the session is still authenticated; otherwise the
 * signed-out device lingers in other devices' lists until the server prunes it.
 */
fun interface UnregisterCurrentDevice {
    suspend operator fun invoke(): Result<Unit>
}
