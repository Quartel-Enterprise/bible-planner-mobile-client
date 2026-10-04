package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TwoBranchWhenRuleTest {
    private val twoBranchWhenRuleAssertThat = assertThatRule { TwoBranchWhenRule() }

    @Test
    fun `GIVEN a subjectless when with one condition and an else WHEN linting THEN reports the when`() {
        // Given
        val code =
            """
            fun toHeaderRes(shouldShowDonate: Boolean): Int = when {
                shouldShowDonate -> 1
                else -> 2
            }
            """.trimIndent()

        // When
        val linted = twoBranchWhenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 51, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a when with a subject and one branch besides else WHEN linting THEN reports the when`() {
        // Given
        val code =
            """
            fun toEvent(group: WeekGroup): Int = when (group) {
                WeekGroup.Completed -> 1
                else -> 2
            }
            """.trimIndent()

        // When
        val linted = twoBranchWhenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 38, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a branch with several conditions next to an else WHEN linting THEN reports the when`() {
        // Given
        val code =
            """
            fun toReason(code: Int): Int = when (code) {
                NO_FILL, MEDIATION_NO_FILL -> 1
                else -> 2
            }
            """.trimIndent()

        // When
        val linted = twoBranchWhenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 32, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a when with three or more branches WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun toLabel(code: Int): String = when (code) {
                0 -> "none"
                1 -> "one"
                else -> "many"
            }
            """.trimIndent()

        // When
        val linted = twoBranchWhenRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an exhaustive two-case dispatch without else WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun toLabel(state: ToggleState): String = when (state) {
                ToggleState.On -> "on"
                ToggleState.Off -> "off"
            }
            """.trimIndent()

        // When
        val linted = twoBranchWhenRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "A when with a single branch besides else is an if/else: keep when for three or more branches"
    }
}
