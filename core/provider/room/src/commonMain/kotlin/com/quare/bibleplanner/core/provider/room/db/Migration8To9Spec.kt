package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.quare.bibleplanner.core.model.book.BookId

/*
 * Why: JHN wrongly shared the "Jo" abbreviation with JOB, so John texts came from Job; versions
 * go back to IN_PROGRESS and the texts are deleted so the resumed download refetches John.
 */
class Migration8To9Spec : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
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
