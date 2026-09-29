package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
fun interface AnnotatedVersionDao {
    @Query(
        "SELECT bibleVersionId FROM verse_highlights WHERE color IS NOT NULL " +
            "UNION SELECT bibleVersionId FROM saved_verses WHERE isSaved = 1 " +
            "UNION SELECT bibleVersionId FROM verse_notes WHERE isDeleted = 0",
    )
    fun getAnnotatedVersionIdsFlow(): Flow<List<String>>
}
