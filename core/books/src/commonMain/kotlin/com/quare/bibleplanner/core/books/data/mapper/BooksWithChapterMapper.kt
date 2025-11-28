package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.provider.room.entity.BookWithChaptersData

class BooksWithChapterMapper {
    fun map(bookWithChapterData: List<BookWithChaptersData>): List<BookDataModel> =
        bookWithChapterData.map { it.toModel() }

    private fun BookWithChaptersData.toModel(): BookDataModel {
        val chaptersModel = chapters.map {
            val versesModel = it.verses.map { verseModel ->
                VerseModel(
                    number = verseModel.number,
                    isRead = verseModel.isRead
                )
            }
            BookChapterModel(
                number = it.chapter.number,
                verses = versesModel,
                isRead = it.chapter.isRead,
            )
        }
        return BookDataModel(
            id = BookId.valueOf(book.id),
            chapters = chaptersModel,
            isRead = book.isRead
        )
    }
}
