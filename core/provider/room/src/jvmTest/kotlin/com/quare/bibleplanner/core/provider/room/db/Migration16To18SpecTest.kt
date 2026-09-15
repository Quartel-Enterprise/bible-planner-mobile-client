package com.quare.bibleplanner.core.provider.room.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class Migration16To18SpecTest {
    private lateinit var spec: Migration16To18Spec
    private lateinit var connection: SQLiteConnection

    @BeforeTest
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        connection.execSQL(
            "CREATE TABLE chapters (id INTEGER PRIMARY KEY, number INTEGER, bookId TEXT, isRead INTEGER)",
        )
        connection.execSQL(
            "CREATE TABLE verses (id INTEGER PRIMARY KEY AUTOINCREMENT, number INTEGER NOT NULL, " +
                "chapterId INTEGER NOT NULL, isRead INTEGER NOT NULL)",
        )
        connection.execSQL("CREATE UNIQUE INDEX index_verses_chapterId_number ON verses (chapterId, number)")
        connection.execSQL(
            "CREATE TABLE verse_texts (id INTEGER PRIMARY KEY AUTOINCREMENT, verseId INTEGER, bibleVersionId TEXT, text TEXT)",
        )
        connection.execSQL("CREATE TABLE bible_versions (id TEXT PRIMARY KEY, status TEXT)")

        // Genesis 35 was seeded with 4 rows for 2 real verses; Genesis 41 with 2 rows for 4, and is read.
        connection.execSQL(
            "INSERT INTO chapters (id, number, bookId, isRead) VALUES " +
                "(1, $SHRINKING_CHAPTER, 'GEN', 0), (2, $GROWING_CHAPTER, 'GEN', 1), (3, 1, 'EXO', 0)",
        )
        connection.execSQL(
            "INSERT INTO verses (number, chapterId, isRead) VALUES " +
                "(1, 1, 0), (2, 1, 0), (3, 1, 0), (4, 1, 0), (1, 2, 1), (2, 2, 1), (1, 3, 0)",
        )
        connection.execSQL(
            "INSERT INTO verse_texts (verseId, bibleVersionId, text) " +
                "SELECT verses.id, 'WEB', 'text' FROM verses JOIN chapters ON chapters.id = verses.chapterId " +
                "WHERE verses.number <= 2 AND chapters.bookId = 'GEN'",
        )
        connection.execSQL(
            "INSERT INTO verse_texts (verseId, bibleVersionId, text) " +
                "SELECT verses.id, 'ACF', 'text' FROM verses WHERE chapterId IN (1, 3)",
        )
        connection.execSQL(
            "INSERT INTO bible_versions (id, status) VALUES ('WEB', '$DONE'), ('ACF', '$DONE'), ('KJV', '$DONE')",
        )
    }

    @AfterTest
    fun tearDown() {
        connection.close()
    }

    @Test
    fun `GIVEN a chapter with rows past its real count WHEN migrating THEN drops them and keeps the rest`() = runTest {
        // Given
        prepareScenario()

        // When
        spec.onPostMigrate(connection)

        // Then
        assertEquals(listOf(1, 2), verseNumbers(chapterId = 1))
        assertEquals(2, countVerseTexts(chapterId = 1, versionId = "WEB"))
        assertEquals(2, countVerseTexts(chapterId = 1, versionId = "ACF"))
    }

    @Test
    fun `GIVEN a chapter missing rows WHEN migrating THEN adds them with the chapter's read state`() = runTest {
        // Given
        prepareScenario()

        // When
        spec.onPostMigrate(connection)

        // Then
        assertEquals(listOf(1, 2, 3, 4), verseNumbers(chapterId = 2))
        assertEquals(listOf(true, true, true, true), verseReadStates(chapterId = 2))
    }

    @Test
    fun `GIVEN a chapter missing rows WHEN migrating THEN deletes its texts and re-arms the versions that had them`() =
        runTest {
            // Given
            prepareScenario()

            // When
            spec.onPostMigrate(connection)

            // Then
            assertEquals(0, countVerseTexts(chapterId = 2, versionId = "WEB"))
            assertEquals("IN_PROGRESS", versionStatus(id = "WEB"))
        }

    @Test
    fun `GIVEN versions without texts in a growing chapter WHEN migrating THEN leaves them done`() = runTest {
        // Given
        prepareScenario()

        // When
        spec.onPostMigrate(connection)

        // Then
        assertEquals(DONE, versionStatus(id = "ACF"))
        assertEquals(DONE, versionStatus(id = "KJV"))
        assertEquals(1, countVerseTexts(chapterId = 3, versionId = "ACF"))
    }

    @Test
    fun `GIVEN counts that are already correct WHEN migrating again THEN changes nothing`() = runTest {
        // Given
        prepareScenario()
        spec.onPostMigrate(connection)
        connection.execSQL("UPDATE bible_versions SET status = '$DONE'")
        connection.execSQL(
            "INSERT INTO verse_texts (verseId, bibleVersionId, text) SELECT id, 'WEB', 'text' FROM verses WHERE chapterId = 2",
        )

        // When
        spec.onPostMigrate(connection)

        // Then
        assertEquals(listOf(1, 2, 3, 4), verseNumbers(chapterId = 2))
        assertEquals(4, countVerseTexts(chapterId = 2, versionId = "WEB"))
        assertEquals(DONE, versionStatus(id = "WEB"))
    }

    private fun verseNumbers(chapterId: Long): List<Int> =
        queryInts("SELECT number FROM verses WHERE chapterId = $chapterId ORDER BY number")

    private fun verseReadStates(chapterId: Long): List<Boolean> =
        queryInts("SELECT isRead FROM verses WHERE chapterId = $chapterId ORDER BY number").map { it == 1 }

    private fun countVerseTexts(
        chapterId: Long,
        versionId: String,
    ): Int = queryInts(
        "SELECT COUNT(*) FROM verse_texts JOIN verses ON verses.id = verse_texts.verseId " +
            "WHERE verses.chapterId = $chapterId AND verse_texts.bibleVersionId = '$versionId'",
    ).single()

    private fun versionStatus(id: String): String {
        val statement = connection.prepare("SELECT status FROM bible_versions WHERE id = ?")
        statement.bindText(index = 1, value = id)
        statement.step()
        return statement.getText(0).also { statement.close() }
    }

    private fun queryInts(sql: String): List<Int> {
        val statement = connection.prepare(sql)
        val values = buildList { while (statement.step()) add(statement.getLong(0).toInt()) }
        statement.close()
        return values
    }

    private fun prepareScenario() {
        spec = Migration16To18Spec(
            corrections = listOf(
                VerseCountCorrection(bookId = BookId.GEN, chapter = SHRINKING_CHAPTER, verses = 2),
                VerseCountCorrection(bookId = BookId.GEN, chapter = GROWING_CHAPTER, verses = 4),
            ),
        )
    }

    private companion object {
        const val DONE = "DONE"
        const val SHRINKING_CHAPTER = 35
        const val GROWING_CHAPTER = 41
    }
}
