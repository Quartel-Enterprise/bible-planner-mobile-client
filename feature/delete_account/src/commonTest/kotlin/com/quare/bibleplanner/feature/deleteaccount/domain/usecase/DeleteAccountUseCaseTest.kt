package com.quare.bibleplanner.feature.deleteaccount.domain.usecase

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DeleteAccountUseCaseTest {
    private lateinit var useCase: DeleteAccountUseCase
    private var closedSession = false

    @BeforeTest
    fun setUp() {
        closedSession = false
    }

    @Test
    fun `GIVEN a successful deletion WHEN invoking THEN reports both phases and finishes with success`() = runTest {
        // Given
        prepareScenario()

        // When
        val progress = useCase().toList()

        // Then
        assertEquals(
            listOf(DeleteAccountPhase.DELETING_DATA, DeleteAccountPhase.CLOSING_ACCOUNT),
            progress.filterIsInstance<DeleteAccountProgress.InProgress>().map(DeleteAccountProgress.InProgress::phase),
        )
        assertTrue(
            progress
                .filterIsInstance<DeleteAccountProgress.Finished>()
                .single()
                .result.isSuccess,
        )
    }

    @Test
    fun `GIVEN a successful deletion WHEN invoking THEN closes the local session`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase().toList()

        // Then
        assertTrue(closedSession)
    }

    @Test
    fun `GIVEN a failing request WHEN invoking THEN finishes with failure without closing the session`() = runTest {
        // Given
        prepareScenario(deletionError = IllegalStateException("boom"))

        // When
        val progress = useCase().toList()

        // Then
        assertEquals(
            listOf(DeleteAccountPhase.DELETING_DATA),
            progress.filterIsInstance<DeleteAccountProgress.InProgress>().map(DeleteAccountProgress.InProgress::phase),
        )
        assertTrue(
            progress
                .filterIsInstance<DeleteAccountProgress.Finished>()
                .single()
                .result.isFailure,
        )
        assertFalse(closedSession)
    }

    @Test
    fun `GIVEN a failing session close WHEN invoking THEN finishes with failure`() = runTest {
        // Given
        prepareScenario(closeSessionResult = Result.failure(IllegalStateException("boom")))

        // When
        val progress = useCase().toList()

        // Then
        assertTrue(
            progress
                .filterIsInstance<DeleteAccountProgress.Finished>()
                .single()
                .result.isFailure,
        )
    }

    private fun prepareScenario(
        deletionError: Throwable? = null,
        closeSessionResult: Result<Unit> = Result.success(Unit),
    ) {
        useCase = DeleteAccountUseCase(
            repository = { deletionError?.let { throw it } },
            closeDeletedAccountSession = {
                closedSession = true
                closeSessionResult
            },
        )
    }
}
