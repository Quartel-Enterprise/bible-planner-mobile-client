package com.quare.bibleplanner.core.chapterstudy.domain.store

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.ChapterStudyStatusKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/*
 * Why: memory only and never persisted. The quota is shared across devices and the server
 * enforces it on generation, so a stored status is a head start for the reader's tap, not
 * the truth.
 */
internal class ChapterStudyStatusPrefetchStore {
    private val state = MutableStateFlow(
        ChapterStudyStatusPrefetchState(
            clearCount = 0,
            statusesByKey = emptyMap(),
        ),
    )

    fun find(key: ChapterStudyStatusKey): ChapterStudyStatusModel? = state.value.statusesByKey[key]

    // Why: a status fetched before a clear() predates the generation that caused it, so it is not kept.
    suspend fun fetchAndKeep(
        key: ChapterStudyStatusKey,
        fetch: suspend () -> ChapterStudyStatusModel?,
    ): ChapterStudyStatusModel? {
        val clearCountAtStart = state.value.clearCount
        val status = fetch() ?: return null
        state.update { current ->
            if (current.clearCount == clearCountAtStart) {
                current.copy(statusesByKey = current.statusesByKey + (key to status))
            } else {
                current
            }
        }
        return status
    }

    fun clear() {
        state.update { current ->
            ChapterStudyStatusPrefetchState(
                clearCount = current.clearCount + 1,
                statusesByKey = emptyMap(),
            )
        }
    }
}
