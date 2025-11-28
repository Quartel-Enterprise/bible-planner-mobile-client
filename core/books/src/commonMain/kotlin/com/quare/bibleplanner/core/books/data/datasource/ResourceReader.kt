package com.quare.bibleplanner.core.books.data.datasource

/**
 * Platform-specific interface for reading resources from assets.
 */
interface ResourceReader {
    /**
     * Reads the content of a resource file as a string.
     * @param path The path to the resource file (e.g., "assets/books_by_chapter/GEN.json")
     * @return The content of the file as a string
     */
    suspend fun readResource(path: String): String
}
