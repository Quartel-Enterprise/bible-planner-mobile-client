package com.quare.bibleplanner.core.books.data.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class JvmResourceReader : ResourceReader {

    override suspend fun readResource(path: String): String = withContext(Dispatchers.IO) {
        // For JVM, resources from commonMain/resources are accessible via ClassLoader
        // The path should be relative to the resources root (no leading slash)
        val resourcePath = if (path.startsWith("/")) path.removePrefix("/") else path
        
        // Try multiple ClassLoaders to find the resource
        // Start with the class's ClassLoader as it's most likely to have the library resources
        val classLoaders = listOf(
            this::class.java.classLoader,
            Thread.currentThread().contextClassLoader,
            ClassLoader.getSystemClassLoader()
        )
        
        for (classLoader in classLoaders) {
            if (classLoader != null) {
                // Try the path as-is
                classLoader.getResourceAsStream(resourcePath)?.use { inputStream ->
                    return@withContext BufferedReader(InputStreamReader(inputStream)).readText()
                }
                
                // Also try with leading slash (some ClassLoaders expect it)
                if (!resourcePath.startsWith("/")) {
                    classLoader.getResourceAsStream("/$resourcePath")?.use { inputStream ->
                        return@withContext BufferedReader(InputStreamReader(inputStream)).readText()
                    }
                }
            }
        }
        
        throw IllegalArgumentException("Resource not found: $resourcePath")
    }
}
