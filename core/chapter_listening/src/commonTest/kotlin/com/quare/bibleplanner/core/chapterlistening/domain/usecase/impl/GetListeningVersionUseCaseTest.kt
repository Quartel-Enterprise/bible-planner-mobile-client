package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetListeningVersionUseCaseTest {
    @Test
    fun `GIVEN a selected version not in the listing WHEN getting the version THEN reads the selected one`() = runTest {
        // Given
        val useCase = GetListeningVersionUseCase(
            FakeBibleRepository(
                bibles = emptyList(),
                selectedVersionId = "ACF",
            ),
        )

        // When
        val version = useCase()

        // Then
        assertEquals("ACF", version)
    }
}
