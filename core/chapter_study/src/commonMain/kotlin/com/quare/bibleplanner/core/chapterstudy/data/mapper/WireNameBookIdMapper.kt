package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.BookId

internal class WireNameBookIdMapper(
    bookIdWireNameMapper: BookIdWireNameMapper,
) {
    private val bookIdsByWireName: Map<String, BookId> = BookId.entries.associateBy(bookIdWireNameMapper::map)

    fun mapOrNull(wireName: String): BookId? = bookIdsByWireName[wireName]
}
