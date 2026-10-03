package com.quare.bibleplanner.feature.login.presentation

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
