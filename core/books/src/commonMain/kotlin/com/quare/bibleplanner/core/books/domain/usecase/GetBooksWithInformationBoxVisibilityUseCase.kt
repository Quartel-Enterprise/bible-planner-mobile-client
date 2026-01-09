package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.IsMoreWebAppEnabled
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBooksWithInformationBoxVisibilityUseCase(
    private val booksRepository: BooksRepository,
    private val isMoreWebAppEnabled: IsMoreWebAppEnabled,
) {
    data class Result(
        val books: List<BookDataModel>,
        val isInformationBoxVisible: Boolean,
        val layoutFormat: String?,
        val selectedTestament: String?,
    )

    operator fun invoke(): Flow<Result> = combine(
        booksRepository.getBooksFlow(),
        booksRepository.getShowInformationBoxFlow(),
        booksRepository.getBookLayoutFormatFlow(),
        booksRepository.getSelectedTestamentFlow(),
    ) { books, showBox, layoutFormat, selectedTestament ->
        Result(
            books = books,
            isInformationBoxVisible = showBox && isMoreWebAppEnabled(),
            layoutFormat = layoutFormat,
            selectedTestament = selectedTestament,
        )
    }
}
