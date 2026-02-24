package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel

/**
 * Toggle read state for the given passages.
 *
 * - If ALL targeted passages are already read -> mark them as unread.
 * - Otherwise -> mark all targeted passages as read.
 *
 * "Read" / "unread" is persisted into the Room database using the existing
 * Book / Chapter / Verse read flags.
 */
class UpdatePassageReadStatusUseCase(
    private val updateBookReadStatus: UpdateBookReadStatusUseCase,
    private val areAllPassagesRead: AreAllPassagesReadUseCase,
    private val updateWholeChapterReadStatus: UpdateWholeChapterReadStatusUseCase,
    private val updateSpecificRangeChapterReadStatus: UpdateSpecificRangeChapterReadStatusUseCase,
) {
    suspend operator fun invoke(passage: PassageModel) {
        invoke(listOf(passage))
    }

    suspend operator fun invoke(passages: List<PassageModel>) {
        if (passages.isEmpty()) return

        // Determine if the whole set is currently read so we can toggle.
        val isCurrentlyFullyRead = areAllPassagesRead(passages)
        val targetRead = !isCurrentlyFullyRead

        passages.forEach { passage ->
            if (passage.chapters.isEmpty()) {
                // No specific chapters -> toggle the whole book (including chapters and verses)
                updateBookReadStatus(
                    bookId = passage.bookId,
                    isRead = targetRead,
                )
            } else {
                passage.chapters.forEach { chapterPlan ->
                    updateChapterReadStatus(
                        chapterPlan = chapterPlan,
                        isRead = targetRead,
                    )
                }
            }
        }
    }

    private suspend fun updateChapterReadStatus(
        chapterPlan: ChapterModel,
        isRead: Boolean,
    ) {
        val bookId = chapterPlan.bookId

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse
        val chapterNumber = chapterPlan.number

        // No verse range -> update the whole chapter (and all its verses)
        if (startVerse == null || endVerse == null) {
            updateWholeChapterReadStatus(
                chapterNumber = chapterNumber,
                isRead = isRead,
                bookId = bookId,
            )
        } else { // Specific range -> update only the verses in that range
            updateSpecificRangeChapterReadStatus(
                startVerse = startVerse,
                endVerse = endVerse,
                isRead = isRead,
                chapterNumber = chapterNumber,
                bookId = bookId,
            )
        }
    }
}
