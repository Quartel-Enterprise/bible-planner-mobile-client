package com.quare.bibleplanner.feature.login.domain.usecase

import com.quare.bibleplanner.core.user.domain.model.UserModel
import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class IsNewAccountUseCaseTest {
    private val createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000)
    private lateinit var useCase: IsNewAccountUseCase

    @Test
    fun `GIVEN a user signed in the instant it was created WHEN invoking THEN reports a new account`() = runTest {
        // Given
        prepareScenario(lastSignInAt = createdAt)

        // When
        val isNewAccount = useCase()

        // Then
        assertTrue(isNewAccount)
    }

    @Test
    fun `GIVEN the two stamps a moment apart WHEN invoking THEN reports a new account`() = runTest {
        // Given
        prepareScenario(lastSignInAt = createdAt + 300.milliseconds)

        // When
        val isNewAccount = useCase()

        // Then
        assertTrue(isNewAccount)
    }

    @Test
    fun `GIVEN a user signing in days after being created WHEN invoking THEN reports an existing account`() = runTest {
        // Given
        prepareScenario(lastSignInAt = createdAt + 3.days)

        // When
        val isNewAccount = useCase()

        // Then
        assertFalse(isNewAccount)
    }

    @Test
    fun `GIVEN a sign-in just past the window WHEN invoking THEN reports an existing account`() = runTest {
        // Given
        prepareScenario(lastSignInAt = createdAt + 10.seconds)

        // When
        val isNewAccount = useCase()

        // Then
        assertFalse(isNewAccount)
    }

    @Test
    fun `GIVEN a user without a creation stamp WHEN invoking THEN reports an existing account`() = runTest {
        // Given
        prepareScenario(createdAt = null, lastSignInAt = createdAt)

        // When
        val isNewAccount = useCase()

        // Then
        assertFalse(isNewAccount)
    }

    @Test
    fun `GIVEN a user without a sign-in stamp WHEN invoking THEN reports an existing account`() = runTest {
        // Given
        prepareScenario(lastSignInAt = null)

        // When
        val isNewAccount = useCase()

        // Then
        assertFalse(isNewAccount)
    }

    @Test
    fun `GIVEN no authenticated user WHEN invoking THEN reports an existing account`() = runTest {
        // Given
        prepareScenario(user = null)

        // When
        val isNewAccount = useCase()

        // Then
        assertFalse(isNewAccount)
    }

    private fun prepareScenario(
        createdAt: Instant? = this.createdAt,
        lastSignInAt: Instant? = this.createdAt,
        user: UserModel? = UserModel(
            id = "user-1",
            name = "Ana",
            email = "ana@example.com",
            photo = null,
            provider = "google",
            lastSignInAt = lastSignInAt,
            createdAt = createdAt,
        ),
    ) {
        useCase = IsNewAccountUseCase(observeCurrentUser = ObserveCurrentUser { flowOf(user) })
    }
}
