package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
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
        .getAllBooksWithChaptersDataFlow()
        .map(booksWithChapterMapper::map)

    override suspend fun getBooks(): List<BookDataModel> = bookDao
        .getAllBooksWithChaptersData()
        .let(booksWithChapterMapper::map)

    override suspend fun initializeDatabase() {
        val books = booksLocalDataSource.getBooks()

        val bookEntities = books.map { book ->
            BookEntity(
                id = book.id.name,
                isRead = book.isRead,
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
                        number = verse.number,
                        chapterId = chapterId,
                        isRead = verse.isRead,
                    )
                }
                verseDao.insertVerses(verseEntities)
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

    companion object {
        private const val SHOW_INFORMATION_BOX = "show_information_box"
    }
}
