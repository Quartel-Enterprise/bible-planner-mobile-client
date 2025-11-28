package com.quare.bibleplanner.core.books.data.datasource

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

class IosResourceReader : ResourceReader {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun readResource(path: String): String = withContext(Dispatchers.IO) {
        val pathComponents = path.split("/")
        val fileName = pathComponents.last().substringBeforeLast(".")
        val fileExtension = pathComponents.last().substringAfterLast(".", "")
        val directory = pathComponents.dropLast(1).joinToString("/")

        val fileManager = platform.Foundation.NSFileManager.defaultManager
        
        // For Kotlin Multiplatform, resources from commonMain/resources should be in the main bundle
        // However, for static frameworks, they might not be automatically included
        val mainBundle = NSBundle.mainBundle
        val mainBundlePath = mainBundle.bundlePath
        
        // Try multiple possible locations for resources
        // 1. Main bundle (standard location)
        // 2. Framework bundle (for static frameworks)
        // 3. Direct file system paths
        
        val frameworkBundlePath = "$mainBundlePath/Frameworks/CoreBooks.framework"
        val frameworkBundle = NSBundle.bundleWithPath(frameworkBundlePath)
        
        val bundlesToTry = listOfNotNull(
            mainBundle,
            frameworkBundle,
        )
        
        // Also try direct file system access as fallback
        val directPathsToTry = listOf(
            "$mainBundlePath/$path",
            "$mainBundlePath/Resources/$path",
            "$mainBundlePath/assets/$path",
            if (frameworkBundle != null) "$frameworkBundlePath/$path" else null,
            if (frameworkBundle != null) "$frameworkBundlePath/Resources/$path" else null,
        ).filterNotNull()

        for (bundle in bundlesToTry) {
            // Strategy 1: Try with full directory path (assets/books_by_chapter)
            var resourcePath = bundle.pathForResource(
                name = fileName,
                ofType = fileExtension,
                inDirectory = directory,
            )

            // Strategy 2: Try with just the subdirectory (books_by_chapter)
            if (resourcePath == null && directory.contains("/")) {
                val subDirectory = directory.substringAfterLast("/")
                resourcePath = bundle.pathForResource(
                    name = fileName,
                    ofType = fileExtension,
                    inDirectory = subDirectory,
                )
            }

            // Strategy 3: Try at bundle root (no directory)
            if (resourcePath == null) {
                resourcePath = bundle.pathForResource(
                    name = fileName,
                    ofType = fileExtension,
                    inDirectory = null,
                )
            }

            // Strategy 4: Try with path relative to bundle resource path
            if (resourcePath == null) {
                val bundleResourcePath = bundle.resourcePath
                if (bundleResourcePath != null) {
                    val fullPath = "$bundleResourcePath/$path"
                    if (fileManager.fileExistsAtPath(fullPath)) {
                        resourcePath = fullPath
                    }
                }
            }

            // Strategy 5: Try in bundle path directly
            if (resourcePath == null) {
                val bundlePath = bundle.bundlePath
                if (bundlePath != null) {
                    val fullPath = "$bundlePath/$path"
                    if (fileManager.fileExistsAtPath(fullPath)) {
                        resourcePath = fullPath
                    }
                }
            }

            if (resourcePath != null) {
                val content = NSString.stringWithContentsOfFile(resourcePath, NSUTF8StringEncoding, null)
                if (content != null) {
                    return@withContext content
                }
            }
        }
        
        // Fallback: Try direct file system paths
        for (directPath in directPathsToTry) {
            if (fileManager.fileExistsAtPath(directPath)) {
                val content = NSString.stringWithContentsOfFile(directPath, NSUTF8StringEncoding, null)
                if (content != null) {
                    return@withContext content
                }
            }
        }

        throw IllegalArgumentException("Resource not found: $path. Tried bundles: ${bundlesToTry.size}, direct paths: ${directPathsToTry.size}")
    }
}
