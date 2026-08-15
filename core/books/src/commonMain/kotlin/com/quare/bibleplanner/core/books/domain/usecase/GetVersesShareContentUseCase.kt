package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.books.util.getVerseReferenceLabel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import kotlinx.coroutines.flow.first

class GetVersesShareContentUseCase(
    private val getChapterId: GetChapterIdUseCase,
    private val verseDao: VerseDao,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlowUseCase,
    private val getSelectedBibleNameFlow: GetSelectedBibleNameFlowUseCase,
) : GetVersesShareContent {
    override suspend fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): VersesShareContentModel? {
        val chapterId = getChapterId(
            bookId = bookId,
            chapterNumber = chapterNumber,
        ) ?: return null
        val versionId = getSelectedVersionIdFlow().first()
        val selectedNumbers = verseNumbers.sorted()
        val texts = verseDao
            .getVersesWithTextsByChapterId(chapterId)
            .filter { it.verse.number in selectedNumbers }
            .sortedBy { it.verse.number }
            .mapNotNull { verseWithTexts ->
                verseWithTexts.texts
                    .find { it.bibleVersionId == versionId }
                    ?.let { verseText -> verseWithTexts.verse.number to verseText.text.trim() }
            }
        if (texts.isEmpty()) return null
        return VersesShareContentModel(
            text = texts.toShareText(),
            reference = getVerseReferenceLabel(
                bookId = bookId,
                chapterNumber = chapterNumber,
                verseNumbers = texts.map { it.first },
            ),
            versionName = getSelectedBibleNameFlow().first(),
        )
    }

    private fun List<Pair<Int, String>>.toShareText(): String = if (size == 1) {
        first().second
    } else {
        joinToString(separator = " ") { (number, text) -> "$number $text" }
    }
}
