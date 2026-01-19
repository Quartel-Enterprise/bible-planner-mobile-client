package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base

/**
 * Use case to retrieve an integer value from remote configuration.
 */
fun interface GetIntRemoteConfig {
    /**
     * Retrieves the integer value associated with the given key.
     *
     * @param key The key of the remote configuration value.
     * @return The integer value associated with the key.
     */
    suspend operator fun invoke(key: String): Int
}
