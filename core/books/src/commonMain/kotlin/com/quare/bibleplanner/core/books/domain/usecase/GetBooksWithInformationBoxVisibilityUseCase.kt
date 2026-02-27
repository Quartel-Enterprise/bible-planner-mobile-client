package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBooksWithInformationBoxVisibilityUseCase(
    private val booksRepository: BooksRepository,
) {
    data class Result(
        val books: List<BookDataModel>,
        val layoutFormat: String?,
        val selectedTestament: String?,
    )

    operator fun invoke(): Flow<Result> = booksRepository.run {
        combine(
            getBooksFlow(),
            getBookLayoutFormatFlow(),
            getSelectedTestamentFlow(),
        ) { books, layoutFormat, selectedTestament ->
            Result(
                books = books,
                layoutFormat = layoutFormat,
                selectedTestament = selectedTestament,
            )
        }
    }
}
