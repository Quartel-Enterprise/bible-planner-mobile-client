package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BooksRepositoryImpl(
    private val booksLocalDataSource: BooksLocalDataSource,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val booksWithChapterMapper: BooksWithChapterMapper,
    private val dataStore: DataStore<Preferences>,
) : BooksRepository {
    private val initMutex = Mutex()

    override fun getBooksFlow(): Flow<List<BookDataModel>> = bookDao
        .getAllBooksWithChaptersFlow()
        .map(booksWithChapterMapper::mapList)

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = bookDao
        .getBookWithChaptersByIdFlow(bookId.name)
        .map { entity ->
            entity?.let(booksWithChapterMapper::mapModel)
        }

    override suspend fun getBooks(): List<BookDataModel> = bookDao
        .getAllBooksWithChapters()
        .let(booksWithChapterMapper::mapList)

    override suspend fun initializeDatabase(onProgress: (current: Int, total: Int) -> Unit) {
        initMutex.withLock {
            // Double-check inside lock to prevent duplicate initialization
            if (bookDao.getAllBooksWithChapters().isNotEmpty()) return

            val books = booksLocalDataSource.getBooks()
            val total = books.size

            val bookEntities = books.map { book ->
                BookEntity(
                    id = book.id.name,
                    isRead = book.isRead,
                    isFavorite = book.isFavorite,
                )
            }
            bookDao.insertBooks(bookEntities)

            books.forEachIndexed { index, book ->
                val chapterEntities = book.chapters.map { chapter ->
                    ChapterEntity(
                        number = chapter.number,
                        bookId = book.id.name,
                        isRead = chapter.isRead,
                    )
                }
                val chapterIds = chapterDao.insertChapters(chapterEntities)

                book.chapters.forEachIndexed { chapterIndex, chapter ->
                    val chapterId = chapterIds[chapterIndex]
                    val verseEntities = chapter.verses.map { verse ->
                        VerseEntity(
                            id = 0,
                            number = verse.number,
                            chapterId = chapterId,
                            isRead = verse.isRead,
                        )
                    }
                    verseDao.upsertVerses(verseEntities)
                }

                onProgress(index + 1, total)
            }
        }
    }

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        bookDao.updateBookFavoriteStatus(bookId.name, isFavorite)
    }

    override fun getBookLayoutFormatFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(BOOK_LAYOUT_FORMAT)]
    }

    override suspend fun setBookLayoutFormat(layoutFormat: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(BOOK_LAYOUT_FORMAT)] = layoutFormat
        }
    }

    override fun getSelectedTestamentFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(SELECTED_TESTAMENT)]
    }

    override suspend fun setSelectedTestament(testament: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(SELECTED_TESTAMENT)] = testament
        }
    }

    companion object {
        private const val BOOK_LAYOUT_FORMAT = "book_layout_format"
        private const val SELECTED_TESTAMENT = "selected_testament"
    }
}
