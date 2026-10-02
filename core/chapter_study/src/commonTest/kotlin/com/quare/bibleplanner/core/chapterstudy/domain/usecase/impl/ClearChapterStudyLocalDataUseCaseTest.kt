package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ClearChapterStudyLocalDataUseCaseTest {
    private lateinit var repository: FakeChapterStudyRepository
    private lateinit var useCase: ClearChapterStudyLocalDataUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeChapterStudyRepository(
            cachedStudy = createChapterStudy(),
            status = null,
            events = emptyList(),
        )
        useCase = ClearChapterStudyLocalDataUseCase(repository)
    }

    @Test
    fun `WHEN clearing the local data THEN clears the study cache once`() = runTest {
        // When
        useCase()

        // Then
        assertEquals(
            expected = 1,
            actual = repository.cacheClearCount,
        )
    }
}
