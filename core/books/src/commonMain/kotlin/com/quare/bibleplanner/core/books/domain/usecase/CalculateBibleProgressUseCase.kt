package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Calculates the overall Bible reading progress based on all verses in the Bible,
 * regardless of the reading plan type.
 */
class CalculateBibleProgressUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<Float> = booksRepository.getBooksFlow().map { books ->
        if (books.isEmpty()) return@map 0f

        val totalVerses = books.sumOf { book ->
            book.chapters.sumOf { chapter ->
                chapter.verses.size
            }
        }

        if (totalVerses == 0) return@map 0f

        val readVerses = books.sumOf { book ->
            book.chapters.sumOf { chapter ->
                chapter.verses.count { it.isRead }
            }
        }

        100 * (readVerses.toFloat() / totalVerses.toFloat())
    }
}
