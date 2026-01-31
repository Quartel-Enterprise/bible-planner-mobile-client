package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.books.presentation.model.BookGroup

data class BookGroupPresentationModel(
    val group: BookGroup,
    val books: List<BookPresentationModel>,
)
