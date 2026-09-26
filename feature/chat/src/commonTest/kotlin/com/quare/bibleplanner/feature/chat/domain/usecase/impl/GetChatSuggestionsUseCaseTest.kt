package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.chat.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.chat.fake.FakeDayStudyRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.core.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.core.daystudy.domain.model.QaModel
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.HasCachedStudyUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetChatSuggestionsUseCaseTest {
    private val passages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = listOf(
                ChapterModel(
                    number = 4,
                    startVerse = null,
                    endVerse = null,
                    bookId = BookId.GEN,
                ),
            ),
            isRead = false,
            chapterRanges = "4",
        ),
    )
    private lateinit var useCase: GetChatSuggestionsUseCase
    private lateinit var repository: FakeDayStudyRepository

    @Test
    fun `GIVEN no passages WHEN asking for suggestions THEN offers none without looking the study up`() = runTest {
        // Given
        prepareScenario(isCached = true)

        // When
        val suggestions = useCase(emptyList())

        // Then
        assertTrue(suggestions.isEmpty())
        assertTrue(repository.cacheLookups.isEmpty())
    }

    @Test
    fun `GIVEN a study not generated yet WHEN asking for suggestions THEN offers none`() = runTest {
        // Given
        prepareScenario(isCached = false)

        // When
        val suggestions = useCase(passages)

        // Then
        assertTrue(suggestions.isEmpty())
        assertEquals(
            expected = listOf("ACF" to "pt-BR"),
            actual = repository.cacheLookups,
        )
    }

    @Test
    fun `GIVEN a cached study WHEN asking for suggestions THEN offers its common questions`() = runTest {
        // Given
        prepareScenario(
            isCached = true,
            study = flowOf(
                DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.entries.first()),
                DayStudyGenerationEventModel.Completed(
                    DayStudyModel(
                        passageLabel = "Gênesis 4",
                        overview = "",
                        chapterSummaries = emptyList(),
                        takeaways = emptyList(),
                        context = HistoricalContextModel(
                            body = "",
                            facts = emptyList(),
                        ),
                        commonQuestions = listOf(
                            QaModel(
                                question = "Por que Caim matou Abel?",
                                answer = "Inveja.",
                            ),
                        ),
                    ),
                ),
            ),
        )

        // When
        val suggestions = useCase(passages)

        // Then
        assertEquals(
            expected = listOf("Por que Caim matou Abel?"),
            actual = suggestions,
        )
    }

    @Test
    fun `GIVEN the study fails to load WHEN asking for suggestions THEN offers none`() = runTest {
        // Given
        prepareScenario(
            isCached = true,
            study = flow { throw IllegalStateException("offline") },
        )

        // When
        val suggestions = useCase(passages)

        // Then
        assertTrue(suggestions.isEmpty())
    }

    private fun prepareScenario(
        isCached: Boolean,
        study: Flow<DayStudyGenerationEventModel> = flowOf(),
    ) {
        repository = FakeDayStudyRepository(
            isCached = isCached,
            study = study,
        )
        val bibleRepository = FakeBibleRepository(selectedVersionId = "ACF")
        useCase = GetChatSuggestionsUseCase(
            hasCachedStudy = HasCachedStudyUseCase(
                repository = repository,
                bibleRepository = bibleRepository,
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
            getDayStudy = GetDayStudyUseCase(
                repository = repository,
                bibleRepository = bibleRepository,
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
        )
    }
}
