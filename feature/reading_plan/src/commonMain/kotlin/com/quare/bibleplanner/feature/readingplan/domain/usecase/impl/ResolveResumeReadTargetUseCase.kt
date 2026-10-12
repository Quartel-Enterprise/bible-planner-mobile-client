package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.isChapterRead
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.readingplan.domain.model.ResumeReadTarget
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveResumeReadTarget
import kotlinx.coroutines.flow.first

internal class ResolveResumeReadTargetUseCase(
    private val booksRepository: BooksRepository,
) : ResolveResumeReadTarget {
    override suspend operator fun invoke(passages: List<PassageModel>): Result<ResumeReadTarget?> = suspendRunCatching {
        /*
         * Why: a tap only needs the books up to the first passage that still has something to read,
         * so each one is read when its passage is examined and read passages cost no query.
         */
        val books = mutableMapOf<BookId, BookDataModel?>()

        suspend fun bookOf(bookId: BookId): BookDataModel? = books.getOrPut(bookId) {
            booksRepository.getBookByIdFlow(bookId).first()
        }
        var target: ResumeReadTarget? = null
        for (passage in passages) {
            if (passage.isRead) continue
            target = passage.toFirstUnreadTarget(bookOf(passage.bookId))
            if (target != null) break
        }
        target ?: passages.firstOrNull()?.let { passage -> passage.toFirstChapterTarget(bookOf(passage.bookId)) }
    }.onFailure { error -> Logger.e(error) { "Failed to resolve the chapter to resume reading" } }

    private fun PassageModel.toFirstUnreadTarget(book: BookDataModel?): ResumeReadTarget? {
        val chapterNumber = if (chapters.isEmpty()) {
            // Why: an empty chapter list means the whole book, so resume at its first unread chapter.
            val bookChapters = book?.chapters.orEmpty()
            if (bookChapters.isEmpty()) {
                FIRST_CHAPTER.takeUnless { isRead }
            } else {
                bookChapters.firstOrNull { !it.isRead }?.number
            }
        } else {
            chapters.firstOrNull { chapter -> !book.isChapterRead(chapter) }?.number
        }
        return chapterNumber?.let { number ->
            ResumeReadTarget(
                bookId = bookId,
                chapterNumber = number,
                isChapterRead = false,
            )
        }
    }

    private fun PassageModel.toFirstChapterTarget(book: BookDataModel?): ResumeReadTarget {
        val chapterNumber = chapters.firstOrNull()?.number ?: FIRST_CHAPTER
        // Why: the reader shows the whole chapter's state, and a read verse range doesn't make it read.
        val isChapterRead = book
            ?.chapters
            ?.find { it.number == chapterNumber }
            ?.isRead == true
        return ResumeReadTarget(
            bookId = bookId,
            chapterNumber = chapterNumber,
            isChapterRead = isChapterRead,
        )
    }

    private companion object {
        const val FIRST_CHAPTER = 1
    }
}
