package com.quare.bibleplanner.core.installattribution.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InstallReferrerParserTest {
    private lateinit var parser: InstallReferrerParser

    @Test
    fun `GIVEN a referrer with a click id WHEN parsing THEN returns the click id`() {
        // Given
        prepareScenario()

        // When
        val oppref = parser.parseOppref("oppref=XYZ&utm_source=openai&utm_medium=cpc")

        // Then
        assertEquals(
            expected = "XYZ",
            actual = oppref,
        )
    }

    @Test
    fun `GIVEN an encoded click id WHEN parsing THEN returns it decoded`() {
        // Given
        prepareScenario()

        // When
        val oppref = parser.parseOppref("utm_source=openai&oppref=a%2Bb%3Dc%2Fd")

        // Then
        assertEquals(
            expected = "a+b=c/d",
            actual = oppref,
        )
    }

    @Test
    fun `GIVEN a referrer still encoded as a whole WHEN parsing THEN decodes it before reading the click id`() {
        // Given
        prepareScenario()

        // When
        val oppref = parser.parseOppref("oppref%3DXYZ%26utm_source%3Dopenai")

        // Then
        assertEquals(
            expected = "XYZ",
            actual = oppref,
        )
    }

    @Test
    fun `GIVEN an organic referrer WHEN parsing THEN returns null`() {
        // Given
        prepareScenario()

        // When
        val oppref = parser.parseOppref("utm_source=google-play&utm_medium=organic")

        // Then
        assertNull(oppref)
    }

    @Test
    fun `GIVEN an empty click id WHEN parsing THEN returns null`() {
        // Given
        prepareScenario()

        // When
        val oppref = parser.parseOppref("oppref=&utm_source=openai")

        // Then
        assertNull(oppref)
    }

    @Test
    fun `GIVEN no referrer WHEN parsing THEN returns null`() {
        // Given
        prepareScenario()

        // When
        val missing = parser.parseOppref(null)
        val blank = parser.parseOppref("  ")

        // Then
        assertNull(missing)
        assertNull(blank)
    }

    private fun prepareScenario() {
        parser = InstallReferrerParser()
    }
}
