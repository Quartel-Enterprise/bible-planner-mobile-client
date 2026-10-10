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
                contentVersion = NO_CONTENT_VERSION,
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
            contentVersion = NO_CONTENT_VERSION,
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
            contentVersion = NO_CONTENT_VERSION,
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
        prepareScenario(failuresBeforeSuccessByPath = mapOf(CHAPTER_TWO_PATH to MAX_DOWNLOAD_ATTEMPTS))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
            contentVersion = NO_CONTENT_VERSION,
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
    fun `GIVEN a book with no chapter rows WHEN downloading it THEN fails without requesting files`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.EXO,
            contentVersion = NO_CONTENT_VERSION,
        )

        // Then
        assertEquals(
            expected = "EXO has no chapters to download",
            actual = result.exceptionOrNull()?.message,
        )
        assertTrue(server.requestedPaths.isEmpty())
    }

    @Test
    fun `GIVEN a book already downloaded WHEN downloading it THEN does nothing`() = runTest {
        // Given
        prepareScenario(downloadedChapterIds = listOf(1L, 2L, 3L))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
            contentVersion = NO_CONTENT_VERSION,
        )

        // Then
        assertTrue(result.isSuccess)
        assertTrue(server.requestedPaths.isEmpty())
        assertTrue(verseDao.savedVerseTextBatches.isEmpty())
    }

    @Test
    fun `GIVEN a book file WHEN downloading the book THEN saves its missing chapters with one request`() = runTest {
        // Given
        prepareScenario(files = mapOf(BOOK_PATH to BOOK_JSON))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
            contentVersion = CONTENT_VERSION,
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = listOf(BOOK_PATH),
            actual = server.requestedPaths,
        )
        assertEquals(
            expected = listOf(listOf(11L, 12L, 21L)),
            actual = verseDao.savedVerseTextBatches.map { batch -> batch.map { it.verseId } },
        )
    }

    @Test
    fun `GIVEN no book file WHEN downloading the book THEN falls back to its chapter files`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
            contentVersion = CONTENT_VERSION,
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = 1,
            actual = server.requestedPaths.count { it == BOOK_PATH },
        )
        assertEquals(
            expected = listOf(11L, 12L, 21L),
            actual = verseDao.savedVerseTextBatches
                .flatten()
                .map { it.verseId },
        )
    }

    @Test
    fun `GIVEN a missing chapter file WHEN downloading the book THEN asks for it only once`() = runTest {
        // Given
        prepareScenario(files = mapOf(CHAPTER_ONE_PATH to CHAPTER_ONE_JSON))

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
            contentVersion = NO_CONTENT_VERSION,
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals(
            expected = 1,
            actual = server.requestedPaths.count { it == CHAPTER_TWO_PATH },
        )
    }

    @Test
    fun `GIVEN a book file that fails once WHEN downloading the book THEN retries it instead of the chapters`() =
        runTest {
            // Given
            prepareScenario(
                files = mapOf(BOOK_PATH to BOOK_JSON),
                failuresBeforeSuccessByPath = mapOf(BOOK_PATH to 1),
            )

            // When
            val result = useCase(
                versionId = VERSION_ID,
                bookId = BookId.GEN,
                contentVersion = CONTENT_VERSION,
            )

            // Then
            assertTrue(result.isSuccess)
            assertEquals(
                expected = listOf(BOOK_PATH, BOOK_PATH),
                actual = server.requestedPaths,
            )
        }

    @Test
    fun `GIVEN a book file missing a chapter WHEN downloading the book THEN downloads that chapter alone`() = runTest {
        // Given
        prepareScenario(
            files = mapOf(
                BOOK_PATH to """{"chapters":[$CHAPTER_ONE_JSON]}""",
                CHAPTER_TWO_PATH to CHAPTER_TWO_JSON,
            ),
        )

        // When
        val result = useCase(
            versionId = VERSION_ID,
            bookId = BookId.GEN,
            contentVersion = CONTENT_VERSION,
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = listOf(BOOK_PATH, CHAPTER_TWO_PATH),
            actual = server.requestedPaths,
        )
    }

    @Test
    fun `GIVEN a book already downloaded WHEN downloading it with a content version THEN skips the book file`() =
        runTest {
            // Given
            prepareScenario(
                files = mapOf(BOOK_PATH to BOOK_JSON),
                downloadedChapterIds = listOf(1L, 2L, 3L),
            )

            // When
            useCase(
                versionId = VERSION_ID,
                bookId = BookId.GEN,
                contentVersion = CONTENT_VERSION,
            )

            // Then
            assertTrue(server.requestedPaths.isEmpty())
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
        const val NO_CONTENT_VERSION = ""
        const val MAX_DOWNLOAD_ATTEMPTS = 3
        const val CONTENT_VERSION = "1.1.0"
        const val BOOK_PATH = "bible/ACF/books/1.1.0/Gn.json"
        const val CHAPTER_ONE_PATH = "bible/ACF/Gn/1.json"
        const val CHAPTER_TWO_PATH = "bible/ACF/Gn/2.json"
        const val CHAPTER_ONE_JSON =
            """{"chapter":1,"verses":[{"number":1,"text":"In the beginning","heading":"The creation"},""" +
                """{"number":2,"text":"And the earth was without form"},{"number":99,"text":"Unknown verse"}]}"""
        const val CHAPTER_TWO_JSON = """{"chapter":2,"verses":[{"number":1,"text":"Thus the heavens were finished"}]}"""
        const val BOOK_JSON = """{"version":"ACF","book":"Gn","chapters":[$CHAPTER_ONE_JSON,$CHAPTER_TWO_JSON]}"""
    }
}
