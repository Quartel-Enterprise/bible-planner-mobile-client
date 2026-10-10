package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.mapper.BibleMapper
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.fake.InMemoryPreferencesDataStore
import com.quare.bibleplanner.core.books.fake.ThrowingBibleVersionDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.provider.room.invalidation.TableInvalidationObserver
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount
import com.quare.bibleplanner.core.provider.room.utils.DatabaseTables
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BibleRepositoryImplTest {
    private val totalChapters = 1189
    private val contentVersion = "1.0.0"
    private val webVersion = VersionModel(
        id = "WEB",
        name = "World English Bible",
        version = contentVersion,
        language = Language.ENGLISH,
        chapters = totalChapters,
        size = null,
    )
    private val kjvVersion = VersionModel(
        id = "KJV",
        name = "King James Version",
        version = contentVersion,
        language = Language.ENGLISH,
        chapters = totalChapters,
        size = null,
    )

    private val versionsInvalidations = MutableSharedFlow<Unit>(replay = 1)

    private lateinit var repository: BibleRepositoryImpl
    private lateinit var bibleVersionDao: StoredBibleVersionDao
    private lateinit var verseDao: CountingVerseDao

    @Test
    fun `GIVEN the app in Portuguese WHEN reading the selected version THEN defaults to the Portuguese version`() =
        runTest {
            // Given
            prepareScenario(appLanguage = Language.PORTUGUESE_BRAZIL)

            // When
            val selectedVersionId = repository.getSelectedVersionIdFlow().first()

            // Then
            assertEquals(expected = "A21", actual = selectedVersionId)
        }

    @Test
    fun `GIVEN the app in Spanish WHEN reading the selected version THEN defaults to the Spanish version`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.SPANISH)

        // When
        val selectedVersionId = repository.getSelectedVersionIdFlow().first()

        // Then
        assertEquals(expected = "RVR1960", actual = selectedVersionId)
    }

    @Test
    fun `GIVEN the app in English WHEN reading the selected version THEN defaults to the English version`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)

        // When
        val selectedVersionId = repository.getSelectedVersionIdFlow().first()

        // Then
        assertEquals(expected = "WEB", actual = selectedVersionId)
    }

    @Test
    fun `GIVEN a stored selection WHEN reading the selected version THEN keeps it instead of the language default`() =
        runTest {
            // Given
            prepareScenario(
                appLanguage = Language.SPANISH,
                storedVersionId = "KJV",
            )

            // When
            val selectedVersionId = repository.getSelectedVersionIdFlow().first()

            // Then
            assertEquals(expected = "KJV", actual = selectedVersionId)
        }

    @Test
    fun `GIVEN a new selection WHEN storing it THEN the selected version flow emits it`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)

        // When
        repository.setSelectedVersionId("NVI")

        // Then
        assertEquals(expected = "NVI", actual = repository.getSelectedVersionIdFlow().first())
    }

    @Test
    fun `GIVEN supported and stored versions WHEN observing the bibles THEN marks the selected one ignoring case`() =
        runTest {
            // Given
            prepareScenario(
                appLanguage = Language.ENGLISH,
                storedVersionId = "kjv",
            )

            // When
            val bibles = repository.getBiblesFlow().first()

            // Then
            assertEquals(
                expected = listOf("WEB" to false, "KJV" to true),
                actual = bibles.map { it.version.id to it.isSelected },
            )
        }

    @Test
    fun `GIVEN downloaded chapter counts WHEN observing the bibles THEN reports each version progress`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)

        // When
        val bibles = repository.getBiblesFlow().first()

        // Then
        assertEquals(
            expected = listOf(
                bible(
                    version = webVersion,
                    downloadedChapters = totalChapters,
                    downloadStatus = DownloadStatusModel.Downloaded,
                    isSelected = true,
                ),
                bible(
                    version = kjvVersion,
                    downloadedChapters = 0,
                    downloadStatus = DownloadStatusModel.NotStarted,
                    isSelected = false,
                ),
            ),
            actual = bibles,
        )
    }

    @Test
    fun `GIVEN a download that just finished WHEN its status changes THEN recounts its chapters with it`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)
        repository.getBiblesFlow().first()
        bibleVersionDao.versions = bibleVersionDao.versions.map { entity ->
            if (entity.id == "KJV") entity.copy(status = DownloadStatus.DONE) else entity
        }
        verseDao.counts = verseDao.counts + VersionChapterCount(
            bibleVersionId = "KJV",
            downloadedChapters = totalChapters,
        )

        // When
        versionsInvalidations.emit(Unit)

        // Then
        val kjv = repository
            .getBiblesFlow()
            .first { bibles ->
                bibles.any { it.version.id == "KJV" && it.downloadStatus == DownloadStatusModel.Downloaded }
            }.single { it.version.id == "KJV" }
        assertEquals(
            expected = totalChapters to false,
            actual = kjv.downloadedChapters to kjv.hasPendingUpdate,
        )
    }

    @Test
    fun `GIVEN version rows rewritten unchanged WHEN observing THEN reuses the last chapter count`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)
        repository.getBiblesFlow().first()
        val countQueriesBefore = verseDao.countQueries
        val versionReadsBefore = bibleVersionDao.reads

        // When
        versionsInvalidations.emit(Unit)
        runCurrent()

        // Then
        assertEquals(
            expected = (versionReadsBefore + 1) to countQueriesBefore,
            actual = bibleVersionDao.reads to verseDao.countQueries,
        )
    }

    private fun bible(
        version: VersionModel,
        downloadedChapters: Int,
        downloadStatus: DownloadStatusModel,
        isSelected: Boolean,
    ): BibleModel = BibleModel(
        version = version,
        downloadedChapters = downloadedChapters,
        downloadStatus = downloadStatus,
        isSelected = isSelected,
        hasPendingUpdate = false,
    )

    private fun TestScope.prepareScenario(
        appLanguage: Language,
        storedVersionId: String? = null,
    ) {
        val preferences = storedVersionId?.let { versionId ->
            mutablePreferencesOf(stringPreferencesKey("selected_bible_version") to versionId)
        } ?: emptyPreferences()
        versionsInvalidations.tryEmit(Unit)
        bibleVersionDao = StoredBibleVersionDao(
            listOf(
                BibleVersionEntity(
                    id = "WEB",
                    status = DownloadStatus.DONE,
                    totalChapters = totalChapters,
                    contentVersion = contentVersion,
                ),
                BibleVersionEntity(
                    id = "KJV",
                    status = DownloadStatus.NOT_STARTED,
                    totalChapters = totalChapters,
                    contentVersion = contentVersion,
                ),
            ),
        )
        verseDao = CountingVerseDao(
            listOf(
                VersionChapterCount(
                    bibleVersionId = "WEB",
                    downloadedChapters = totalChapters,
                ),
            ),
        )
        repository = BibleRepositoryImpl(
            bibleVersionDao = bibleVersionDao,
            verseDao = verseDao,
            bibleVersionRepository = StubBibleVersionRepository(listOf(webVersion, kjvVersion)),
            bibleMapper = BibleMapper(DownloadStatusMapper()),
            dataStore = InMemoryPreferencesDataStore(preferences),
            languageProvider = FixedLanguageProvider(appLanguage),
            observeTableInvalidation = TableInvalidationObserver { tables ->
                if (DatabaseTables.BIBLE_VERSIONS in tables) versionsInvalidations else flowOf(Unit)
            },
            applicationScope = ApplicationScope(backgroundScope),
        )
    }
}

private class FixedLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("Unexpected call")

    override fun getAppLanguage(): Language = appLanguage
}

private class StubBibleVersionRepository(
    private val versions: List<VersionModel>,
) : BibleVersionRepository {
    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> = error("Unexpected call")

    override fun observeVersions(): Flow<List<VersionModel>> = flowOf(versions)
}

private class StoredBibleVersionDao(
    var versions: List<BibleVersionEntity>,
) : ThrowingBibleVersionDao() {
    var reads = 0
        private set

    override suspend fun getAllVersions(): List<BibleVersionEntity> {
        reads += 1
        return versions
    }
}

private class CountingVerseDao(
    var counts: List<VersionChapterCount>,
) : ThrowingVerseDao() {
    var countQueries = 0
        private set

    override suspend fun getDownloadedChaptersPerVersion(): List<VersionChapterCount> {
        countQueries += 1
        return counts
    }
}
