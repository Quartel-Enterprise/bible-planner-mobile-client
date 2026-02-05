package com.quare.bibleplanner.feature.bibleversion.domain.repository

import com.quare.bibleplanner.feature.bibleversion.domain.model.VersionModel

internal interface BibleVersionMetadataRepository {
    suspend fun getVersions(): Result<List<VersionModel>>
}
