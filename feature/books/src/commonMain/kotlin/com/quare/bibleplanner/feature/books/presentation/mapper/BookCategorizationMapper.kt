package com.quare.bibleplanner.feature.books.presentation.mapper

import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.feature.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel

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
