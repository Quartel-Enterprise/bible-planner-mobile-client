package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/*
 * Why: bundled books_by_chapter verse counts were wrong (Genesis 34-45, tradition splits such as
 * Deut 12-13); missing rows silently dropped downloaded verses, so a chapter that gains rows
 * loses its texts and its DONE versions go back to IN_PROGRESS (as Migration8To9Spec) to refetch.
 */
class Migration16To18Spec(
    private val corrections: List<VerseCountCorrection> = VERSE_COUNT_CORRECTIONS,
) : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        corrections.forEach { correction -> connection.apply(correction) }
    }

    private suspend fun SQLiteConnection.apply(correction: VerseCountCorrection) {
        val bookId = correction.bookId.name
        val count = correction.verses
        val chapterId = "(SELECT id FROM chapters WHERE bookId = '$bookId' AND number = ${correction.chapter})"
        val chapterVerseIds = "(SELECT id FROM verses WHERE chapterId = $chapterId)"
        /*
         * Why: evaluated before any row changes; once the missing rows exist the chapter no longer
         * lacks them.
         */
        val lacksRows = "(SELECT COUNT(*) FROM verses WHERE chapterId = $chapterId) < $count"

        execSQL(
            "UPDATE bible_versions SET status = 'IN_PROGRESS' WHERE status = 'DONE' AND $lacksRows AND id IN (" +
                "SELECT DISTINCT bibleVersionId FROM verse_texts WHERE verseId IN $chapterVerseIds)",
        )
        execSQL("DELETE FROM verse_texts WHERE $lacksRows AND verseId IN $chapterVerseIds")

        execSQL(
            "DELETE FROM verse_texts WHERE verseId IN " +
                "(SELECT id FROM verses WHERE chapterId = $chapterId AND number > $count)",
        )
        execSQL("DELETE FROM verses WHERE chapterId = $chapterId AND number > $count")

        execSQL(
            "WITH RECURSIVE verse_numbers(number) AS " +
                "(SELECT 1 UNION ALL SELECT number + 1 FROM verse_numbers WHERE number < $count) " +
                "INSERT OR IGNORE INTO verses (number, chapterId, isRead) " +
                "SELECT verse_numbers.number, chapters.id, chapters.isRead FROM verse_numbers " +
                "JOIN chapters ON chapters.bookId = '$bookId' AND chapters.number = ${correction.chapter}",
        )
    }
}
