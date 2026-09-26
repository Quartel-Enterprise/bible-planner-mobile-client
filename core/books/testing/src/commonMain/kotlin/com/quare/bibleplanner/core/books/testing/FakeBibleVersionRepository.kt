package com.quare.bibleplanner.core.books.testing

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBibleVersionRepository(
    remoteVersions: Result<List<VersionModel>>,
) : BibleVersionRepository {
    var remoteVersions = remoteVersions
    val cachedVersions = MutableStateFlow<List<VersionModel>>(emptyList())
    val forceRefreshRequests = mutableListOf<Boolean>()

    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> {
        forceRefreshRequests += forceRefresh
        return remoteVersions.onSuccess { versions -> cachedVersions.value = versions }
    }

    override fun observeVersions(): Flow<List<VersionModel>> = cachedVersions
}
