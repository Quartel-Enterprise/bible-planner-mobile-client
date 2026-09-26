package com.quare.bibleplanner.feature.read.presentation.factory

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesWithTextsByChapterIdFlowUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import com.quare.bibleplanner.feature.read.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.read.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.read.fake.FakePlanRepository
import com.quare.bibleplanner.feature.read.fake.ThrowingChapterDao
import com.quare.bibleplanner.feature.read.fake.ThrowingVerseDao
import com.quare.bibleplanner.feature.read.fake.passage
import com.quare.bibleplanner.feature.read.fake.singleWeek
import com.quare.bibleplanner.feature.read.presentation.mapper.ChapterVersesUiModelMapper
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class ReadDataPresentationModelFactoryTest {
    private val versionId = "nvi"
    private val selectedBible = BibleModel(
        version = VersionModel(
            id = versionId,
            name = "Nova Versão Internacional",
            version = "1",
            language = Language.PORTUGUESE_BRAZIL,
            chapters = 1189,
            size = 4_000_000L,
        ),
        downloadedChapters = 10,
        downloadStatus = DownloadStatusModel.InProgress.Paused(progress = 0.4f),
        isSelected = true,
        hasPendingUpdate = false,
    )
    private lateinit var factory: ReadDataPresentationModelFactory

    @Test
    fun `GIVEN a downloaded chapter WHEN observing it THEN shows its verses under a header with its neighbours`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val data = observe(chapterNumber = 2).first()

            // Then
            assertEquals(
                expected = BookId.GEN,
                actual = data.header.bookId,
            )
            assertEquals(
                expected = Loadable.Loaded("NVI"),
                actual = data.header.versionAbbreviation,
            )
            assertTrue(data.header.isChapterRead)
            assertEquals(
                expected = ReadNavigationSuggestionsModel(
                    previous = ReadNavigationSuggestionModel(
                        bookId = BookId.GEN,
                        chapterNumber = 1,
                    ),
                    next = ReadNavigationSuggestionModel(
                        bookId = BookId.GEN,
                        chapterNumber = 3,
                    ),
                ),
                actual = data.header.navigationSuggestions,
            )
            val chapter = assertIs<ReadContentUiState.Success>(data.content).chapters.single()
            assertEquals(
                expected = listOf(
                    VerseUiModel(
                        number = 1,
                        heading = null,
                        text = "GEN 2:1",
                        isSelected = false,
                        highlightColor = null,
                        isSaved = true,
                        noteId = null,
                    ),
                    VerseUiModel(
                        number = 2,
                        heading = null,
                        text = "GEN 2:2",
                        isSelected = false,
                        highlightColor = null,
                        isSaved = false,
                        noteId = null,
                    ),
                ),
                actual = chapter.verses,
            )
        }

    @Test
    fun `GIVEN a chapter with an unread verse WHEN observing it THEN the header shows it unread`() = runTest {
        // Given
        prepareScenario()

        // When
        val data = observe(chapterNumber = 1).first()

        // Then
        assertFalse(data.header.isChapterRead)
    }

    @Test
    fun `GIVEN a chapter missing from the index WHEN observing it THEN offers a retry keeping the read state`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val data = observe(
                chapterNumber = 50,
                isInitiallyRead = true,
            ).first()

            // Then
            assertEquals(
                expected = ReadContentUiState.Error.Unknown(errorUiEvent = ReadUiEvent.OnRetryClick),
                actual = data.content,
            )
            assertTrue(data.header.isChapterRead)
        }

    @Test
    fun `GIVEN a version without the chapter text WHEN observing it THEN asks to finish downloading it`() = runTest {
        // Given
        prepareScenario()

        // When
        val data = observe(chapterNumber = 3).first { it.content != ReadContentUiState.Loading }

        // Then
        assertEquals(
            expected = ReadContentUiState.Error.ChapterNotFound(
                errorUiEvent = ReadUiEvent.ManageBibleVersions,
                selectedBibleVersionName = "Nova Versão Internacional",
                downloadStatus = DownloadStatusModel.InProgress.Paused(progress = 0.4f),
                versionSizeInBytes = 4_000_000L,
            ),
            actual = data.content,
        )
    }

    @Test
    fun `GIVEN no selected version and no text WHEN observing THEN asks to download without a version name`() =
        runTest {
            // Given
            prepareScenario(bibles = flowOf(emptyList()))

            // When
            val data = observe(chapterNumber = 3).first { it.content != ReadContentUiState.Loading }

            // Then
            assertEquals(
                expected = ReadContentUiState.Error.ChapterNotFound(
                    errorUiEvent = ReadUiEvent.ManageBibleVersions,
                    selectedBibleVersionName = "",
                    downloadStatus = DownloadStatusModel.NotStarted,
                    versionSizeInBytes = null,
                ),
                actual = data.content,
            )
        }

    @Test
    fun `GIVEN the versions still loading and no text WHEN observing THEN keeps the chapter loading`() = runTest {
        // Given
        prepareScenario(bibles = MutableSharedFlow())

        // When
        val data = observe(chapterNumber = 3).first()

        // Then
        assertEquals(
            expected = ReadContentUiState.Loading,
            actual = data.content,
        )
    }

    @Test
    fun `GIVEN chapters pulled in around the current one WHEN observing THEN lays them out in reading order`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val data = observe(
                chapterNumber = 2,
                prependedChapters = listOf(
                    ReadNavigationSuggestionModel(
                        bookId = BookId.GEN,
                        chapterNumber = 1,
                    ),
                ),
                appendedChapters = listOf(
                    ReadNavigationSuggestionModel(
                        bookId = BookId.GEN,
                        chapterNumber = 50,
                    ),
                ),
            ).first()

            // Then
            val chapters = assertIs<ReadContentUiState.Success>(data.content).chapters
            assertEquals(
                expected = listOf(1, 2),
                actual = chapters.map { it.chapter.chapterNumber },
            )
            assertEquals(
                expected = listOf(false, true),
                actual = chapters.map(ReadChapterUiModel::isRead),
            )
        }

    private fun observe(
        chapterNumber: Int,
        isInitiallyRead: Boolean = false,
        prependedChapters: List<ReadNavigationSuggestionModel> = emptyList(),
        appendedChapters: List<ReadNavigationSuggestionModel> = emptyList(),
    ): Flow<ReadDataUiModel> = factory(
        bookId = BookId.GEN,
        chapterNumber = chapterNumber,
        bookStringResource = Res.string.mark_as_read,
        isInitiallyRead = isInitiallyRead,
        isFromBookDetails = false,
        prependedChapters = prependedChapters,
        appendedChapters = appendedChapters,
    )

    private fun verse(
        chapterId: Long,
        number: Int,
        isRead: Boolean,
        text: String?,
    ): VerseWithTexts {
        val verseId = chapterId * 100 + number
        return VerseWithTexts(
            verse = VerseEntity(
                id = verseId,
                number = number,
                chapterId = chapterId,
                isRead = isRead,
            ),
            texts = listOfNotNull(
                text?.let {
                    VerseTextEntity(
                        verseId = verseId,
                        bibleVersionId = versionId,
                        text = it,
                    )
                },
            ),
        )
    }

    private fun prepareScenario(bibles: Flow<List<BibleModel>> = flowOf(listOf(selectedBible))) {
        val chapterIds = mapOf(1 to 1L, 2 to 2L, 3 to 3L)
        val versesByChapterId = mapOf(
            1L to listOf(
                verse(chapterId = 1L, number = 1, isRead = true, text = "GEN 1:1"),
                verse(chapterId = 1L, number = 2, isRead = false, text = "GEN 1:2"),
            ),
            2L to listOf(
                verse(chapterId = 2L, number = 1, isRead = true, text = "GEN 2:1"),
                verse(chapterId = 2L, number = 2, isRead = true, text = "GEN 2:2"),
                verse(chapterId = 2L, number = 3, isRead = true, text = null),
            ),
            3L to listOf(verse(chapterId = 3L, number = 1, isRead = false, text = null)),
        )
        val bibleRepository = FakeBibleRepository(
            bibles = bibles,
            selectedVersionId = flowOf(versionId),
        )
        val planRepository = FakePlanRepository(
            weeksByPlan = mapOf(
                ReadingPlanType.CHRONOLOGICAL to singleWeek(listOf(passage(BookId.GEN, 1, 2, 3))),
                ReadingPlanType.BOOKS to singleWeek(listOf(passage(BookId.GEN, 1, 2, 3))),
            ),
            selectedPlan = ReadingPlanType.CHRONOLOGICAL,
        )
        val booksRepository = FakeBooksRepository(flowOf(emptyList()))
        factory = ReadDataPresentationModelFactory(
            getSelectedVersionIdFlow = GetSelectedVersionIdFlowUseCase(bibleRepository),
            getChapterId = GetChapterIdUseCase(
                object : ThrowingChapterDao() {
                    override suspend fun getChapterByBookIdAndNumber(
                        bookId: String,
                        chapterNumber: Int,
                    ): ChapterEntity? = chapterIds[chapterNumber]?.let { id ->
                        ChapterEntity(
                            id = id,
                            number = chapterNumber,
                            bookId = bookId,
                        )
                    }
                },
            ),
            getVersesWithTextsByChapterIdFlow = GetVersesWithTextsByChapterIdFlowUseCase(
                object : ThrowingVerseDao() {
                    override fun getVersesWithTextsByChapterIdFlow(chapterId: Long): Flow<List<VerseWithTexts>> =
                        flowOf(versesByChapterId.getValue(chapterId))
                },
            ),
            getSelectedBibleFlow = GetSelectedBibleFlowUseCase(bibleRepository),
            getReadNavigationSuggestionsModelFlow = GetReadNavigationSuggestionsModelUseCase(
                getPlansByWeek = GetPlansByWeekUseCase(
                    planRepository = planRepository,
                    booksRepository = booksRepository,
                    getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                    currentTimestampProvider = { 0L },
                    localDateTimeProvider = {
                        LocalDateTime(
                            year = 2026,
                            month = 1,
                            day = 1,
                            hour = 0,
                            minute = 0,
                        )
                    },
                ),
                planRepository = planRepository,
                booksRepository = booksRepository,
            ),
            observeChapterAnnotations = {
                flowOf(
                    ChapterAnnotations(
                        highlightColorByVerse = emptyMap(),
                        savedVerseNumbers = setOf(1),
                        noteIdByVerse = emptyMap(),
                    ),
                )
            },
            chapterVersesUiModelMapper = ChapterVersesUiModelMapper(),
        )
    }
}
