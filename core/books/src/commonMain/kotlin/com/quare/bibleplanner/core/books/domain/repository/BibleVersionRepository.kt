package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import kotlinx.coroutines.flow.Flow

interface BibleVersionRepository {
    suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>>

    fun observeVersions(): Flow<List<VersionModel>>
}
