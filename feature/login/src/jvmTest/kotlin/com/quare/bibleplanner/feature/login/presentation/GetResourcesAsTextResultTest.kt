package com.quare.bibleplanner.feature.login.presentation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetResourcesAsTextResultTest {
    private lateinit var getResourcesAsTextResult: GetResourcesAsTextResult

    @BeforeTest
    fun setUp() {
        getResourcesAsTextResult = GetResourcesAsTextResult()
    }

    @Test
    fun `GIVEN a bundled resource WHEN reading it THEN returns its text`() {
        // When
        val result = getResourcesAsTextResult("com/quare/bibleplanner/feature/login/auth/desktop_auth_success.html")

        // Then
        assertTrue(result.getOrThrow().contains("{{HEADING}}"))
    }

    @Test
    fun `GIVEN a missing resource WHEN reading it THEN fails naming the path`() {
        // When
        val result = getResourcesAsTextResult("missing/resource.txt")

        // Then
        assertEquals("Resource not found on classpath: missing/resource.txt", result.exceptionOrNull()?.message)
    }
}
