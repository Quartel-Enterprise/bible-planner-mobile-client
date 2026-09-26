package com.quare.bibleplanner.feature.logout.domain.usecase

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

internal class LogoutUseCaseTest {
    private val flushTimeout = 5.seconds
    private lateinit var useCase: LogoutUseCase
    private var pushCalls = 0
    private var endSessionCalls = 0

    @Test
    fun `GIVEN a successful flush WHEN logging out THEN syncs, ends the session and finishes with its result`() =
        runTest {
            // Given
            prepareScenario(
                pushResult = Result.success(Unit),
                endSessionResult = Result.success(Unit),
            )

            // When
            val progress = useCase(shouldFlushPending = true).toList()

            // Then
            assertEquals(
                listOf(
                    LogoutProgress.InProgress(LogoutPhase.SYNCING),
                    LogoutProgress.InProgress(LogoutPhase.ENDING_SESSION),
                    LogoutProgress.Finished(Result.success(Unit)),
                ),
                progress,
            )
            assertEquals(1, pushCalls)
            assertEquals(1, endSessionCalls)
        }

    @Test
    fun `GIVEN a failing flush WHEN logging out THEN aborts with a flush failure without ending the session`() =
        runTest {
            // Given
            prepareScenario(
                pushResult = Result.failure(IllegalStateException("offline")),
                endSessionResult = Result.success(Unit),
            )

            // When
            val progress = useCase(shouldFlushPending = true).toList()

            // Then
            assertEquals(LogoutProgress.InProgress(LogoutPhase.SYNCING), progress.first())
            val finished = assertIs<LogoutProgress.Finished>(progress.last())
            val error = assertIs<LogoutFlushFailedException>(finished.result.exceptionOrNull())
            assertEquals("offline", error.cause?.message)
            assertEquals(2, progress.size)
            assertEquals(0, endSessionCalls)
        }

    @Test
    fun `GIVEN the user signs out anyway WHEN logging out THEN skips the flush and ends the session`() = runTest {
        // Given
        prepareScenario(
            pushResult = Result.success(Unit),
            endSessionResult = Result.success(Unit),
        )

        // When
        val progress = useCase(shouldFlushPending = false).toList()

        // Then
        assertEquals(
            listOf(
                LogoutProgress.InProgress(LogoutPhase.ENDING_SESSION),
                LogoutProgress.Finished(Result.success(Unit)),
            ),
            progress,
        )
        assertEquals(0, pushCalls)
    }

    @Test
    fun `GIVEN a sign-out failure WHEN logging out THEN finishes with that failure`() = runTest {
        // Given
        val failure = Result.failure<Unit>(IllegalStateException("sign out failed"))
        prepareScenario(
            pushResult = Result.success(Unit),
            endSessionResult = failure,
        )

        // When
        val progress = useCase(shouldFlushPending = false).toList()

        // Then
        assertEquals(LogoutProgress.Finished(failure), progress.last())
    }

    private fun prepareScenario(
        pushResult: Result<Unit>,
        endSessionResult: Result<Unit>,
    ) {
        pushCalls = 0
        endSessionCalls = 0
        useCase = LogoutUseCase(
            flushPendingChanges = FlushPendingChangesUseCase(
                pushAllPending = {
                    pushCalls++
                    pushResult.getOrThrow()
                },
                flushTimeout = flushTimeout,
            ),
            endSession = {
                endSessionCalls++
                endSessionResult
            },
        )
    }
}
