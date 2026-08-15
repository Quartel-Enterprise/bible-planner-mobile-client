package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource

fun interface ObserveReadData {
    operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
        isVerticalReadingEnabled: Boolean,
    ): Flow<ReadDataUiModel>
}
