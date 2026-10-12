package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MaxFileLengthRuleTest {
    private val maxFileLengthRuleAssertThat = assertThatRule { MaxFileLengthRule() }

    @Test
    fun `GIVEN a file one line over the limit WHEN linting THEN reports it on its first line`() {
        // Given
        val code = buildCode(lineCount = 601)

        // When
        val linted = maxFileLengthRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            1,
            1,
            "File has 601 lines, more than the 600 allowed: move a responsibility into a class, mapper or " +
                "composable of its own",
        )
    }

    @Test
    fun `GIVEN a file exactly at the limit WHEN linting THEN reports nothing`() {
        // Given
        val code = buildCode(lineCount = 600)

        // When
        val linted = maxFileLengthRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a file at the limit ending with a newline WHEN linting THEN does not count the newline as a line`() {
        // Given
        val code = buildCode(lineCount = 600) + "\n"

        // When
        val linted = maxFileLengthRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildCode(lineCount: Int): String =
    (1..lineCount).joinToString(separator = "\n") { line -> "val v$line = $line" }
