package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersion
import kotlinx.coroutines.flow.first

internal class GetListeningVersionUseCase(
    private val bibleRepository: BibleRepository,
) : GetListeningVersion {
    override suspend fun invoke(): String = bibleRepository.getSelectedVersionIdFlow().first()
}
