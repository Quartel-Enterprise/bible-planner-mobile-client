package com.quare.bibleplanner.feature.books.presentation.mapper

import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel

class BookCategorizationMapper(
    private val bookGroupMapper: BookGroupMapper,
) {
    private val canonicalGroupOrder: List<BookGroup> = BookId.entries.map(bookGroupMapper::fromBookId).distinct()

    fun map(books: List<BookPresentationModel>): Map<BookTestament, List<BookGroupPresentationModel>> {
        val groupedBooks = books.groupBy { bookGroupMapper.fromBookId(it.id) }
        return canonicalGroupOrder
            .filter(groupedBooks::containsKey)
            .groupBy { it.testament }
            .mapValues { (_, groups) ->
                groups.map { group ->
                    BookGroupPresentationModel(
                        group = group,
                        books = groupedBooks.getValue(group),
                    )
                }
            }
    }
}
