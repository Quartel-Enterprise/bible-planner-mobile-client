package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.transaction.DatabaseTransactionRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
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
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val runInTransaction: DatabaseTransactionRunner,
) : BooksRepository {
    private val initMutex = Mutex()

    override fun getBooksFlow(): Flow<List<BookDataModel>> = bookDao
        .getAllBooksWithChaptersFlow()
        .map(booksWithChapterMapper::mapList)
        .flowOn(Dispatchers.Default)

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = bookDao
        .getBookWithChaptersByIdFlow(bookId.name)
        .map { entity ->
            entity?.let { booksWithChapterMapper.mapModel(it) }
        }.flowOn(Dispatchers.Default)

    override suspend fun getBooks(): List<BookDataModel> = booksWithChapterMapper
        .mapList(bookDao.getAllBooksWithChapters())

    override suspend fun isDatabaseInitialized(): Boolean = bookDao.hasBooks()

    override suspend fun initializeDatabase() {
        initMutex.withLock {
            if (isDatabaseInitialized()) return

            val books = booksLocalDataSource.getBooks()

            val bookEntities = books.map { book ->
                BookEntity(
                    id = book.id.name,
                    isRead = book.isRead,
                    isFavorite = book.isFavorite,
                    favoriteUpdatedAt = null,
                    isFavoritePendingSync = false,
                )
            }
            val chapters = books.flatMap { book -> book.chapters.map { chapter -> book.id to chapter } }
            val chapterEntities = chapters.map { (bookId, chapter) ->
                ChapterEntity(
                    number = chapter.number,
                    bookId = bookId.name,
                    isRead = chapter.isRead,
                    id = 0,
                    readUpdatedAt = null,
                    isReadPendingSync = false,
                )
            }

            /*
             * Why: seeding in one transaction keeps a Bible download started meanwhile from finding only
             * some books, skipping the rest and being marked done. One insert per table keeps the
             * transaction, which blocks every other write, as short as possible.
             */
            runInTransaction {
                bookDao.insertBooks(bookEntities)
                val chapterIds = chapterDao.insertChapters(chapterEntities)
                val verseEntities = chapters
                    .zip(chapterIds) { (_, chapter), chapterId ->
                        chapter.verses.map { verse ->
                            VerseEntity(
                                id = 0,
                                number = verse.number,
                                chapterId = chapterId,
                                isRead = verse.isRead,
                                readUpdatedAt = null,
                                isReadPendingSync = false,
                            )
                        }
                    }.flatten()
                verseDao.insertVerses(verseEntities)
            }
        }
    }

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        bookDao.updateBookFavoriteStatus(
            bookId = bookId.name,
            isFavorite = isFavorite,
            updatedAt = currentTimestampProvider.getCurrentTimestamp(),
        )
    }

    override fun getBookLayoutFormatFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(BOOK_LAYOUT_FORMAT)]
    }

    override suspend fun setBookLayoutFormat(layoutFormat: String) {
        dataStore.write(
            key = stringPreferencesKey(BOOK_LAYOUT_FORMAT),
            value = layoutFormat,
        )
    }

    override fun getSelectedTestamentFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(SELECTED_TESTAMENT)]
    }

    override suspend fun setSelectedTestament(testament: String) {
        dataStore.write(
            key = stringPreferencesKey(SELECTED_TESTAMENT),
            value = testament,
        )
    }

    companion object {
        private const val BOOK_LAYOUT_FORMAT = "book_layout_format"
        private const val SELECTED_TESTAMENT = "selected_testament"
    }
}
