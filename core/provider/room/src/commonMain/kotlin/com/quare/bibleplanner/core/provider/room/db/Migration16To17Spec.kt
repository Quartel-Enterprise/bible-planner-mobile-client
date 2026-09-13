package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Brings each chapter's verse rows in line with the verse numbers the Bible versions really have.
 *
 * Verse rows are seeded from the bundled `books_by_chapter` counts, and some of those were wrong:
 * Genesis 34-45 carried their neighbours' counts, and chapters whose verse division differs between
 * traditions (Deuteronomy 12-13, Jeremiah 8-9, Daniel 3-4...) had the other tradition's. A chapter with
 * more rows than a version has verses never opened in the reader, and one with fewer rows silently
 * lost its last verses on download, because a verse's text is only saved when its row exists.
 *
 * For every [VerseCountCorrection] this drops the rows past the real count and adds the missing ones,
 * which inherit the chapter's read state. A chapter that gains rows also loses its downloaded texts,
 * and every version that had them goes from DONE back to IN_PROGRESS — as [Migration8To9Spec] does for
 * John — so the launch-time download fetches the chapter again, this time keeping every verse.
 */
class Migration16To17Spec(
    private val corrections: List<VerseCountCorrection> = VERSE_COUNT_CORRECTIONS,
) : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        corrections.forEach { correction -> connection.apply(correction) }
    }

    private fun SQLiteConnection.apply(correction: VerseCountCorrection) {
        val bookId = correction.bookId.name
        val count = correction.verses
        val chapterId = "(SELECT id FROM chapters WHERE bookId = '$bookId' AND number = ${correction.chapter})"
        val chapterVerseIds = "(SELECT id FROM verses WHERE chapterId = $chapterId)"
        // Evaluated before any row changes: once the missing rows exist, the chapter no longer lacks them.
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
