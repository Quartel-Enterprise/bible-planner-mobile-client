package com.quare.bibleplanner.feature.login.presentation

/**
 * Reads a bundled classpath resource as UTF-8 text and returns the outcome as a [Result].
 *
 * Returning [Result] instead of throwing lets every downstream layer compose the failure
 * (factory → flow → synchronizer → starter → ViewModel → UI error state) without scattering
 * try/catch blocks across the call chain.
 */
internal class GetResourcesAsTextResult {
    operator fun invoke(path: String): Result<String> = runCatching {
        val classLoader = Thread.currentThread().contextClassLoader
            ?: javaClass.classLoader
        classLoader
            .getResourceAsStream(path)
            ?.reader(Charsets.UTF_8)
            ?.readText()
            ?: error("Resource not found on classpath: $path")
    }
}
