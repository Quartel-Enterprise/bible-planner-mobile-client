package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class SuspendRunCatchingRuleTest {
    private val suspendRunCatchingRuleAssertThat = assertThatRule { SuspendRunCatchingRule() }

    @Test
    fun `GIVEN a cancellation rethrown before catching Exception WHEN linting THEN reports the try`() {
        // Given
        val code =
            """
            suspend fun load() {
                try {
                    fetch()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log(e)
                }
            }
            """.trimIndent()

        // When
        val linted = suspendRunCatchingRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a qualified cancellation rethrown before catching Throwable WHEN linting THEN reports the try`() {
        // Given
        val code =
            """
            suspend fun load() {
                try {
                    fetch()
                } catch (cancellation: kotlinx.coroutines.CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    log(throwable)
                }
            }
            """.trimIndent()

        // When
        val linted = suspendRunCatchingRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN cancellation handled on its own WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            suspend fun download() {
                try {
                    fetch()
                } catch (_: CancellationException) {
                    showPaused()
                } catch (e: Exception) {
                    log(e)
                }
            }
            """.trimIndent()

        // When
        val linted = suspendRunCatchingRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a specific exception caught after the rethrow WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            suspend fun load() {
                try {
                    fetch()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (e: IOException) {
                    log(e)
                }
            }
            """.trimIndent()

        // When
        val linted = suspendRunCatchingRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a try without a cancellation clause WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun parse(text: String): Int? = try {
                text.toInt()
            } catch (e: NumberFormatException) {
                null
            }
            """.trimIndent()

        // When
        val linted = suspendRunCatchingRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "Rethrowing CancellationException and catching everything else by hand is what suspendRunCatching does: " +
                "use it and handle the Result with onSuccess/onFailure"
    }
}
