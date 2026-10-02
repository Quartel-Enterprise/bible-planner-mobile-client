package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import kotlinx.coroutines.flow.first

internal class ChapterStudyScopeResolver(
    private val bibleRepository: BibleRepository,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val languageCodeMapper: LanguageCodeMapper,
) {
    suspend fun resolve(target: ChapterStudyTargetModel): ChapterStudyScope = ChapterStudyScope(
        chapter = ChapterRef(
            bibleVersionId = bibleRepository.getSelectedVersionIdFlow().first(),
            bookId = target.bookId,
            chapterNumber = target.chapterNumber,
        ),
        languageCode = languageCodeMapper.map(getAppLanguageFlow().first()),
    )
}
