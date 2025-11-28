package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.model.book.BookId

class FileNameToBookIdMapper(
    private val bookMapsProvider: BookMapsProvider,
) {
    fun map(fileName: String): BookId? = bookMapsProvider.bookMaps.firstNotNullOfOrNull { it[fileName] }
}
