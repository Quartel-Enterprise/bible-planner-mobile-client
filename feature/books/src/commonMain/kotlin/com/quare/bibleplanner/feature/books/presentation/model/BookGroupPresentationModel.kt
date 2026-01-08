package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup

data class BookGroupPresentationModel(
    val group: BookGroup,
    val books: List<BookPresentationModel>,
)
