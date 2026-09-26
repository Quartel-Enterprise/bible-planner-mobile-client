package com.quare.bibleplanner.feature.day.fake

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.AreAllPassagesReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsChapterReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsPassageReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateBookReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdatePassageReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateSpecificRangeChapterReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateWholeBookReadStatusIfNeededUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateWholeChapterReadStatusUseCase
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.db.DatabaseConstructor
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

internal class InMemoryBibleDatabase(
    queryContext: CoroutineContext,
) {
    val database: AppDatabase = Room
        .inMemoryDatabaseBuilder<AppDatabase>(DatabaseConstructor::initialize)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryContext)
        .build()

    val booksRepository: BooksRepository = DaoBooksRepository(database.bookDao())

    suspend fun insertBook(
        bookId: BookId,
        versesPerChapter: List<Int>,
    ) {
        database.bookDao().insertBook(
            BookEntity(
                id = bookId.name,
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
            ),
        )
        versesPerChapter.forEachIndexed { index, verseCount ->
            val chapterId = database.chapterDao().insertChapter(
                ChapterEntity(
                    number = index + 1,
                    bookId = bookId.name,
                ),
            )
            database.verseDao().upsertVerses(
                (1..verseCount).map { verseNumber ->
                    VerseEntity(
                        id = 0,
                        number = verseNumber,
                        chapterId = chapterId,
                    )
                },
            )
        }
    }

    fun updatePassageReadStatus(
        currentTimestampProvider: CurrentTimestampProvider,
        trackEvent: TrackEvent,
    ): UpdatePassageReadStatusUseCase {
        val bookDao = database.bookDao()
        val chapterDao = database.chapterDao()
        val verseDao = database.verseDao()
        val updateWholeBookReadStatusIfNeeded = UpdateWholeBookReadStatusIfNeededUseCase(
            bookDao = bookDao,
            chapterDao = chapterDao,
            trackEvent = trackEvent,
        )
        return UpdatePassageReadStatusUseCase(
            updateBookReadStatus = UpdateBookReadStatusUseCase(
                bookDao = bookDao,
                chapterDao = chapterDao,
                verseDao = verseDao,
                currentTimestampProvider = currentTimestampProvider,
                trackEvent = trackEvent,
            ),
            areAllPassagesRead = AreAllPassagesReadUseCase(
                IsPassageReadUseCase(
                    bookDao = bookDao,
                    chapterDao = chapterDao,
                    isChapterRead = IsChapterReadUseCase(
                        chapterDao = chapterDao,
                        verseDao = verseDao,
                        isWholeChapterRead = IsWholeChapterReadUseCase(
                            chapterDao = chapterDao,
                            verseDao = verseDao,
                        ),
                    ),
                ),
            ),
            updateWholeChapterReadStatus = UpdateWholeChapterReadStatusUseCase(
                chapterDao = chapterDao,
                verseDao = verseDao,
                updateWholeBookReadStatusIfNeeded = updateWholeBookReadStatusIfNeeded,
                currentTimestampProvider = currentTimestampProvider,
            ),
            updateSpecificRangeChapterReadStatus = UpdateSpecificRangeChapterReadStatusUseCase(
                chapterDao = chapterDao,
                verseDao = verseDao,
                updateWholeBookReadStatusIfNeeded = updateWholeBookReadStatusIfNeeded,
                currentTimestampProvider = currentTimestampProvider,
            ),
        )
    }

    fun close() {
        database.close()
    }
}

private class DaoBooksRepository(
    private val bookDao: BookDao,
) : BooksRepository {
    private val mapper = BooksWithChapterMapper()

    override fun getBooksFlow(): Flow<List<BookDataModel>> = bookDao
        .getAllBooksWithChaptersFlow()
        .map(mapper::mapList)

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = error("unused")

    override suspend fun getBooks(): List<BookDataModel> = error("unused")

    override suspend fun initializeDatabase() = error("unused")

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) = error("unused")

    override fun getBookLayoutFormatFlow(): Flow<String?> = error("unused")

    override suspend fun setBookLayoutFormat(layoutFormat: String) = error("unused")

    override fun getSelectedTestamentFlow(): Flow<String?> = error("unused")

    override suspend fun setSelectedTestament(testament: String) = error("unused")
}
