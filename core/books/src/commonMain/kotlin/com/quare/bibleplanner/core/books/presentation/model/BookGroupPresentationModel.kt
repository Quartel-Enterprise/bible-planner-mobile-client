package com.quare.bibleplanner.core.books.presentation.model

data class BookGroupPresentationModel(
    val group: BookGroup,
    val books: List<BookPresentationModel>,
)
