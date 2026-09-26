package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.DayStudyChapterSummaryEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyFactEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyTakeawayEntity
import com.quare.bibleplanner.core.provider.room.relation.DayStudyWithContent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DayStudyDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: DayStudyDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.dayStudyDao()
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
        dao.replace(
            content(
                cacheKey = OTHER_CACHE_KEY,
                label = "other",
            ),
        )

        // When
        dao.deleteByCacheKey(CACHE_KEY)

        // Then
        assertNull(dao.getByCacheKey(CACHE_KEY))
        assertEquals(
            expected = "other",
            actual = dao.getByCacheKey(OTHER_CACHE_KEY)?.study?.overview,
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

        // When
        dao.deleteAll()

        // Then
        assertNull(dao.getByCacheKey(CACHE_KEY))
    }

    private fun DayStudyWithContent.withoutIds(): DayStudyWithContent = copy(
        chapterSummaries = chapterSummaries.map { it.copy(id = 0) },
        takeaways = takeaways.map { it.copy(id = 0) },
        facts = facts.map { it.copy(id = 0) },
        questions = questions.map { it.copy(id = 0) },
    )

    private fun content(
        cacheKey: String,
        label: String,
    ): DayStudyWithContent = DayStudyWithContent(
        study = DayStudyEntity(
            cacheKey = cacheKey,
            passageLabel = "Genesis 1",
            overview = label,
            contextBody = "Context $label",
            model = "model",
            promptVersion = 1,
            updatedAt = "2026-07-11T10:00:00Z",
            cacheToken = "token",
        ),
        chapterSummaries = listOf(
            DayStudyChapterSummaryEntity(
                cacheKey = cacheKey,
                position = 0,
                title = "Summary $label",
                body = "Body",
            ),
        ),
        takeaways = listOf(
            DayStudyTakeawayEntity(
                cacheKey = cacheKey,
                position = 0,
                text = "Takeaway $label",
            ),
        ),
        facts = listOf(
            DayStudyFactEntity(
                cacheKey = cacheKey,
                position = 0,
                label = "Author",
                value = "Moses $label",
            ),
        ),
        questions = listOf(
            DayStudyQuestionEntity(
                cacheKey = cacheKey,
                position = 0,
                question = "Who? $label",
                answer = "God",
            ),
        ),
    )

    private companion object {
        const val CACHE_KEY = "books-1-1"
        const val OTHER_CACHE_KEY = "books-1-2"
    }
}
