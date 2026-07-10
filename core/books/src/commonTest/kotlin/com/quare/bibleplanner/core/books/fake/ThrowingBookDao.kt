package com.quare.bibleplanner.core.books.fake

import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import kotlinx.coroutines.flow.Flow

internal open class ThrowingBookDao : BookDao {
    override fun getAllBooksFlow(): Flow<List<BookEntity>> = error("Unexpected call")

    override suspend fun getBookById(bookId: String): BookEntity? = error("Unexpected call")

    override suspend fun getAllBooksWithChapters(): List<BookWithChapters> = error("Unexpected call")

    override fun getAllBooksWithChaptersFlow(): Flow<List<BookWithChapters>> = error("Unexpected call")

    override fun getBookWithChaptersByIdFlow(bookId: String): Flow<BookWithChapters?> = error("Unexpected call")

    override suspend fun getBookWithChaptersById(bookId: String): BookWithChapters? = error("Unexpected call")

    override suspend fun insertBook(book: BookEntity): Unit = error("Unexpected call")

    override suspend fun insertBooks(books: List<BookEntity>): Unit = error("Unexpected call")

    override suspend fun updateBook(book: BookEntity): Unit = error("Unexpected call")

    override suspend fun updateBookReadStatus(
        bookId: String,
        isRead: Boolean,
    ): Unit = error("Unexpected call")

    override suspend fun updateBookFavoriteStatus(
        bookId: String,
        isFavorite: Boolean,
        updatedAt: Long,
    ): Unit = error("Unexpected call")

    override fun getPendingFavoriteSyncBooksFlow(): Flow<List<BookEntity>> = error("Unexpected call")

    override suspend fun getPendingFavoriteSyncBooks(): List<BookEntity> = error("Unexpected call")

    override suspend fun markFavoriteSynced(
        bookId: String,
        syncedUpdatedAt: Long,
    ): Unit = error("Unexpected call")

    override suspend fun applyRemoteFavorite(
        bookId: String,
        isFavorite: Boolean,
        remoteUpdatedAt: Long,
    ): Unit = error("Unexpected call")

    override suspend fun markLegacyFavoritesPending(now: Long): Unit = error("Unexpected call")

    override suspend fun deleteBook(bookId: String): Unit = error("Unexpected call")

    override suspend fun deleteAllBooks(): Unit = error("Unexpected call")

    override suspend fun resetAllBooksProgress(): Unit = error("Unexpected call")

    override suspend fun resetAllFavorites(): Unit = error("Unexpected call")
}
