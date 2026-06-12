package com.quare.bibleplanner.core.provider.platform

/**
 * Detects the current platform.
 * @return The platform the application is running on.
 */
internal expect fun getPlatform(): Platform
