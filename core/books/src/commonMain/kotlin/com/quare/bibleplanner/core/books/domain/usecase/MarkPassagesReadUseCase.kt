package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
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
) {
    suspend operator fun invoke(passages: List<PassagePlanModel>) {
        if (passages.isEmpty()) return

        // Determine if the whole set is currently read so we can toggle.
        val isCurrentlyFullyRead = passages.all { passage ->
            isPassageRead(passage)
        }
        val targetRead = !isCurrentlyFullyRead

        passages.forEach { passage ->
            val bookId = passage.bookId.name

            if (passage.chapters.isEmpty()) {
                // No specific chapters -> toggle the whole book (including chapters and verses)
                bookDao.updateBookReadStatus(
                    bookId = bookId,
                    isRead = targetRead,
                )
                chapterDao.updateChaptersReadStatusByBook(
                    bookId = bookId,
                    isRead = targetRead,
                )
                verseDao.updateVersesReadStatusByBook(
                    bookId = bookId,
                    isRead = targetRead,
                )
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

    private suspend fun isPassageRead(passage: PassagePlanModel): Boolean {
        val bookId = passage.bookId.name
        val book = bookDao.getBookByIdSuspend(bookId) ?: return false

        // If no chapters specified (empty list), check if entire book is read
        if (passage.chapters.isEmpty()) {
            return book.isRead
        }

        return passage.chapters.all { chapterPlan ->
            isChapterPlanRead(
                bookId = bookId,
                chapterPlan = chapterPlan,
            )
        }
    }

    private suspend fun isChapterPlanRead(
        bookId: String,
        chapterPlan: ChapterPlanModel,
    ): Boolean {
        val chapter = chapterDao.getChapterByBookIdAndNumber(
            bookId = bookId,
            chapterNumber = chapterPlan.number,
        ) ?: return false

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        return when {
            // If verse range is specified, check those specific verses
            startVerse != null && endVerse != null -> {
                val verses = verseDao.getVersesByChapterId(chapter.id)
                val requiredVerses = startVerse..endVerse
                requiredVerses.all { verseNumber ->
                    verses.find { it.number == verseNumber }?.isRead == true
                }
            }

            // If only start verse is specified, check from that verse to end of chapter
            startVerse != null -> {
                val verses = verseDao.getVersesByChapterId(chapter.id)
                verses
                    .filter { it.number >= startVerse }
                    .all { it.isRead }
            }

            // If no verse range specified, check if entire chapter is read
            else -> {
                chapter.isRead
            }
        }
    }

    private suspend fun updateChapterPlanRead(
        bookId: String,
        chapterPlan: ChapterPlanModel,
        isRead: Boolean,
    ) {
        val chapter = chapterDao.getChapterByBookIdAndNumber(
            bookId = bookId,
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
                val verses = verseDao.getVersesByChapterId(chapter.id)

                val targetVerses = verses.filter { verse ->
                    when {
                        startVerse != null && endVerse != null -> {
                            verse.number in startVerse..endVerse
                        }

                        startVerse != null -> {
                            verse.number >= startVerse
                        }

                        else -> {
                            // Only endVerse defined: apply up to that verse
                            endVerse != null && verse.number <= endVerse
                        }
                    }
                }

                targetVerses.forEach { verse ->
                    if (verse.isRead != isRead) {
                        verseDao.updateVerseReadStatus(
                            verseId = verse.id,
                            isRead = isRead,
                        )
                    }
                }
            }
        }
    }
}
