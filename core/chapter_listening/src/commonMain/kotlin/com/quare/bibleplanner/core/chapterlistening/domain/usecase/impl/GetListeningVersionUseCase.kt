package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVersionModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersion
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.first

internal class GetListeningVersionUseCase(
    private val bibleRepository: BibleRepository,
) : GetListeningVersion {
    override suspend fun invoke(): ListeningVersionModel? = bibleRepository
        .getBiblesFlow()
        .first()
        .find { bible -> bible.isSelected }
        ?.version
        ?.let { version ->
            ListeningVersionModel(
                id = version.id,
                languageTag = toLanguageTag(version.language),
            )
        }

    private fun toLanguageTag(language: Language): String = when (language) {
        Language.ENGLISH -> "en-US"
        Language.PORTUGUESE_BRAZIL -> "pt-BR"
        Language.SPANISH -> "es-MX"
    }
}
