package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncBillingUserIdUseCaseTest {
    private val billingUserAccount = FakeBillingUserAccount()

    @Test
    fun `GIVEN an authenticated user WHEN syncing THEN logs in with the user id`() = runTest {
        // Given
        val useCase = createUseCase(userId = "user-1")

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertEquals(listOf("user-1"), billingUserAccount.loggedInUserIds)
        assertEquals(0, billingUserAccount.logOutCount)
    }

    @Test
    fun `GIVEN no authenticated user WHEN syncing THEN logs out`() = runTest {
        // Given
        val useCase = createUseCase(userId = null)

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertEquals(emptyList(), billingUserAccount.loggedInUserIds)
        assertEquals(1, billingUserAccount.logOutCount)
    }

    private fun createUseCase(userId: String?): SyncBillingUserIdUseCase = SyncBillingUserIdUseCase(
        observeAuthenticatedUserId = ObserveAuthenticatedUserId { MutableStateFlow(userId) },
        billingUserAccount = billingUserAccount,
    )

    private class FakeBillingUserAccount : BillingUserAccount {
        val loggedInUserIds = mutableListOf<String>()
        var logOutCount = 0

        override suspend fun logIn(userId: String) {
            loggedInUserIds += userId
        }

        override suspend fun logOut() {
            logOutCount++
        }
    }
}
