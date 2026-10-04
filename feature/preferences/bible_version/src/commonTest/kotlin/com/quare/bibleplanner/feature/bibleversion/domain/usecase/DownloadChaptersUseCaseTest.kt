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
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        ),
        ChapterEntity(
            id = 2L,
            number = 2,
            bookId = BookId.GEN.name,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        ),
        ChapterEntity(
            id = 3L,
            number = 3,
            bookId = BookId.GEN.name,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        ),
    )
    private val verses = listOf(
        VerseEntity(
            id = 11L,
            number = 1,
            chapterId = 1L,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        ),
        VerseEntity(
            id = 12L,
            number = 2,
            chapterId = 1L,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        ),
        VerseEntity(
            id = 21L,
            number = 1,
            chapterId = 2L,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        ),
    )
    private lateinit var useCase: DownloadChaptersUseCase
    private lateinit var verseDao: InMemoryVerseDao
    private lateinit var server: StorageServer

    @Test
    fun `GIVEN missing chapters WHEN downloading the book THEN saves the text of every known verse in one write`() =
        runTest {
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
                            id = 0,
                        ),
                        VerseTextEntity(
                            verseId = 12L,
                            bibleVersionId = VERSION_ID,
                            text = "And the earth was without form",
                            heading = null,
                            id = 0,
                        ),
                        VerseTextEntity(
                            verseId = 21L,
                            bibleVersionId = VERSION_ID,
                            text = "Thus the heavens were finished",
                            heading = null,
                            id = 0,
                        ),
                    ),
                ),
                actual = verseDao.savedVerseTextBatches,
            )
        }

    @Test
    fun `GIVEN a chapter already downloaded WHEN downloading the book THEN skips it`() = runTest {
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
    fun `GIVEN a chapter that fails twice WHEN downloading the book THEN retries it`() = runTest {
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
    fun `GIVEN a chapter that never downloads WHEN downloading the book THEN fails but keeps the others`() = runTest {
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
    fun `GIVEN a book already downloaded WHEN downloading it THEN does nothing`() = runTest {
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
