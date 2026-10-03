package com.quare.bibleplanner.core.sync.domain

fun interface FetchedSnapshot {
    suspend fun apply()
}
