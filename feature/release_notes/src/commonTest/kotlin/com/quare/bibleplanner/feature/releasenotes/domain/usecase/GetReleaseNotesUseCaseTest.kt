package com.quare.bibleplanner.feature.releasenotes.domain.usecase

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetReleaseNotesUseCaseTest {
    private lateinit var useCase: GetReleaseNotesUseCase

    @Test
    fun `GIVEN release notes in any order WHEN getting them THEN sorts them from the newest version`() = runTest {
        // Given
        prepareScenario(
            result = Result.success(
                listOf(
                    note("2.9.0"),
                    note("2.10.0"),
                    note("1.0.0"),
                ),
            ),
        )

        // When
        val notes = useCase().getOrThrow()

        // Then
        assertEquals(listOf("2.10.0", "2.9.0", "1.0.0"), notes.map(ReleaseNoteModel::version))
    }

    @Test
    fun `GIVEN the release notes fail to load WHEN getting them THEN returns the failure`() = runTest {
        // Given
        prepareScenario(result = Result.failure(IllegalStateException("boom")))

        // When
        val result = useCase()

        // Then
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    private fun note(version: String): ReleaseNoteModel = ReleaseNoteModel(
        version = version,
        changes = listOf("Change in $version"),
    )

    private fun prepareScenario(result: Result<List<ReleaseNoteModel>>) {
        useCase = GetReleaseNotesUseCase { result }
    }
}
