package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel

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

        val isCurrentlyFullyRead = areAllPassagesRead(passages)
        val targetRead = !isCurrentlyFullyRead

        passages.forEach { passage ->
            if (passage.chapters.isEmpty()) {
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

        if (startVerse == null || endVerse == null) {
            updateWholeChapterReadStatus(
                chapterNumber = chapterNumber,
                isRead = isRead,
                bookId = bookId,
            )
        } else {
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
