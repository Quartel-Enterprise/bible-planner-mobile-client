package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChapterStudyScopeResolverTest {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private lateinit var resolver: ChapterStudyScopeResolver

    @Test
    fun `GIVEN a selected version and portuguese WHEN resolving THEN scopes the chapter to them`() = runTest {
        // Given
        prepareScenario(
            selectedVersionId = "ACF",
            language = Language.PORTUGUESE_BRAZIL,
        )

        // When
        val scope = resolver.resolve(target)

        // Then
        assertEquals(
            expected = ChapterStudyScope(
                chapter = ChapterRef(
                    bibleVersionId = "ACF",
                    bookId = BookId.GEN,
                    chapterNumber = 3,
                ),
                languageCode = "pt-BR",
            ),
            actual = scope,
        )
    }

    @Test
    fun `GIVEN the app in english WHEN resolving THEN the language code is en`() = runTest {
        // Given
        prepareScenario(
            selectedVersionId = "KJV",
            language = Language.ENGLISH,
        )

        // When
        val scope = resolver.resolve(target)

        // Then
        assertEquals(
            expected = ChapterStudyScope(
                chapter = ChapterRef(
                    bibleVersionId = "KJV",
                    bookId = BookId.GEN,
                    chapterNumber = 3,
                ),
                languageCode = "en",
            ),
            actual = scope,
        )
    }

    @Test
    fun `GIVEN the app in spanish WHEN resolving THEN the language code is es`() = runTest {
        // Given
        prepareScenario(
            selectedVersionId = "RVR",
            language = Language.SPANISH,
        )

        // When
        val scope = resolver.resolve(target)

        // Then
        assertEquals(
            expected = ChapterStudyScope(
                chapter = ChapterRef(
                    bibleVersionId = "RVR",
                    bookId = BookId.GEN,
                    chapterNumber = 3,
                ),
                languageCode = "es",
            ),
            actual = scope,
        )
    }

    private fun prepareScenario(
        selectedVersionId: String,
        language: Language,
    ) {
        resolver = ChapterStudyScopeResolver(
            bibleRepository = FakeBibleRepository(
                bibles = emptyList(),
                selectedVersionId = selectedVersionId,
            ),
            getAppLanguageFlow = { flowOf(language) },
            languageCodeMapper = LanguageCodeMapper(),
        )
    }
}
