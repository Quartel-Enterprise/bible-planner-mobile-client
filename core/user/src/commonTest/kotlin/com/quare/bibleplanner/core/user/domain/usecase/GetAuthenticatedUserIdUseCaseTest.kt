package com.quare.bibleplanner.core.user.domain.usecase

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetAuthenticatedUserIdUseCaseTest {
    @Test
    fun `GIVEN a signed in user WHEN reading the id THEN returns the current id`() = runTest {
        // Given
        val useCase = GetAuthenticatedUserIdUseCase { flowOf("user-1", "user-2") }

        // When
        val userId = useCase()

        // Then
        assertEquals(
            expected = "user-1",
            actual = userId,
        )
    }

    @Test
    fun `GIVEN nobody signed in WHEN reading the id THEN returns null`() = runTest {
        // Given
        val useCase = GetAuthenticatedUserIdUseCase { flowOf(null) }

        // When
        val userId = useCase()

        // Then
        assertNull(userId)
    }
}
