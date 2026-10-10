package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryChapterDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.StorageServer
import com.quare.bibleplanner.feature.bibleversion.fake.chaptersOfOtherBooks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DownloadBooksInParallelUseCaseTest {
    private lateinit var useCase: DownloadBooksInParallelUseCase
    private lateinit var verseDao: InMemoryVerseDao

    @Test
    fun `GIVEN every book available WHEN downloading the version THEN downloads every book`() = runTest {
        // Given
        prepareScenario(files = mapOf(GENESIS_PATH to CHAPTER_JSON, MATTHEW_PATH to CHAPTER_JSON))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            contentVersion = NO_CONTENT_VERSION,
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = setOf(1L, 2L),
            actual = verseDao.savedVerseTextBatches
                .flatten()
                .map { it.verseId }
                .toSet(),
        )
    }

    @Test
    fun `GIVEN a book that fails to download WHEN downloading the version THEN fails`() = runTest {
        // Given
        prepareScenario(files = mapOf(GENESIS_PATH to CHAPTER_JSON))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            contentVersion = NO_CONTENT_VERSION,
        )

        // Then
        assertEquals(
            expected = "1 chapters of MAT failed to download",
            actual = result.exceptionOrNull()?.message,
        )
    }

    private fun prepareScenario(files: Map<String, String>) {
        val otherBooksChapters = chaptersOfOtherBooks(BookId.GEN, BookId.MAT)
        verseDao = InMemoryVerseDao(
            verses = listOf(
                VerseEntity(
                    id = 1L,
                    number = 1,
                    chapterId = 10L,
                    isRead = false,
                    readUpdatedAt = null,
                    isReadPendingSync = false,
                ),
                VerseEntity(
                    id = 2L,
                    number = 1,
                    chapterId = 20L,
                    isRead = false,
                    readUpdatedAt = null,
                    isReadPendingSync = false,
                ),
            ),
            downloadedChapterIds = otherBooksChapters.map { it.id },
        )
        useCase = DownloadBooksInParallelUseCase(
            getPrioritizedBookIds = GetPrioritizedBookIdsUseCase(
                getPentateuchIds = GetPentateuchIdsUseCase(),
                getNewTestamentIds = GetNewTestamentIdsUseCase(),
            ),
            downloadChapters = DownloadChaptersUseCase(
                supabaseBookAbbreviationMapper = SupabaseBookAbbreviationMapper(),
                chapterDao = InMemoryChapterDao(
                    listOf(
                        ChapterEntity(
                            id = 10L,
                            number = 1,
                            bookId = BookId.GEN.name,
                            isRead = false,
                            readUpdatedAt = null,
                            isReadPendingSync = false,
                        ),
                        ChapterEntity(
                            id = 20L,
                            number = 1,
                            bookId = BookId.MAT.name,
                            isRead = false,
                            readUpdatedAt = null,
                            isReadPendingSync = false,
                        ),
                    ) + otherBooksChapters,
                ),
                verseDao = verseDao,
                bucketApi = StorageServer(filesByPath = files).bucketApi,
            ),
        )
    }

    private companion object {
        const val VERSION_ID = "acf"
        const val NO_CONTENT_VERSION = ""
        const val GENESIS_PATH = "bible/ACF/Gn/1.json"
        const val MATTHEW_PATH = "bible/ACF/Mt/1.json"
        const val CHAPTER_JSON = """{"chapter":1,"verses":[{"number":1,"text":"Verse one"}]}"""
    }
}
