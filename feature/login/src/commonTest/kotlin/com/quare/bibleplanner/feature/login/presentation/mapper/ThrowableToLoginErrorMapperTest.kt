package com.quare.bibleplanner.feature.login.presentation.mapper

import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ThrowableToLoginErrorMapperTest {
    private lateinit var mapper: ThrowableToLoginErrorMapper

    @BeforeTest
    fun setUp() {
        mapper = ThrowableToLoginErrorMapper()
    }

    @Test
    fun `GIVEN a request timeout WHEN mapping THEN returns a connection error`() {
        // When
        val error = mapper(
            HttpRequestTimeoutException(
                url = "https://auth.example.com",
                timeoutMillis = 1_000,
            ),
        )

        // Then
        assertEquals(LoginError.CONNECTION, error)
    }

    @Test
    fun `GIVEN the provider returned no email WHEN mapping THEN returns an email required error`() {
        // When
        val error = mapper(IllegalStateException("error getting user email from external provider"))

        // Then
        assertEquals(LoginError.EMAIL_REQUIRED, error)
    }

    @Test
    fun `GIVEN any other failure WHEN mapping THEN returns a generic error`() {
        // When
        val error = mapper(IllegalStateException("boom"))

        // Then
        assertEquals(LoginError.GENERIC, error)
    }

    @Test
    fun `GIVEN no throwable WHEN mapping THEN returns a generic error`() {
        // When
        val error = mapper(null)

        // Then
        assertEquals(LoginError.GENERIC, error)
    }
}
