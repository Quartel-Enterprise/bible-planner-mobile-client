package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters

class BooksWithChapterMapper {
    fun map(bookWithChapters: List<BookWithChapters>): List<BookDataModel> = bookWithChapters.map { it.toModel() }

    private fun BookWithChapters.toModel(): BookDataModel {
        val chaptersModel = chapters.map { chapterWithVerses ->
            val versesModel = chapterWithVerses.verses.flatMap { verseWithTexts ->
                if (verseWithTexts.texts.isNotEmpty()) {
                    verseWithTexts.texts.map { textEntity ->
                        VerseModel(
                            number = verseWithTexts.verse.number,
                            isRead = verseWithTexts.verse.isRead,
                            text = textEntity.text,
                        )
                    }
                } else {
                    listOf(
                        VerseModel(
                            number = verseWithTexts.verse.number,
                            isRead = verseWithTexts.verse.isRead,
                            text = null,
                        ),
                    )
                }
            }

            // Derive chapter read status: either the flag is true, or all verses are read
            val isChapterRead = chapterWithVerses.chapter.isRead || (
                versesModel.isNotEmpty() && versesModel.all { it.isRead }
            )

            BookChapterModel(
                number = chapterWithVerses.chapter.number,
                verses = versesModel,
                isRead = isChapterRead,
            )
        }

        // Derive book read status: either the flag is true, or all chapters are read
        val isBookRead = book.isRead || (
            chaptersModel.isNotEmpty() && chaptersModel.all { it.isRead }
        )

        return BookDataModel(
            id = BookId.valueOf(book.id),
            chapters = chaptersModel,
            isRead = isBookRead,
            isFavorite = book.isFavorite,
        )
    }
}
