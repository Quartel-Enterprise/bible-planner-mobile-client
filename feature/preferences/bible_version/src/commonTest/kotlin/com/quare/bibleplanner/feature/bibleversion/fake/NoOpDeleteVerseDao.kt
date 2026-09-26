package com.quare.bibleplanner.feature.bibleversion.fake

internal class NoOpDeleteVerseDao : ThrowingVerseDao() {
    override suspend fun deleteVerseTextsByVersion(versionId: String) = Unit
}
