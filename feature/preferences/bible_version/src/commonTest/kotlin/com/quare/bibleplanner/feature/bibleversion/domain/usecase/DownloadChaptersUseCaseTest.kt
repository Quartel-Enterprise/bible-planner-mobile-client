package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryChapterDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.StorageServer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DownloadChaptersUseCaseTest {
    private val chapters = listOf(
        ChapterEntity(
            id = 1L,
            number = 1,
            bookId = BookId.GEN.name,
        ),
        ChapterEntity(
            id = 2L,
            number = 2,
            bookId = BookId.GEN.name,
        ),
        ChapterEntity(
            id = 3L,
            number = 3,
            bookId = BookId.GEN.name,
        ),
    )
    private val verses = listOf(
        VerseEntity(
            id = 11L,
            number = 1,
            chapterId = 1L,
        ),
        VerseEntity(
            id = 12L,
            number = 2,
            chapterId = 1L,
        ),
        VerseEntity(
            id = 21L,
            number = 1,
            chapterId = 2L,
        ),
    )
    private lateinit var useCase: DownloadChaptersUseCase
    private lateinit var verseDao: InMemoryVerseDao
    private lateinit var server: StorageServer

    @Test
    fun `downloads the missing chapters and saves the text of every known verse in one write`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = listOf(
                listOf(
                    VerseTextEntity(
                        verseId = 11L,
                        bibleVersionId = VERSION_ID,
                        text = "In the beginning",
                        heading = "The creation",
                    ),
                    VerseTextEntity(
                        verseId = 12L,
                        bibleVersionId = VERSION_ID,
                        text = "And the earth was without form",
                        heading = null,
                    ),
                    VerseTextEntity(
                        verseId = 21L,
                        bibleVersionId = VERSION_ID,
                        text = "Thus the heavens were finished",
                        heading = null,
                    ),
                ),
            ),
            actual = verseDao.savedVerseTextBatches,
        )
    }

    @Test
    fun `skips the chapters this version already downloaded`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
        )

        // Then
        assertEquals(
            expected = setOf(CHAPTER_ONE_PATH, CHAPTER_TWO_PATH),
            actual = server.requestedPaths.toSet(),
        )
    }

    @Test
    fun `retries a chapter that failed to download`() = runTest {
        // Given
        prepareScenario(failuresBeforeSuccessByPath = mapOf(CHAPTER_ONE_PATH to 2))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = 3,
            actual = server.requestedPaths.count { it == CHAPTER_ONE_PATH },
        )
    }

    @Test
    fun `fails after the last attempt but keeps the chapters that did download`() = runTest {
        // Given
        prepareScenario(files = mapOf(CHAPTER_ONE_PATH to CHAPTER_ONE_JSON))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
        )

        // Then
        assertEquals(
            expected = "1 chapters of GEN failed to download",
            actual = result.exceptionOrNull()?.message,
        )
        assertEquals(
            expected = 3,
            actual = server.requestedPaths.count { it == CHAPTER_TWO_PATH },
        )
        assertEquals(
            expected = listOf(11L, 12L),
            actual = verseDao.savedVerseTextBatches.single().map { it.verseId },
        )
    }

    @Test
    fun `does nothing for a book that is already downloaded`() = runTest {
        // Given
        prepareScenario(downloadedChapterIds = listOf(1L, 2L, 3L))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
        )

        // Then
        assertTrue(result.isSuccess)
        assertTrue(server.requestedPaths.isEmpty())
        assertTrue(verseDao.savedVerseTextBatches.isEmpty())
    }

    private fun prepareScenario(
        files: Map<String, String> = mapOf(
            CHAPTER_ONE_PATH to CHAPTER_ONE_JSON,
            CHAPTER_TWO_PATH to CHAPTER_TWO_JSON,
        ),
        failuresBeforeSuccessByPath: Map<String, Int> = emptyMap(),
        downloadedChapterIds: List<Long> = listOf(3L),
    ) {
        server = StorageServer(
            filesByPath = files,
            failuresBeforeSuccessByPath = failuresBeforeSuccessByPath,
        )
        verseDao = InMemoryVerseDao(
            verses = verses,
            downloadedChapterIds = downloadedChapterIds,
        )
        useCase = DownloadChaptersUseCase(
            supabaseBookAbbreviationMapper = SupabaseBookAbbreviationMapper(),
            chapterDao = InMemoryChapterDao(chapters),
            verseDao = verseDao,
            bucketApi = server.bucketApi,
        )
    }

    private companion object {
        const val VERSION_ID = "acf"
        const val CHAPTER_ONE_PATH = "bible/ACF/Gn/1.json"
        const val CHAPTER_TWO_PATH = "bible/ACF/Gn/2.json"
        const val CHAPTER_ONE_JSON =
            """{"chapter":1,"verses":[{"number":1,"text":"In the beginning","heading":"The creation"},""" +
                """{"number":2,"text":"And the earth was without form"},{"number":99,"text":"Unknown verse"}]}"""
        const val CHAPTER_TWO_JSON = """{"chapter":2,"verses":[{"number":1,"text":"Thus the heavens were finished"}]}"""
    }
}
