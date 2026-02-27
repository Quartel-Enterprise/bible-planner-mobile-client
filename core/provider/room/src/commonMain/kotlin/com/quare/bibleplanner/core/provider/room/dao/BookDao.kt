package com.quare.bibleplanner.core.provider.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooksFlow(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?

    @Transaction
    @Query("SELECT * FROM books")
    suspend fun getAllBooksWithChapters(): List<BookWithChapters>

    @Transaction
    @Query("SELECT * FROM books")
    fun getAllBooksWithChaptersFlow(): Flow<List<BookWithChapters>>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookWithChaptersByIdFlow(bookId: String): Flow<BookWithChapters?>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookWithChaptersById(bookId: String): BookWithChapters?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET isRead = :isRead WHERE id = :bookId")
    suspend fun updateBookReadStatus(
        bookId: String,
        isRead: Boolean,
    )

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :bookId")
    suspend fun updateBookFavoriteStatus(
        bookId: String,
        isFavorite: Boolean,
    )

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: String)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Query("UPDATE books SET isRead = 0")
    suspend fun resetAllBooksProgress()
}
