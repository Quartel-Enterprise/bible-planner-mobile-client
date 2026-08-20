package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.mapper.BibleMapper
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class BibleRepositoryImpl(
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val bibleVersionRepository: BibleVersionRepository,
    private val bibleMapper: BibleMapper,
    private val dataStore: DataStore<Preferences>,
    private val languageProvider: LanguageProvider,
) : BibleRepository {
    private val bibleVersionKey = stringPreferencesKey(BIBLE_VERSION_KEY)

    override fun getBiblesFlow(): Flow<List<BibleModel>> = combine(
        bibleVersionRepository.observeVersions(),
        getSelectedVersionIdFlow(),
        bibleVersionDao.getAllVersionsFlow(),
        verseDao.getDownloadedChaptersPerVersionFlow(),
    ) { supportedVersions, selectedVersionId, dbVersions, chapterCounts ->
        val downloadedChaptersMap = chapterCounts.associate { it.bibleVersionId to it.downloadedChapters }
        bibleMapper
            .map(
                dataBaseVersions = dbVersions,
                supportedVersions = supportedVersions,
                downloadedChaptersMap = downloadedChaptersMap,
            ).map { bible ->
                bible.copy(
                    isSelected = bible.version.id.equals(
                        other = selectedVersionId,
                        ignoreCase = true,
                    ),
                )
            }
    }

    override fun getSelectedVersionIdFlow(): Flow<String> = dataStore.data
        .map { preferences -> preferences[bibleVersionKey] ?: getDefaultVersion() }
        .distinctUntilChanged()

    override suspend fun setSelectedVersionId(id: String) = dataStore.write(
        key = bibleVersionKey,
        value = id,
    )

    private fun getDefaultVersion(): String = when (languageProvider.getAppLanguage()) {
        Language.PORTUGUESE_BRAZIL -> "ACF"
        Language.SPANISH -> "RVR1960"
        Language.ENGLISH -> "WEB"
    }

    companion object {
        private const val BIBLE_VERSION_KEY = "selected_bible_version"
    }
}
