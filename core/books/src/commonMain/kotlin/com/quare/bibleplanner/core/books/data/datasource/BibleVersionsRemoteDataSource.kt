package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.dto.VersionDto

internal fun interface BibleVersionsRemoteDataSource {
    suspend fun getVersions(): Result<List<VersionDto>>
}
