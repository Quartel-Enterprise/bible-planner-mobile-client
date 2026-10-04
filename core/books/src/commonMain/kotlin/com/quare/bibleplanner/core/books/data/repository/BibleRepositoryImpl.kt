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
import com.quare.bibleplanner.core.provider.room.invalidation.TableInvalidationObserver
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount
import com.quare.bibleplanner.core.provider.room.utils.DatabaseTables
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.core.utils.throttleLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class BibleRepositoryImpl(
    private val verseDao: VerseDao,
    private val dataStore: DataStore<Preferences>,
    private val languageProvider: LanguageProvider,
    private val observeTableInvalidation: TableInvalidationObserver,
    bibleVersionDao: BibleVersionDao,
    bibleVersionRepository: BibleVersionRepository,
    bibleMapper: BibleMapper,
    applicationScope: ApplicationScope,
) : BibleRepository {
    private val bibleVersionKey = stringPreferencesKey(BIBLE_VERSION_KEY)

    // Why: counting downloaded chapters walks every verse and a download writes thousands
    // of times; recounting per write made the app stutter, so it is throttled.
    private val downloadedChaptersThrottle: Duration = 1.seconds

    private val sharedBiblesFlow: Flow<List<BibleModel>> by lazy {
        combine(
            bibleVersionRepository.observeVersions(),
            getSelectedVersionIdFlow(),
            bibleVersionDao.getAllVersionsFlow(),
            observeDownloadedChaptersPerVersion(),
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
        }.distinctUntilChanged()
            .shareIn(
                scope = applicationScope,
                started = SharingStarted.WhileSubscribed(SHARING_STOP_TIMEOUT_MILLIS),
                replay = 1,
            )
    }

    // Why: shared because every screen showing a version subscribes; otherwise each one
    // pays for its own remote listing and chapter count.
    override fun getBiblesFlow(): Flow<List<BibleModel>> = sharedBiblesFlow

    override fun getSelectedVersionIdFlow(): Flow<String> = dataStore.data
        .map { preferences -> preferences[bibleVersionKey] ?: getDefaultVersion() }
        .distinctUntilChanged()

    override suspend fun setSelectedVersionId(id: String) {
        dataStore.write(
            key = bibleVersionKey,
            value = id,
        )
    }

    private fun observeDownloadedChaptersPerVersion(): Flow<List<VersionChapterCount>> =
        observeTableInvalidation(DatabaseTables.VERSE_TEXTS)
            .throttleLatest(downloadedChaptersThrottle)
            .map { verseDao.getDownloadedChaptersPerVersion() }
            .distinctUntilChanged()

    private fun getDefaultVersion(): String = when (languageProvider.getAppLanguage()) {
        Language.PORTUGUESE_BRAZIL -> "ACF"
        Language.SPANISH -> "RVR1960"
        Language.ENGLISH -> "WEB"
    }

    companion object {
        private const val BIBLE_VERSION_KEY = "selected_bible_version"
        private const val SHARING_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
