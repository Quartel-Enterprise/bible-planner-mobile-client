package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity

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
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val updateBookReadStatus: UpdateBookReadStatusUseCase,
    private val isPassageRead: IsPassageReadUseCase,
) {
    suspend operator fun invoke(passage: PassageModel) {
        invoke(listOf(passage))
    }

    suspend operator fun invoke(passages: List<PassageModel>) {
        if (passages.isEmpty()) return

        // Determine if the whole set is currently read so we can toggle.
        val isCurrentlyFullyRead = passages.all { passage ->
            isPassageRead(passage)
        }
        val targetRead = !isCurrentlyFullyRead

        passages.forEach { passage ->
            if (passage.chapters.isEmpty()) {
                // No specific chapters -> toggle the whole book (including chapters and verses)
                updateBookReadStatus(
                    bookId = passage.bookId,
                    isRead = targetRead
                )
            } else {
                passage.chapters.forEach { chapterPlan ->
                    updateChapterPlanReadStatus(
                        chapterPlan = chapterPlan,
                        isRead = targetRead,
                    )
                }
            }
        }
    }

    private suspend fun updateChapterPlanReadStatus(
        chapterPlan: ChapterModel,
        isRead: Boolean,
    ) {
        val bookId = chapterPlan.bookId
        val chapter = chapterDao.getChapterByBookIdAndNumber(
            bookId = bookId.name,
            chapterNumber = chapterPlan.number,
        ) ?: return

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        // No verse range -> update the whole chapter (and all its verses)
        if (startVerse == null && endVerse == null) {
            updateWholeChapterReadStatus(
                chapterId = chapter.id,
                isRead = isRead
            )
        } else if (startVerse != null && endVerse != null) { // Specific range -> update only the verses in that range
            updateSpecificRangeChapterReadStatus(chapter, startVerse, endVerse, isRead)
        }

        // Check if the whole book status needs update
        updateWholeBookReadStatusIfNeeded(bookId)
    }

    private suspend fun updateWholeChapterReadStatus(
        chapterId: Long,
        isRead: Boolean,
    ) {
        chapterDao.updateChapterReadStatus(
            chapterId = chapterId,
            isRead = isRead,
        )
        verseDao.updateVersesReadStatusByChapter(
            chapterId = chapterId,
            isRead = isRead,
        )
    }

    private suspend fun updateSpecificRangeChapterReadStatus(
        chapter: ChapterEntity,
        startVerse: Int,
        endVerse: Int,
        isRead: Boolean,
    ) {
        verseDao.updateVerseReadStatusRange(
            chapterId = chapter.id,
            startVerse = startVerse,
            endVerse = endVerse,
            isRead = isRead,
        )

        // Check if Chapter status needs update after modifying verses
        val verses = verseDao.getVersesByChapterId(chapter.id)
        val isChapterFullyRead = verses.isNotEmpty() && verses.all { it.isRead }

        if (chapter.isRead != isChapterFullyRead) {
            chapterDao.updateChapterReadStatus(chapter.id, isChapterFullyRead)
        }
    }

    private suspend fun updateWholeBookReadStatusIfNeeded(bookId: BookId) {
        val updatedChaptersInBook = chapterDao.getChaptersByBookId(bookId.name)
        val allChaptersRead = updatedChaptersInBook.all { it.isRead }

        val currentBook = bookDao.getBookById(bookId.name)
        if (currentBook?.isRead != allChaptersRead) {
            bookDao.updateBookReadStatus(bookId.name, allChaptersRead)
        }
    }
}
