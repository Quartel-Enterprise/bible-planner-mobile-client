package com.quare.bibleplanner.core.provider.room.db

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.quare.bibleplanner.core.model.book.BookId

/**
 * Repairs the gospel of John, whose verse texts were downloaded from the book of Job's remote
 * directory because [BookId.JHN] wrongly shared the abbreviation `"Jo"` with [BookId.JOB].
 *
 * Re-arms every version that holds corrupted John texts (DONE to IN_PROGRESS) so the launch-time
 * download resumes, then deletes those texts so the resumed download re-fetches John from the
 * corrected source instead of skipping the already-populated chapters.
 */
class Migration8To9Spec : AutoMigrationSpec {
    override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL(
            "UPDATE bible_versions SET status = 'IN_PROGRESS' " +
                "WHERE status = 'DONE' AND id IN (" +
                "SELECT DISTINCT vt.bibleVersionId FROM verse_texts vt " +
                "JOIN verses v ON vt.verseId = v.id " +
                "JOIN chapters c ON v.chapterId = c.id " +
                "WHERE c.bookId = '${BookId.JHN.name}')",
        )
        connection.execSQL(
            "DELETE FROM verse_texts WHERE verseId IN (" +
                "SELECT v.id FROM verses v " +
                "JOIN chapters c ON v.chapterId = c.id " +
                "WHERE c.bookId = '${BookId.JHN.name}')",
        )
    }
}
