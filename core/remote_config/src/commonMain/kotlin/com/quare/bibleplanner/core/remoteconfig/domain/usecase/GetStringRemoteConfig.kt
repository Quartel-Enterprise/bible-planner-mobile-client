package com.quare.bibleplanner.core.remoteconfig.domain.usecase

/**
 * Use case to retrieve a string value from remote configuration.
 */
fun interface GetStringRemoteConfig {
    /**
     * Retrieves the string value associated with the given key.
     *
     * @param key The key of the remote configuration value.
     * @return The string value associated with the key.
     */
    suspend operator fun invoke(key: String): String
}
