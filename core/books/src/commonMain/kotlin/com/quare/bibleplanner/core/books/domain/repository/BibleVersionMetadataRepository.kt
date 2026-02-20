package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.books.domain.model.VersionModel

interface BibleVersionMetadataRepository {
    suspend fun getVersions(): Result<List<VersionModel>>
}
