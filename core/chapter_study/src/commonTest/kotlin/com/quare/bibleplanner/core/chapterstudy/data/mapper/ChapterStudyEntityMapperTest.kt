package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.data.dto.CrossReferenceDto
import com.quare.bibleplanner.core.chapterstudy.domain.model.CrossReferenceModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.KeyVerseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.OutlineSectionModel
import com.quare.bibleplanner.core.chapterstudy.fake.chapterStudyResponse
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyCrossReferenceEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyNameEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyOutlineSectionEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.relation.ChapterStudyWithContent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ChapterStudyEntityMapperTest {
    private val response = chapterStudyResponse(cacheToken = "token-1")
    private lateinit var mapper: ChapterStudyEntityMapper

    @BeforeTest
    fun setUp() {
        mapper = ChapterStudyEntityMapper(WireNameBookIdMapper(BookIdWireNameMapper()))
    }

    @Test
    fun `GIVEN a study response WHEN mapping to entities THEN stores the study with positioned children`() {
        // When
        val content = mapper.mapToEntities(
            cacheKey = CACHE_KEY,
            response = response,
        )

        // Then
        assertEquals(
            expected = ChapterStudyWithContent(
                study = ChapterStudyEntity(
                    cacheKey = CACHE_KEY,
                    summary = "The serpent leads the woman to doubt the word of God.",
                    context = "Chapter 3 explains where sin and death come from.",
                    keyVerseStart = 15,
                    keyVerseEnd = 15,
                    keyVerseNote = "The first promise of the Redeemer",
                    model = "model-x",
                    promptVersion = 2,
                    updatedAt = "2026-10-01T10:00:00Z",
                    cacheToken = "token-1",
                ),
                outlineSections = listOf(
                    ChapterStudyOutlineSectionEntity(
                        cacheKey = CACHE_KEY,
                        position = 0,
                        startVerse = 1,
                        endVerse = 7,
                        title = "The temptation and the fall",
                    ),
                    ChapterStudyOutlineSectionEntity(
                        cacheKey = CACHE_KEY,
                        position = 1,
                        startVerse = 8,
                        endVerse = 24,
                        title = "The judgement of God",
                    ),
                ),
                names = listOf(
                    ChapterStudyNameEntity(
                        cacheKey = CACHE_KEY,
                        position = 0,
                        name = "The serpent",
                    ),
                    ChapterStudyNameEntity(
                        cacheKey = CACHE_KEY,
                        position = 1,
                        name = "Eve",
                    ),
                    ChapterStudyNameEntity(
                        cacheKey = CACHE_KEY,
                        position = 2,
                        name = "Garden of Eden",
                    ),
                ),
                crossReferences = listOf(
                    ChapterStudyCrossReferenceEntity(
                        cacheKey = CACHE_KEY,
                        position = 0,
                        bookId = "ROM",
                        chapterNumber = 5,
                        startVerse = 12,
                        endVerse = 19,
                    ),
                ),
                questions = listOf(
                    ChapterStudyQuestionEntity(
                        cacheKey = CACHE_KEY,
                        position = 0,
                        text = "Where do you tend to doubt God's goodness?",
                    ),
                ),
            ),
            actual = content,
        )
    }

    @Test
    fun `GIVEN a study response WHEN mapping to entities and back THEN returns the same study`() {
        // Given
        val content = mapper.mapToEntities(
            cacheKey = CACHE_KEY,
            response = response,
        )

        // When
        val study = mapper.mapToDomain(content)

        // Then
        assertEquals(
            expected = createChapterStudy(),
            actual = study,
        )
    }

    @Test
    fun `GIVEN a cross reference to an unknown wire book WHEN mapping to entities THEN drops it`() {
        // Given
        val responseWithUnknownBook = response.copy(
            content = response.content.copy(
                crossReferences = listOf(
                    CrossReferenceDto(
                        book = "TOBIT",
                        chapter = 1,
                        startVerse = 1,
                        endVerse = 2,
                    ),
                    CrossReferenceDto(
                        book = "FIRST_CORINTHIANS",
                        chapter = 15,
                        startVerse = 21,
                        endVerse = 22,
                    ),
                    CrossReferenceDto(
                        book = "REVELATION",
                        chapter = 12,
                        startVerse = 9,
                        endVerse = 9,
                    ),
                ),
            ),
        )

        // When
        val content = mapper.mapToEntities(
            cacheKey = CACHE_KEY,
            response = responseWithUnknownBook,
        )

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyCrossReferenceEntity(
                    cacheKey = CACHE_KEY,
                    position = 0,
                    bookId = "FIRST_CO",
                    chapterNumber = 15,
                    startVerse = 21,
                    endVerse = 22,
                ),
                ChapterStudyCrossReferenceEntity(
                    cacheKey = CACHE_KEY,
                    position = 1,
                    bookId = "REV",
                    chapterNumber = 12,
                    startVerse = 9,
                    endVerse = 9,
                ),
            ),
            actual = content.crossReferences,
        )
    }

    @Test
    fun `GIVEN a response without a key verse WHEN mapping to entities THEN leaves the key verse columns empty`() {
        // Given
        val responseWithoutKeyVerse = response.copy(content = response.content.copy(keyVerse = null))

        // When
        val content = mapper.mapToEntities(
            cacheKey = CACHE_KEY,
            response = responseWithoutKeyVerse,
        )

        // Then
        assertNull(content.study.keyVerseStart)
        assertNull(content.study.keyVerseEnd)
        assertNull(content.study.keyVerseNote)
    }

    @Test
    fun `GIVEN a stored study without a key verse WHEN mapping to domain THEN the study has no key verse`() {
        // Given
        val content = storedContent(
            keyVerseStart = null,
            keyVerseEnd = null,
            keyVerseNote = null,
        )

        // When
        val study = mapper.mapToDomain(content)

        // Then
        assertNull(study.keyVerse)
    }

    @Test
    fun `GIVEN a stored key verse with only its start WHEN mapping to domain THEN it is a single verse without note`() {
        // Given
        val content = storedContent(
            keyVerseStart = 15,
            keyVerseEnd = null,
            keyVerseNote = null,
        )

        // When
        val study = mapper.mapToDomain(content)

        // Then
        assertEquals(
            expected = KeyVerseModel(
                startVerse = 15,
                endVerse = 15,
                note = "",
            ),
            actual = study.keyVerse,
        )
    }

    @Test
    fun `GIVEN stored children out of order WHEN mapping to domain THEN orders each section by position`() {
        // Given
        val content = storedContent(
            keyVerseStart = 15,
            keyVerseEnd = 16,
            keyVerseNote = "Note",
        )

        // When
        val study = mapper.mapToDomain(content)

        // Then
        assertEquals(
            expected = listOf(
                OutlineSectionModel(
                    startVerse = 1,
                    endVerse = 7,
                    title = "First section",
                ),
                OutlineSectionModel(
                    startVerse = 8,
                    endVerse = 24,
                    title = "Second section",
                ),
            ),
            actual = study.outline,
        )
        assertEquals(
            expected = listOf("First name", "Second name"),
            actual = study.peopleAndPlaces,
        )
        assertEquals(
            expected = listOf(
                CrossReferenceModel(
                    bookId = BookId.ROM,
                    chapterNumber = 5,
                    startVerse = 12,
                    endVerse = 19,
                ),
                CrossReferenceModel(
                    bookId = BookId.REV,
                    chapterNumber = 12,
                    startVerse = 9,
                    endVerse = 9,
                ),
            ),
            actual = study.crossReferences,
        )
        assertEquals(
            expected = listOf("First question", "Second question"),
            actual = study.reflectionQuestions,
        )
    }

    @Test
    fun `GIVEN a stored cross reference to an unknown book WHEN mapping to domain THEN drops it`() {
        // Given
        val content = storedContent(
            keyVerseStart = null,
            keyVerseEnd = null,
            keyVerseNote = null,
        ).copy(
            crossReferences = listOf(
                ChapterStudyCrossReferenceEntity(
                    cacheKey = CACHE_KEY,
                    position = 0,
                    bookId = "TOBIT",
                    chapterNumber = 1,
                    startVerse = 1,
                    endVerse = 2,
                ),
            ),
        )

        // When
        val study = mapper.mapToDomain(content)

        // Then
        assertEquals(
            expected = emptyList(),
            actual = study.crossReferences,
        )
    }

    private fun storedContent(
        keyVerseStart: Int?,
        keyVerseEnd: Int?,
        keyVerseNote: String?,
    ): ChapterStudyWithContent = ChapterStudyWithContent(
        study = ChapterStudyEntity(
            cacheKey = CACHE_KEY,
            summary = "Summary",
            context = "Context",
            keyVerseStart = keyVerseStart,
            keyVerseEnd = keyVerseEnd,
            keyVerseNote = keyVerseNote,
            model = "model-x",
            promptVersion = 2,
            updatedAt = "2026-10-01T10:00:00Z",
            cacheToken = "token-1",
        ),
        outlineSections = listOf(
            ChapterStudyOutlineSectionEntity(
                cacheKey = CACHE_KEY,
                position = 1,
                startVerse = 8,
                endVerse = 24,
                title = "Second section",
            ),
            ChapterStudyOutlineSectionEntity(
                cacheKey = CACHE_KEY,
                position = 0,
                startVerse = 1,
                endVerse = 7,
                title = "First section",
            ),
        ),
        names = listOf(
            ChapterStudyNameEntity(
                cacheKey = CACHE_KEY,
                position = 1,
                name = "Second name",
            ),
            ChapterStudyNameEntity(
                cacheKey = CACHE_KEY,
                position = 0,
                name = "First name",
            ),
        ),
        crossReferences = listOf(
            ChapterStudyCrossReferenceEntity(
                cacheKey = CACHE_KEY,
                position = 1,
                bookId = "REV",
                chapterNumber = 12,
                startVerse = 9,
                endVerse = 9,
            ),
            ChapterStudyCrossReferenceEntity(
                cacheKey = CACHE_KEY,
                position = 0,
                bookId = "ROM",
                chapterNumber = 5,
                startVerse = 12,
                endVerse = 19,
            ),
        ),
        questions = listOf(
            ChapterStudyQuestionEntity(
                cacheKey = CACHE_KEY,
                position = 1,
                text = "Second question",
            ),
            ChapterStudyQuestionEntity(
                cacheKey = CACHE_KEY,
                position = 0,
                text = "First question",
            ),
        ),
    )

    private companion object {
        const val CACHE_KEY = "GEN|3|ACF|en"
    }
}
