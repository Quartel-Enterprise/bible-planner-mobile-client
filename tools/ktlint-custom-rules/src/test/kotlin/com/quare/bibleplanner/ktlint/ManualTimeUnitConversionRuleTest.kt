package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class ManualTimeUnitConversionRuleTest {
    private val manualTimeUnitConversionRuleAssertThat = assertThatRule { ManualTimeUnitConversionRule() }

    @Test
    fun `GIVEN minutes converted to milliseconds by hand WHEN linting THEN reports the multiplication`() {
        // Given
        val code =
            """
            class ListeningMediaSession {
                private companion object {
                    const val WAKE_LOCK_TIMEOUT_MILLIS = 30L * 60 * 1000
                }
            }
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 46, buildViolationMessage("30L * 60 * 1000"))
    }

    @Test
    fun `GIVEN days and hours converted by hand WHEN linting THEN reports each chain once`() {
        // Given
        val code =
            """
            val cooldown = 60L * 24 * 60 * 60 * 1_000
            val window = (24L * 60) * 60
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 16, buildViolationMessage("60L * 24 * 60 * 60 * 1_000")),
            LintViolation(2, 14, buildViolationMessage("(24L * 60) * 60")),
        )
    }

    @Test
    fun `GIVEN a variable converted to milliseconds by hand WHEN linting THEN reports the multiplication`() {
        // Given
        val code =
            """
            fun toMillis(minutes: Long): Long = minutes * 60 * 1000
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 37, buildViolationMessage("minutes * 60 * 1000"))
    }

    @Test
    fun `GIVEN a sum of two hand conversions WHEN linting THEN reports each term`() {
        // Given
        val code =
            """
            val timestamp = 5 * 60 * 60 * 1_000L + 7 * 60 * 1_000L
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 17, buildViolationMessage("5 * 60 * 60 * 1_000L")),
            LintViolation(1, 40, buildViolationMessage("7 * 60 * 1_000L")),
        )
    }

    @Test
    fun `GIVEN unsigned and floating point factors WHEN linting THEN reports the multiplications`() {
        // Given
        val code =
            """
            val timeout = 30u * 60u * 1000u
            val delay = seconds * 60.0 * 1000
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 15, buildViolationMessage("30u * 60u * 1000u")),
            LintViolation(2, 13, buildViolationMessage("seconds * 60.0 * 1000")),
        )
    }

    @Test
    fun `GIVEN a single unit factor WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            val seconds = minutes * 60
            val millis = seconds * 1000
            val hours = days * 24
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN multiplications that are not about time WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            val bytes = 1000 * 1000 * 8
            val avatarPixels = 24 * 24
            val gridCells = 60 * 60
            val area = width * height * 60
            val cacheSize = 50L * 1024 * 1024
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a Duration converted for an API WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class ListeningMediaSession {
                private val wakeLockTimeout: Duration = 30.minutes

                fun acquire() {
                    wakeLock.acquire(wakeLockTimeout.inWholeMilliseconds)
                }
            }
            """.trimIndent()

        // When
        val linted = manualTimeUnitConversionRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(expression: String): String =
    "'$expression' converts time units by hand: use a kotlin.time.Duration (minutes, hours, days) " +
        "and convert with inWholeMilliseconds or inWholeSeconds only where an API takes a number"
