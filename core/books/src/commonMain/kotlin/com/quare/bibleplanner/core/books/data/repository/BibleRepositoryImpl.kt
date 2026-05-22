package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.mapper.BibleMapper
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.utils.locale.isPortugueseBrazil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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

    override fun getBiblesFlow(): Flow<List<BibleModel>> = flow {
        val supportedVersions = bibleVersionRepository.getVersions().getOrDefault(emptyList())
        emitAll(
            combine(
                getSelectedVersionIdFlow(),
                bibleVersionDao.getAllVersionsFlow(),
                verseDao.getDownloadedChaptersPerVersionFlow(),
            ) { selectedVersionId, dbVersions, chapterCounts ->
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
            },
        )
    }

    override fun getSelectedVersionIdFlow(): Flow<String> = dataStore.data.map { preferences ->
        preferences[bibleVersionKey] ?: getDefaultVersion()
    }

    override suspend fun setSelectedVersionId(id: String) {
        dataStore.edit { preferences ->
            preferences[bibleVersionKey] = id
        }
    }

    private fun getDefaultVersion(): String = if (languageProvider.getAppLanguage().isPortugueseBrazil) "ACF" else "WEB"

    companion object {
        private const val BIBLE_VERSION_KEY = "selected_bible_version"
    }
}
