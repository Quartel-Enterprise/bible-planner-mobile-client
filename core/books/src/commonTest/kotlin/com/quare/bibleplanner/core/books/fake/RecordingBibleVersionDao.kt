package com.quare.bibleplanner.core.books.fake

import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity

internal class RecordingBibleVersionDao(
    private val existingVersions: List<BibleVersionEntity>,
) : ThrowingBibleVersionDao() {
    val insertedVersions = mutableListOf<BibleVersionEntity>()
    val updatedVersions = mutableListOf<BibleVersionEntity>()

    override suspend fun getVersionById(id: String): BibleVersionEntity? =
        (existingVersions + insertedVersions).find { it.id == id }

    override suspend fun insertVersion(version: BibleVersionEntity) {
        insertedVersions += version
    }

    override suspend fun updateVersion(version: BibleVersionEntity) {
        updatedVersions += version
    }
}
