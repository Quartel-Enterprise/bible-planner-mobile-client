package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.books.domain.model.VersionModel

fun interface BibleVersionRepository {
    suspend fun getVersions(): Result<List<VersionModel>>
}
