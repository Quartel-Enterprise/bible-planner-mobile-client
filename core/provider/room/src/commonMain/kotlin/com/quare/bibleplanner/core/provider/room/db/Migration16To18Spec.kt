package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

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
