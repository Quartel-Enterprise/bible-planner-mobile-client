package com.quare.bibleplanner.core.provider.room.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class Migration8To9SpecTest {
    private val spec = Migration8To9Spec()
    private lateinit var connection: SQLiteConnection

    @BeforeTest
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        connection.execSQL("CREATE TABLE books (id TEXT PRIMARY KEY)")
        connection.execSQL("CREATE TABLE chapters (id INTEGER PRIMARY KEY, number INTEGER, bookId TEXT)")
        connection.execSQL("CREATE TABLE verses (id INTEGER PRIMARY KEY, number INTEGER, chapterId INTEGER)")
        connection.execSQL(
            "CREATE TABLE verse_texts (id INTEGER PRIMARY KEY, verseId INTEGER, bibleVersionId TEXT, text TEXT)",
        )
        connection.execSQL("CREATE TABLE bible_versions (id TEXT PRIMARY KEY, status TEXT)")

        connection.execSQL("INSERT INTO books (id) VALUES ('JHN'), ('JOB'), ('GEN')")
        connection.execSQL(
            "INSERT INTO chapters (id, number, bookId) VALUES (1, 1, 'JHN'), (2, 1, 'JOB'), (3, 1, 'GEN')",
        )
        connection.execSQL(
            "INSERT INTO verses (id, number, chapterId) VALUES (10, 1, 1), (20, 1, 2), (30, 1, 3)",
        )
        connection.execSQL(
            "INSERT INTO verse_texts (id, verseId, bibleVersionId, text) VALUES " +
                "(100, 10, 'WEB', 'There was a man in the land of Uz'), " +
                "(200, 20, 'WEB', 'Job text'), " +
                "(300, 30, 'WEB', 'Genesis text')",
        )
        connection.execSQL(
            "INSERT INTO bible_versions (id, status) VALUES ('WEB', '$DONE_STATUS'), ('ACF', 'NOT_STARTED')",
        )
    }

    @AfterTest
    fun tearDown() {
        connection.close()
    }

    @Test
    fun `GIVEN John texts from a done version WHEN migrating THEN deletes them and re-arms the version`() {
        // When
        spec.onPostMigrate(connection)

        // Then
        assertEquals(0, countVerseTexts(bookId = "JHN"))
        assertEquals("IN_PROGRESS", versionStatus(id = "WEB"))
    }

    @Test
    fun `GIVEN other books texts WHEN migrating THEN keeps them untouched`() {
        // When
        spec.onPostMigrate(connection)

        // Then
        assertEquals(1, countVerseTexts(bookId = "GEN"))
        assertEquals(1, countVerseTexts(bookId = "JOB"))
    }

    @Test
    fun `GIVEN a version without John texts WHEN migrating THEN leaves its status untouched`() {
        // When
        spec.onPostMigrate(connection)

        // Then
        assertEquals("NOT_STARTED", versionStatus(id = "ACF"))
    }

    private fun countVerseTexts(bookId: String): Int {
        val statement = connection.prepare(
            "SELECT COUNT(*) FROM verse_texts vt " +
                "JOIN verses v ON vt.verseId = v.id " +
                "JOIN chapters c ON v.chapterId = c.id " +
                "WHERE c.bookId = ?",
        )
        statement.bindText(index = 1, value = bookId)
        statement.step()
        return statement.getLong(0).toInt().also { statement.close() }
    }

    private fun versionStatus(id: String): String {
        val statement = connection.prepare("SELECT status FROM bible_versions WHERE id = ?")
        statement.bindText(index = 1, value = id)
        statement.step()
        return statement.getText(0).also { statement.close() }
    }

    companion object {
        private const val DONE_STATUS = "DONE"
    }
}
