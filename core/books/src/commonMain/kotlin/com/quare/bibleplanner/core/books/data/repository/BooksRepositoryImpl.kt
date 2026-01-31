package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BooksRepositoryImpl(
    private val booksLocalDataSource: BooksLocalDataSource,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val booksWithChapterMapper: BooksWithChapterMapper,
    private val dataStore: DataStore<Preferences>,
) : BooksRepository {
    override fun getBooksFlow(): Flow<List<BookDataModel>> = bookDao
        .getAllBooksWithChaptersFlow()
        .map(booksWithChapterMapper::map)

    override suspend fun getBooks(): List<BookDataModel> = bookDao
        .getAllBooksWithChapters()
        .let(booksWithChapterMapper::map)

    override suspend fun initializeDatabase() {
        val books = booksLocalDataSource.getBooks()

        val bookEntities = books.map { book ->
            BookEntity(
                id = book.id.name,
                isRead = book.isRead,
                isFavorite = book.isFavorite,
            )
        }
        bookDao.insertBooks(bookEntities)

        books.forEach { book ->
            val chapterEntities = book.chapters.map { chapter ->
                ChapterEntity(
                    number = chapter.number,
                    bookId = book.id.name,
                    isRead = chapter.isRead,
                )
            }
            val chapterIds = chapterDao.insertChapters(chapterEntities)

            book.chapters.forEachIndexed { index, chapter ->
                val chapterId = chapterIds[index]
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
        }
    }

    override fun getShowInformationBoxFlow(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(SHOW_INFORMATION_BOX)] ?: true
    }

    override suspend fun setInformationBoxDismissed() {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(SHOW_INFORMATION_BOX)] = false
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
        private const val SHOW_INFORMATION_BOX = "show_information_box"
        private const val BOOK_LAYOUT_FORMAT = "book_layout_format"
        private const val SELECTED_TESTAMENT = "selected_testament"
    }
}
