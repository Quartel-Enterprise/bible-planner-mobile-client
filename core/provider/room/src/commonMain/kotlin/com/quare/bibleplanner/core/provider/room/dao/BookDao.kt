package com.quare.bibleplanner.core.provider.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.BookWithChaptersData
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooksFlow(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookByIdSuspend(bookId: String): BookEntity?

    @Transaction
    @Query("SELECT * FROM books")
    suspend fun getAllBooksWithChaptersData(): List<BookWithChaptersData>

    @Transaction
    @Query("SELECT * FROM books")
    fun getAllBooksWithChaptersDataFlow(): Flow<List<BookWithChaptersData>>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookWithChaptersByIdFlow(bookId: String): Flow<BookWithChaptersData?>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookWithChaptersById(bookId: String): BookWithChaptersData?

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

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: String)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()
}
