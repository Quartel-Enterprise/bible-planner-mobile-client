package com.quare.bibleplanner.core.devices.domain.usecase

/**
 * Removes THIS device's row from the connected-devices list, called during logout while the session
 * is still authenticated. Other devices see the removal live over realtime; without it the signed-out
 * device would linger in their lists until the server prunes it.
 */
fun interface UnregisterCurrentDevice {
    suspend operator fun invoke(): Result<Unit>
}
