package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyCrossReferenceEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyNameEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyOutlineSectionEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.relation.ChapterStudyWithContent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ChapterStudyDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ChapterStudyDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.chapterStudyDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN a study WHEN storing it THEN reads it back with all its sections`() = runTest {
        // Given
        val content = content(
            cacheKey = CACHE_KEY,
            label = "first",
        )

        // When
        dao.replace(content)

        // Then
        assertEquals(
            expected = content,
            actual = dao.getByCacheKey(CACHE_KEY)?.withoutIds(),
        )
        assertTrue(dao.exists(CACHE_KEY))
    }

    @Test
    fun `GIVEN no study WHEN reading it THEN finds nothing`() = runTest {
        // When
        val content = dao.getByCacheKey(CACHE_KEY)

        // Then
        assertNull(content)
        assertFalse(dao.exists(CACHE_KEY))
    }

    @Test
    fun `GIVEN a stored study WHEN replacing it THEN keeps only the new sections`() = runTest {
        // Given
        dao.replace(
            content(
                cacheKey = CACHE_KEY,
                label = "first",
            ),
        )
        val replacement = content(
            cacheKey = CACHE_KEY,
            label = "second",
        )

        // When
        dao.replace(replacement)

        // Then
        assertEquals(
            expected = replacement,
            actual = dao.getByCacheKey(CACHE_KEY)?.withoutIds(),
        )
    }

    @Test
    fun `GIVEN two studies WHEN deleting one THEN keeps the other`() = runTest {
        // Given
        dao.replace(
            content(
                cacheKey = CACHE_KEY,
                label = "first",
            ),
        )
        val other = content(
            cacheKey = OTHER_CACHE_KEY,
            label = "other",
        )
        dao.replace(other)

        // When
        dao.deleteByCacheKey(CACHE_KEY)

        // Then
        assertNull(dao.getByCacheKey(CACHE_KEY))
        assertEquals(
            expected = other,
            actual = dao.getByCacheKey(OTHER_CACHE_KEY)?.withoutIds(),
        )
    }

    @Test
    fun `GIVEN studies WHEN deleting all THEN none is left`() = runTest {
        // Given
        dao.replace(
            content(
                cacheKey = CACHE_KEY,
                label = "first",
            ),
        )
        dao.replace(
            content(
                cacheKey = OTHER_CACHE_KEY,
                label = "other",
            ),
        )

        // When
        dao.deleteAll()

        // Then
        assertNull(dao.getByCacheKey(CACHE_KEY))
        assertNull(dao.getByCacheKey(OTHER_CACHE_KEY))
    }

    private fun ChapterStudyWithContent.withoutIds(): ChapterStudyWithContent = copy(
        outlineSections = outlineSections.map { it.copy(id = 0) },
        names = names.map { it.copy(id = 0) },
        crossReferences = crossReferences.map { it.copy(id = 0) },
        questions = questions.map { it.copy(id = 0) },
    )

    private fun content(
        cacheKey: String,
        label: String,
    ): ChapterStudyWithContent = ChapterStudyWithContent(
        study = ChapterStudyEntity(
            cacheKey = cacheKey,
            summary = "Summary $label",
            context = "Context $label",
            keyVerseStart = 15,
            keyVerseEnd = 16,
            keyVerseNote = "Note $label",
            model = "model",
            promptVersion = 1,
            updatedAt = "2026-10-01T10:00:00Z",
            cacheToken = "token",
        ),
        outlineSections = listOf(
            ChapterStudyOutlineSectionEntity(
                cacheKey = cacheKey,
                position = 0,
                startVerse = 1,
                endVerse = 7,
                title = "Outline $label",
                id = 0,
            ),
            ChapterStudyOutlineSectionEntity(
                cacheKey = cacheKey,
                position = 1,
                startVerse = 8,
                endVerse = 24,
                title = "Second outline $label",
                id = 0,
            ),
        ),
        names = listOf(
            ChapterStudyNameEntity(
                cacheKey = cacheKey,
                position = 0,
                name = "Name $label",
                id = 0,
            ),
        ),
        crossReferences = listOf(
            ChapterStudyCrossReferenceEntity(
                cacheKey = cacheKey,
                position = 0,
                bookId = "ROM",
                chapterNumber = 5,
                startVerse = 12,
                endVerse = 19,
                id = 0,
            ),
        ),
        questions = listOf(
            ChapterStudyQuestionEntity(
                cacheKey = cacheKey,
                position = 0,
                text = "Question $label",
                id = 0,
            ),
        ),
    )

    private companion object {
        const val CACHE_KEY = "GEN|3|ACF|en"
        const val OTHER_CACHE_KEY = "GEN|4|ACF|en"
    }
}
