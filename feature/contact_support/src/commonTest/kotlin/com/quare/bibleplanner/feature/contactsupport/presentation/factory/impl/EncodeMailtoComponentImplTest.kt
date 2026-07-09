package com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl

import kotlin.test.Test
import kotlin.test.assertEquals

internal class EncodeMailtoComponentImplTest {
    private lateinit var encodeMailtoComponent: EncodeMailtoComponentImpl

    @Test
    fun `GIVEN a value with only unreserved ASCII characters WHEN encoding THEN returns it unchanged`() {
        // Given
        prepareScenario()

        // When
        val result = encodeMailtoComponent("Hello-World_2026.test~ok")

        // Then
        assertEquals("Hello-World_2026.test~ok", result)
    }

    @Test
    fun `GIVEN a value with spaces and reserved punctuation WHEN encoding THEN percent-encodes each reserved byte`() {
        // Given
        prepareScenario()

        // When
        val result = encodeMailtoComponent("a b&c=d")

        // Then
        assertEquals("a%20b%26c%3Dd", result)
    }

    @Test
    fun `GIVEN a value with a line break WHEN encoding THEN percent-encodes the newline`() {
        // Given
        prepareScenario()

        // When
        val result = encodeMailtoComponent("line1\nline2")

        // Then
        assertEquals("line1%0Aline2", result)
    }

    @Test
    fun `GIVEN a value with a non-ASCII character WHEN encoding THEN percent-encodes its UTF-8 bytes`() {
        // Given
        prepareScenario()

        // When
        val result = encodeMailtoComponent("café")

        // Then
        assertEquals("caf%C3%A9", result)
    }

    @Test
    fun `GIVEN an empty value WHEN encoding THEN returns an empty string`() {
        // Given
        prepareScenario()

        // When
        val result = encodeMailtoComponent("")

        // Then
        assertEquals("", result)
    }

    private fun prepareScenario() {
        encodeMailtoComponent = EncodeMailtoComponentImpl()
    }
}
