package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryChapterDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.StorageServer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DownloadBooksInParallelUseCaseTest {
    private lateinit var useCase: DownloadBooksInParallelUseCase
    private lateinit var verseDao: InMemoryVerseDao

    @Test
    fun `downloads every book of the version`() = runTest {
        // Given
        prepareScenario(files = mapOf(GENESIS_PATH to CHAPTER_JSON, MATTHEW_PATH to CHAPTER_JSON))

        // When
        val result = useCase(VERSION_ID)

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
    fun `fails when a book fails to download`() = runTest {
        // Given
        prepareScenario(files = mapOf(GENESIS_PATH to CHAPTER_JSON))

        // When
        val result = useCase(VERSION_ID)

        // Then
        assertEquals(
            expected = "1 chapters of MAT failed to download",
            actual = result.exceptionOrNull()?.message,
        )
    }

    private fun prepareScenario(files: Map<String, String>) {
        verseDao = InMemoryVerseDao(
            verses = listOf(
                VerseEntity(
                    id = 1L,
                    number = 1,
                    chapterId = 10L,
                ),
                VerseEntity(
                    id = 2L,
                    number = 1,
                    chapterId = 20L,
                ),
            ),
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
                        ),
                        ChapterEntity(
                            id = 20L,
                            number = 1,
                            bookId = BookId.MAT.name,
                        ),
                    ),
                ),
                verseDao = verseDao,
                bucketApi = StorageServer(filesByPath = files).bucketApi,
            ),
        )
    }

    private companion object {
        const val VERSION_ID = "acf"
        const val GENESIS_PATH = "bible/ACF/Gn/1.json"
        const val MATTHEW_PATH = "bible/ACF/Mt/1.json"
        const val CHAPTER_JSON = """{"chapter":1,"verses":[{"number":1,"text":"Verse one"}]}"""
    }
}
