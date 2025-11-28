package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.dto.ChapterDataDto
import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.VerseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class BooksLocalDataSource(
    private val resourceReader: ResourceReader,
    private val fileNameToBookIdMapper: FileNameToBookIdMapper,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getBooks(): List<BookDataModel> = withContext(Dispatchers.IO) {
        BibleBooks.fileNames
            .map { fileName ->
                async {
                    readBookFromFile(fileName)
                }
            }
            .awaitAll()
            .filterNotNull()
            .sortedBy { it.id.ordinal }
    }

    private suspend fun readBookFromFile(fileName: String): BookDataModel? {
        val bookIdString = fileName.removeSuffix(".json")
        val bookId = fileNameToBookIdMapper.map(bookIdString) ?: return null

        val filePath = "$BOOKS_DIRECTORY/$fileName"
        val jsonContent = resourceReader.readResource(filePath)
        val chapters = json.decodeFromString<List<ChapterDataDto>>(jsonContent)

        return BookDataModel(
            id = bookId,
            chapters = chapters.map { chapterDto ->
                BookChapterModel(
                    number = chapterDto.number,
                    verses = (1..chapterDto.verses).map { verseNumber ->
                        VerseModel(
                            number = verseNumber,
                            isRead = false,
                        )
                    },
                    isRead = false,
                )
            },
            isRead = false,
        )
    }

    companion object {
        private const val BOOKS_DIRECTORY = "assets/books_by_chapter"
    }
}
