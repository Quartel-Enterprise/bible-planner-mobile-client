package com.quare.bibleplanner.core.remoteconfig.domain.usecase

/**
 * Use case to retrieve a boolean value from remote configuration.
 */
fun interface GetBooleanRemoteConfig {
    /**
     * Retrieves the boolean value associated with the given key.
     *
     * @param key The key of the remote configuration value.
     * @return The boolean value associated with the key.
     */
    suspend operator fun invoke(key: String): Boolean
}
