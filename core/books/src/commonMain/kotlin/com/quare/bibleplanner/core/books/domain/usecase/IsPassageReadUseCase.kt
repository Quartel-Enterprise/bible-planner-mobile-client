package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao

class IsPassageReadUseCase(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val isChapterRead: IsChapterReadUseCase,
) {
    suspend operator fun invoke(passage: PassageModel): Boolean {
        val bookId = passage.bookId.name
        val book = bookDao.getBookById(bookId) ?: return false

        return passage.chapters.run {
            // If no chapters specified (empty list), check if entire book is read
            if (isEmpty()) {
                if (book.isRead) {
                    true
                } else {
                    val chapters = chapterDao.getChaptersByBookId(bookId)
                    if (chapters.isEmpty()) {
                        false
                    } else {
                        chapters.all { chapter ->
                            isChapterRead(
                                com.quare.bibleplanner.core.model.plan.ChapterModel(
                                    number = chapter.number,
                                    bookId = passage.bookId,
                                    startVerse = null,
                                    endVerse = null,
                                ),
                            )
                        }
                    }
                }
            } else {
                all { chapterPlan ->
                    isChapterRead(chapterPlan)
                }
            }
        }
    }
}
