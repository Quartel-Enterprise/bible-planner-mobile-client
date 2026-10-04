package com.quare.bibleplanner.core.chapterlistening.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningUnlockRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/*
 * Why: the unlocks only count for the day they were earned, so the set is stored with its date and
 * a stored date other than the asked one reads as no unlock at all.
 */
internal class ChapterListeningUnlockRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ChapterListeningUnlockRepository {
    private val unlockDateKey = stringPreferencesKey("listening_unlock_date")
    private val unlockedChaptersKey = stringSetPreferencesKey("listening_unlocked_chapters")

    override suspend fun getUnlockedChapters(date: LocalDate): Set<ChapterLocationModel> {
        val preferences = dataStore.data.first()
        if (preferences[unlockDateKey] != date.toString()) return emptySet()
        return preferences[unlockedChaptersKey]
            .orEmpty()
            .mapNotNull(::toChapterOrNull)
            .toSet()
    }

    override suspend fun addUnlockedChapter(
        date: LocalDate,
        chapter: ChapterLocationModel,
    ) {
        dataStore.edit { preferences ->
            val storedKeys = preferences[unlockedChaptersKey]
                .orEmpty()
                .takeIf { preferences[unlockDateKey] == date.toString() }
                .orEmpty()
            preferences[unlockDateKey] = date.toString()
            preferences[unlockedChaptersKey] = storedKeys + toKey(chapter)
        }
    }

    private fun toKey(chapter: ChapterLocationModel): String =
        "${chapter.bookId.name}$SEPARATOR${chapter.chapterNumber}"

    private fun toChapterOrNull(key: String): ChapterLocationModel? {
        val parts = key.split(SEPARATOR)
        val bookId = BookId.entries.find { it.name == parts.firstOrNull() } ?: return null
        val chapterNumber = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return ChapterLocationModel(
            bookId = bookId,
            chapterNumber = chapterNumber,
        )
    }

    private companion object {
        const val SEPARATOR = ":"
    }
}
