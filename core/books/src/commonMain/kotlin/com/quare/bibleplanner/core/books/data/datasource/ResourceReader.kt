package com.quare.bibleplanner.core.books.data.datasource

fun interface ResourceReader {
    suspend fun readResource(path: String): String
}
