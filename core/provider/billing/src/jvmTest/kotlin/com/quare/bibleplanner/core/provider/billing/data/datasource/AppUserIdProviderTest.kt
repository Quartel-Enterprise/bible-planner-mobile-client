package com.quare.bibleplanner.core.provider.billing.data.datasource

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppUserIdProviderTest {
    @Test
    fun `GIVEN a signed in user WHEN resolving the app user id THEN uses the account id`() = runTest {
        // Given
        val provider = AppUserIdProvider(
            getAuthenticatedUserId = { "user-1" },
            getAnonymousAppUserId = { ANONYMOUS_ID },
        )

        // When
        val appUserId = provider()

        // Then
        assertEquals(
            expected = "user-1",
            actual = appUserId,
        )
    }

    @Test
    fun `GIVEN nobody signed in WHEN resolving the app user id THEN uses the anonymous id`() = runTest {
        // Given
        val provider = AppUserIdProvider(
            getAuthenticatedUserId = { null },
            getAnonymousAppUserId = { ANONYMOUS_ID },
        )

        // When
        val appUserId = provider()

        // Then
        assertEquals(
            expected = ANONYMOUS_ID,
            actual = appUserId,
        )
    }

    private companion object {
        const val ANONYMOUS_ID = "\$RCAnonymousID:abc"
    }
}
