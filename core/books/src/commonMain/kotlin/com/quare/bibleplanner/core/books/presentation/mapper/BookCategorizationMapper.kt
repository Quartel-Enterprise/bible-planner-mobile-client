package com.quare.bibleplanner.core.books.presentation.mapper

import com.quare.bibleplanner.core.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.core.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.core.books.presentation.model.BookTestament

class BookCategorizationMapper(
    private val bookGroupMapper: BookGroupMapper,
) {
    fun map(books: List<BookPresentationModel>): Map<BookTestament, List<BookGroupPresentationModel>> {
        val groupedBooks = books.groupBy { bookGroupMapper.fromBookId(it.id) }
        return groupedBooks.keys
            .groupBy { it.testament }
            .mapValues { (_, groups) ->
                groups
                    .sortedBy { (it as? Enum<*>)?.ordinal ?: 0 }
                    .map { group ->
                        BookGroupPresentationModel(
                            group = group,
                            books = groupedBooks[group].orEmpty(),
                        )
                    }
            }
    }
}
