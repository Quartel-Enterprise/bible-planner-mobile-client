package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import kotlinx.coroutines.flow.Flow

internal class FakeBibleVersionRepository(
    private val versions: Result<List<VersionModel>>,
) : BibleVersionRepository {
    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> = versions

    override fun observeVersions(): Flow<List<VersionModel>> = error("Unexpected call")
}
