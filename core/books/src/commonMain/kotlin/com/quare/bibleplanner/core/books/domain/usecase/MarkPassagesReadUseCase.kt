package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

/**
 * Toggle read state for the given passages.
 *
 * - If ALL targeted passages are already read -> mark them as unread.
 * - Otherwise -> mark all targeted passages as read.
 *
 * "Read" / "unread" is persisted into the Room database using the existing
 * Book / Chapter / Verse read flags.
 */
class MarkPassagesReadUseCase(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val markBookRead: MarkBookReadUseCase,
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
            val bookId = passage.bookId

            if (passage.chapters.isEmpty()) {
                // No specific chapters -> toggle the whole book (including chapters and verses)
                markBookRead(bookId = bookId, isRead = targetRead)
            } else {
                passage.chapters.forEach { chapterPlan ->
                    updateChapterPlanRead(
                        bookId = bookId,
                        chapterPlan = chapterPlan,
                        isRead = targetRead,
                    )
                }
            }
        }
    }

    private suspend fun updateChapterPlanRead(
        bookId: BookId,
        chapterPlan: ChapterModel,
        isRead: Boolean,
    ) {
        val chapter = chapterDao.getChapterByBookIdAndNumber(
            bookId = bookId.name,
            chapterNumber = chapterPlan.number,
        ) ?: return

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        when {
            // No verse range -> update the whole chapter (and all its verses)
            startVerse == null && endVerse == null -> {
                chapterDao.updateChapterReadStatus(
                    chapterId = chapter.id,
                    isRead = isRead,
                )
                verseDao.updateVersesReadStatusByChapter(
                    chapterId = chapter.id,
                    isRead = isRead,
                )
            }

            // Specific range -> update only the verses in that range
            else -> {
                verseDao.updateVerseReadStatusRange(
                    chapterId = chapter.id,
                    startVerse = startVerse ?: 0,
                    endVerse = endVerse ?: Int.MAX_VALUE,
                    isRead = isRead,
                )

                // Check if Chapter status needs update after modifying verses
                val verses = verseDao.getVersesByChapterId(chapter.id)
                val isChapterFullyRead = verses.isNotEmpty() && verses.all { it.isRead }

                if (chapter.isRead != isChapterFullyRead) {
                    chapterDao.updateChapterReadStatus(chapter.id, isChapterFullyRead)
                }
            }
        }

        // Check if the whole book status needs update
        val updatedChaptersInBook = chapterDao.getChaptersByBookId(bookId.name)
        val allChaptersRead = updatedChaptersInBook.all { it.isRead }

        val currentBook = bookDao.getBookById(bookId.name)
        if (currentBook?.isRead != allChaptersRead) {
            bookDao.updateBookReadStatus(bookId.name, allChaptersRead)
        }
    }
}
